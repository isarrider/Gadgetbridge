/*  Copyright (C) 2026 Gadgetbridge contributors

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState
import nodomain.freeyourgadget.gadgetbridge.service.btbr.AbstractBTBRDeviceSupport
import nodomain.freeyourgadget.gadgetbridge.service.btbr.TransactionBuilder
import nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus.OnePlusProtocol.Proto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.TimeZone
import java.util.UUID

/**
 * OnePlus Watch (2021, W301GB): Bluetooth Classic / RFCOMM, plain protobuf, no app-level authentication
 * (the only requirement is a normal Bluetooth bond). See PROTOCOL.md in the reverse-engineering notes.
 *
 * Implemented: connect, first-time setup, time and timezone sync, battery level and device info.
 */
class OnePlusWatchSupport : AbstractBTBRDeviceSupport(LOG, BUFFER_SIZE) {
    private val deframer = OnePlusProtocol.Deframer()

    override fun getSupportedService(): UUID = SERVICE_UUID

    override fun useAutoConnect(): Boolean = true

    override fun initializeDevice(builder: TransactionBuilder): TransactionBuilder {
        deframer.reset()
        oobeStarted = false
        builder.write(*OnePlusProtocol.HELLO)
        builder.setDeviceState(GBDevice.State.INITIALIZED)
        return builder
    }

    override fun onSocketRead(data: ByteArray) {
        for (msg in deframer.feed(data)) {
            try {
                handle(msg)
            } catch (e: Exception) {
                LOG.warn("Failed to handle {}", msg, e)
            }
        }
    }

    private fun handle(msg: OnePlusProtocol.Message) {
        LOG.debug("RX {}.{} ({} bytes)", msg.service, msg.command, msg.body.size)
        when (msg.service) {
            OnePlusProtocol.SERVICE_DEVICE -> when (msg.command) {
                OnePlusProtocol.CMD_HELLO -> onHelloReply()
                OnePlusProtocol.CMD_BATTERY -> onBattery(msg.body)
                OnePlusProtocol.CMD_DEVICE_INFO -> onDeviceInfo(msg.body)
            }
            OnePlusProtocol.SERVICE_OOBE -> handleOobe(msg)
        }
    }

    // ---------------------------------------------------------------- first-time setup (OOBE)

    private var oobeStarted = false

    /** The watch repeats an empty 11.12 while it sits on its pairing/setup screen: answer with OHealth's setup steps. */
    private fun handleOobe(msg: OnePlusProtocol.Message) {
        if (msg.command == OnePlusProtocol.CMD_OOBE_WAITING && !oobeStarted) {
            oobeStarted = true
            LOG.info("Watch is waiting for setup: starting OOBE sequence")
            val b = createTransactionBuilder("oobe")
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_DEVICE, OnePlusProtocol.CMD_BIND_REQUEST))
            b.sleep(700)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_START_SETTING, Proto.encode(1 to 1L, 2 to 2L)))
            b.sleep(700)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_CONNECT_RESULT, Proto.encode(2 to 2L)))
            b.sleep(700)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_GET_STATUS))
            b.sleep(1500)
            // wrist side (0/1), then OHealth's capability-sync handshake
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_HAND, Proto.encode(1 to 1L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_SYNC_START,
                Proto.encode(5 to 11L, 4 to 10L, 6 to 9L, 3 to 12L, 2 to 13L, 9 to 1L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_SYNC_END,
                Proto.encode(5 to 10L, 2 to 11L, 6 to 9L, 4 to 12L, 9 to 1L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_SYNC_FINISH,
                Proto.encode(5 to 10L, 2 to 11L, 6 to 9L, 4 to 12L, 9 to 1L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_TRANSFER_STATE, Proto.encode(1 to 18L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_GET_STATUS))
            b.queue()
        }
    }

    private fun onHelloReply() {
        LOG.info("Watch is ready")
        requestDeviceInfo()
        requestBattery()
        syncTime()
    }

    // ---------------------------------------------------------------- time sync

    private val mac: String get() = gbDevice.address.uppercase()

    override fun onSetTime() {
        syncTime()
    }

    /**
     * 1.5 in two steps, as OHealth does: first the clock, then the timezone.
     * The timezone offset goes on the wire in half-minute units (UTC+2 = 120 min is sent as 240).
     */
    private fun syncTime() {
        val prefs = getDevicePrefs().preferences
        val now = System.currentTimeMillis() / 1000
        val oldTime = prefs.getLong(PREF_LAST_TIME_SYNC, now)
        val tz = TimeZone.getDefault()
        val offset = tz.getOffset(System.currentTimeMillis()) / 60_000 * 2
        val oldOffset = prefs.getInt(PREF_LAST_TZ_OFFSET, offset)

        val gmt = Proto.encode(1 to Proto.encode(1 to now, 2 to oldTime))
        val sys = Proto.encode(2 to Proto.encode(1 to now, 2 to oldTime, 3 to offset.toLong(), 4 to oldOffset.toLong(), 6 to tz.id))
        val b = createTransactionBuilder("time sync")
        b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_DEVICE, OnePlusProtocol.CMD_TIME_SYNC, Proto.encode(1 to 4L, 5 to gmt)))
        b.sleep(400)
        b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_DEVICE, OnePlusProtocol.CMD_TIME_SYNC, Proto.encode(1 to 4L, 5 to sys)))
        b.queue()
        prefs.edit().putLong(PREF_LAST_TIME_SYNC, now).putInt(PREF_LAST_TZ_OFFSET, offset).apply()
        LOG.info("Time sync sent: {} offset={} half-minutes", tz.id, offset)
    }

    // ---------------------------------------------------------------- battery and device info

    private fun requestBattery() {
        send("battery", OnePlusProtocol.SERVICE_DEVICE, OnePlusProtocol.CMD_BATTERY, Proto.encode(1 to mac))
    }

    /** 1.8: {1: percent, 2: mac, 3: charging}; also pushed by the watch when the level changes. */
    private fun onBattery(body: ByteArray) {
        val f = Proto.decode(body)
        val level = Proto.long(f, 1)?.toInt() ?: return
        val charging = (Proto.long(f, 3) ?: 0L) != 0L
        val event = GBDeviceEventBatteryInfo()
        event.level = level.coerceIn(0, 100)
        event.state = when {
            charging -> BatteryState.BATTERY_CHARGING
            level <= 15 -> BatteryState.BATTERY_LOW
            else -> BatteryState.BATTERY_NORMAL
        }
        evaluateGBDeviceEvent(event)
    }

    private fun requestDeviceInfo() {
        send("device info", OnePlusProtocol.SERVICE_DEVICE, OnePlusProtocol.CMD_DEVICE_INFO, Proto.encode(1 to mac, 13 to mac))
    }

    /** 1.7 reply: 7 = firmware, 21 = full firmware build, 10 = model, 20 = hardware version. */
    private fun onDeviceInfo(body: ByteArray) {
        val f = Proto.decode(body)
        val event = GBDeviceEventVersionInfo()
        event.fwVersion = Proto.string(f, 21) ?: Proto.string(f, 7) ?: return
        event.hwVersion = Proto.string(f, 20) ?: Proto.string(f, 10)
        evaluateGBDeviceEvent(event)
    }

    // ---------------------------------------------------------------- helpers

    private fun send(task: String, service: Int, command: Int, body: ByteArray) {
        createTransactionBuilder(task).write(*OnePlusProtocol.frame(service, command, body)).queue()
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(OnePlusWatchSupport::class.java)

        /** SDP service "SPP SERVER2" (RFCOMM channel 4 on the W301GB). */
        val SERVICE_UUID: UUID = UUID.fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3")
        private const val BUFFER_SIZE = 8192
        private const val PREF_LAST_TIME_SYNC = "oneplus_last_time_sync"
        private const val PREF_LAST_TZ_OFFSET = "oneplus_last_tz_offset"
    }
}
