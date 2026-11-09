/*  Copyright (C) 2026 Pedro

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.soundpeats

import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_ANC_MODE
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_DUAL_CONNECT
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_EQ_PRESET
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_FIND_EARBUDS
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_GAME_MODE
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_MOVIE_MODE
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_PROMPT_VOLUME
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_TOUCH_LOCK
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_WATER_DRAINAGE
import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsAncMode
import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsEqPresets
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.service.AbstractHeadphoneBTBRDeviceSupport
import nodomain.freeyourgadget.gadgetbridge.service.btbr.TransactionBuilder
import org.slf4j.LoggerFactory
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

/**
 * RFCOMM control channel for Soundpeats headphones.
 *
 * The control channel is SPP (RFCOMM channel 0x1101), confirmed by capturing a session between the
 * earbuds and the vendor app: no ATT traffic at all.
 */
class SoundpeatsQ3ProSupport : AbstractHeadphoneBTBRDeviceSupport(LOG, BUFFER_SIZE) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SoundpeatsQ3ProSupport::class.java)
        private const val BUFFER_SIZE = 1024
        private val SERIAL_PORT_SERVICE: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val RFCOMM_CHANNEL = 1
        private const val BATTERY_POLL_INTERVAL_MS = 60_000L
        private const val BATTERY_QUERY_DELAY_MS = 300
        private const val EQ_FRAME_DELAY_MS = 40
    }

    private val protocol = SoundpeatsProtocol()
    private val batteryTimer = Timer("soundpeats-battery", true)
    private var batteryTask: TimerTask? = null

    init {
        addSupportedService(SERIAL_PORT_SERVICE)
    }

    override fun useAutoConnect(): Boolean = false

    override fun getRfcommChannel(): Int = RFCOMM_CHANNEL

    override fun initializeDevice(builder: TransactionBuilder): TransactionBuilder {
        LOG.info("Initializing SOUNDPEATS Q3 Pro")

        builder.write(*SoundpeatsProtocol.encodeGetFirmwareVersion())
        builder.sleep(100)
        builder.write(*SoundpeatsProtocol.encodeGetBattery(SoundpeatsProtocol.BATTERY_INDEX_LEFT))
        builder.sleep(BATTERY_QUERY_DELAY_MS)
        builder.write(*SoundpeatsProtocol.encodeGetBattery(SoundpeatsProtocol.BATTERY_INDEX_RIGHT))
        builder.sleep(BATTERY_QUERY_DELAY_MS)
        builder.write(*SoundpeatsProtocol.encodeGetBattery(SoundpeatsProtocol.BATTERY_INDEX_CASE))
        builder.sleep(100)
        builder.write(*SoundpeatsProtocol.encodeGetAncMode())
        builder.sleep(100)
        builder.write(*SoundpeatsProtocol.encodeGetGameMode())
        builder.sleep(100)
        builder.write(*SoundpeatsProtocol.encodeGetDualConnection())
        builder.sleep(100)
        builder.write(*SoundpeatsProtocol.encodeGetTouchLock())
        builder.sleep(100)
        builder.write(*SoundpeatsProtocol.encodeGetPromptVolume())
        builder.sleep(100)
        builder.write(*SoundpeatsProtocol.encodeGetWaterDrainage())

        startBatteryPolling()

        builder.setDeviceState(GBDevice.State.INITIALIZED)
        return builder
    }

    override fun onSocketRead(data: ByteArray) {
        for (event in protocol.processIncomingBytes(data)) {
            evaluateGBDeviceEvent(event)
        }
    }

    override fun onSendConfiguration(config: String) {
        val prefs = GBApplication.getDeviceSpecificSharedPrefs(device.address)

        when (config) {
            PREF_SOUNDPEATS_ANC_MODE -> {
                val mode = protocol.ancPreference(prefs.getString(config, null))
                sendCommand("set anc mode", SoundpeatsProtocol.encodeSetAncMode(mode))
            }

            PREF_SOUNDPEATS_GAME_MODE -> sendCommand(
                "set game mode",
                SoundpeatsProtocol.encodeSetGameMode(prefs.getBoolean(config, false))
            )

            PREF_SOUNDPEATS_MOVIE_MODE -> sendCommand(
                "set movie mode",
                SoundpeatsProtocol.encodeSetMovieMode(prefs.getBoolean(config, false))
            )

            PREF_SOUNDPEATS_DUAL_CONNECT -> sendCommand(
                "set dual connection",
                SoundpeatsProtocol.encodeSetDualConnection(prefs.getBoolean(config, false))
            )

            PREF_SOUNDPEATS_TOUCH_LOCK -> sendCommand(
                "set touch lock",
                SoundpeatsProtocol.encodeSetTouchLock(prefs.getBoolean(config, false))
            )

            PREF_SOUNDPEATS_PROMPT_VOLUME -> sendCommand(
                "set prompt volume",
                SoundpeatsProtocol.encodeSetPromptVolume(prefs.getInt(config, 50))
            )

            PREF_SOUNDPEATS_EQ_PRESET -> {
                val preset = SoundpeatsEqPresets.Preset.fromPreference(prefs.getString(config, null))
                val frames = SoundpeatsProtocol.encodeEqV2(preset.masterGainDb, preset.toBands())
                val builder = createTransactionBuilder("set eq preset")
                // The earbuds drop bands when the whole profile is written back to back; the vendor
                // app sends them a few milliseconds apart.
                frames.forEachIndexed { index, frame ->
                    if (index > 0) {
                        builder.sleep(EQ_FRAME_DELAY_MS)
                    }
                    builder.write(*frame)
                }
                builder.queue()
            }



            PREF_SOUNDPEATS_WATER_DRAINAGE -> sendCommand(
                "set water drainage",
                SoundpeatsProtocol.encodeSetWaterDrainage(prefs.getBoolean(config, false))
            )

            PREF_SOUNDPEATS_FIND_EARBUDS -> sendFindEarbuds(prefs.getBoolean(config, false))

            else -> super.onSendConfiguration(config)
        }
    }

    private fun sendFindEarbuds(ring: Boolean) {
        val builder = createTransactionBuilder(if (ring) "find earbuds" else "stop find earbuds")
        builder.write(*SoundpeatsProtocol.encodeFindEarbuds(ring))
        builder.sleep(100)
        builder.write(*SoundpeatsProtocol.encodeFindRightEarbud(ring))
        builder.queue()
    }

    /**
     * The earbuds only report a battery level when asked, and an earbud that is charging or in the
     * case answers 0xFF. Polling keeps all three levels on the device card up to date.
     */
    private fun startBatteryPolling() {
        batteryTask?.cancel()
        batteryTask = object : TimerTask() {
            override fun run() {
                if (!isConnected) {
                    return
                }
                val builder = createTransactionBuilder("poll battery")
                builder.write(*SoundpeatsProtocol.encodeGetBattery(SoundpeatsProtocol.BATTERY_INDEX_LEFT))
                builder.sleep(BATTERY_QUERY_DELAY_MS)
                builder.write(*SoundpeatsProtocol.encodeGetBattery(SoundpeatsProtocol.BATTERY_INDEX_RIGHT))
                builder.sleep(BATTERY_QUERY_DELAY_MS)
                builder.write(*SoundpeatsProtocol.encodeGetBattery(SoundpeatsProtocol.BATTERY_INDEX_CASE))
                builder.queue()
            }
        }
        batteryTimer.schedule(batteryTask, BATTERY_POLL_INTERVAL_MS, BATTERY_POLL_INTERVAL_MS)
    }

    override fun dispose() {
        batteryTask?.cancel()
        batteryTimer.cancel()
        super.dispose()
    }

    private fun sendCommand(taskName: String, payload: ByteArray) {
        val builder = createTransactionBuilder(taskName)
        builder.write(*payload)
        builder.queue()
    }
}
