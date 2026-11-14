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

import android.os.Handler
import android.os.Looper
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventNotificationControl
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo
import nodomain.freeyourgadget.gadgetbridge.devices.GenericHeartRateSampleProvider
import nodomain.freeyourgadget.gadgetbridge.devices.GenericSleepStageSampleProvider
import nodomain.freeyourgadget.gadgetbridge.devices.GenericSpo2SampleProvider
import nodomain.freeyourgadget.gadgetbridge.devices.oneplus.OnePlusWatchCoordinator
import nodomain.freeyourgadget.gadgetbridge.entities.GenericHeartRateSample
import nodomain.freeyourgadget.gadgetbridge.entities.GenericSleepStageSample
import nodomain.freeyourgadget.gadgetbridge.entities.GenericSpo2Sample
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec
import nodomain.freeyourgadget.gadgetbridge.service.btbr.AbstractBTBRDeviceSupport
import nodomain.freeyourgadget.gadgetbridge.service.btbr.TransactionBuilder
import nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus.OnePlusProtocol.Proto
import nodomain.freeyourgadget.gadgetbridge.util.GB
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.TimeZone
import java.util.UUID

/**
 * OnePlus Watch (2021, W301GB): Bluetooth Classic / RFCOMM, plain protobuf, no app-level authentication
 * (the only requirement is a normal Bluetooth bond). See PROTOCOL.md in the reverse-engineering notes.
 *
 * Implemented: connect, first-time setup, time and timezone sync, battery level and device info, notifications (+ quick reply, dismiss), heart-rate history sync and monitoring mode, SpO2 and sleep history sync, diagnostic logging.
 */
class OnePlusWatchSupport : AbstractBTBRDeviceSupport(LOG, BUFFER_SIZE) {
    private val deframer = OnePlusProtocol.Deframer()

    // notification id -> spec, so a quick reply from the watch can be routed back to the phone app
    private val sentNotifications = object : LinkedHashMap<Int, NotificationSpec>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, NotificationSpec>?) = size > 64
    }

    // state of the heart-rate fetch in progress
    private var hrStart = 0L
    private var hrPacketsTotal = 0
    private var hrPacketsReceived = 0
    private var hrSamples = ArrayList<GenericHeartRateSample>()

    override fun getSupportedService(): UUID = SERVICE_UUID

    override fun useAutoConnect(): Boolean = true

    override fun initializeDevice(builder: TransactionBuilder): TransactionBuilder {
        deframer.reset()
        hrPacketsTotal = 0
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
        if (diagnostics) {
            LOG.info("RX {}.{} body={}", msg.service, msg.command, hex(msg.body))
        } else {
            LOG.debug("RX {}.{} ({} bytes)", msg.service, msg.command, msg.body.size)
        }
        when (msg.service) {
            OnePlusProtocol.SERVICE_DEVICE -> when (msg.command) {
                OnePlusProtocol.CMD_HELLO -> onHelloReply()
                OnePlusProtocol.CMD_BATTERY -> onBattery(msg.body)
                OnePlusProtocol.CMD_DEVICE_INFO -> onDeviceInfo(msg.body)
            }
            OnePlusProtocol.SERVICE_NOTIFICATION -> handleNotification(msg)
            OnePlusProtocol.SERVICE_OOBE -> handleOobe(msg)
            OnePlusProtocol.SERVICE_FITNESS -> when (msg.command) {
                OnePlusProtocol.CMD_HR_QUERY -> onHeartRateIndex(msg.body)
                OnePlusProtocol.CMD_HR_DETAIL -> onHeartRatePacket(msg.body)
                OnePlusProtocol.CMD_SPO2_QUERY, OnePlusProtocol.CMD_SLEEP_QUERY -> onTypedIndex(msg)
                OnePlusProtocol.CMD_SPO2_DETAIL -> onSpo2Packet(msg.body)
                OnePlusProtocol.CMD_SLEEP_DETAIL -> onSleepPacket(msg.body)
                in PROBE_QUERY_CMDS -> onProbeIndex(msg)
                in PROBE_DETAIL_CMDS -> LOG.info("PROBE detail 5.{} ({}B) body={}", msg.command, msg.body.size, hex(msg.body))
            }
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
        requestHeartRate()
        requestSpo2AndSleep()
        applyHeartRateMonitoring()
        if (diagnostics) probeAll()
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

    // ---------------------------------------------------------------- notifications

    override fun onNotification(notificationSpec: NotificationSpec) {
        val pkg = notificationSpec.sourceAppId ?: "nodomain.freeyourgadget.gadgetbridge"
        val title = notificationSpec.title ?: notificationSpec.sender ?: notificationSpec.sourceName ?: ""
        val text = notificationSpec.body ?: notificationSpec.subject ?: ""
        sentNotifications[notificationSpec.id] = notificationSpec

        val body = Proto.encode(
            1 to notificationSpec.id.toLong(),
            3 to pkg,
            5 to title,
            6 to text,
            7 to (notificationSpec.`when` / 1000),
            9 to (notificationSpec.sourceName ?: pkg),
            11 to 1L,
            12 to "0|$pkg|${notificationSpec.id}|null|10000",
        )
        send("notification", OnePlusProtocol.SERVICE_NOTIFICATION, OnePlusProtocol.CMD_NOTIFICATION_POST, body)
    }

    override fun onDeleteNotification(id: Int) {
        val spec = sentNotifications.remove(id) ?: return
        val pkg = spec.sourceAppId ?: return
        val body = Proto.encode(
            1 to id.toLong(),
            3 to pkg,
            6 to "0|$pkg|$id|null|10000",
        )
        send("remove notification", OnePlusProtocol.SERVICE_NOTIFICATION, OnePlusProtocol.CMD_NOTIFICATION_REMOVE, body)
    }

    private fun handleNotification(msg: OnePlusProtocol.Message) {
        val fields = Proto.decode(msg.body)
        val id = Proto.long(fields, 1)?.toInt() ?: return
        when (msg.command) {
            OnePlusProtocol.CMD_NOTIFICATION_REPLY -> {
                val text = Proto.string(fields, 5) ?: return
                LOG.info("Quick reply from watch for notification {}", id)
                val event = GBDeviceEventNotificationControl()
                event.handle = id.toLong()
                event.event = GBDeviceEventNotificationControl.Event.REPLY
                event.reply = text
                val spec = sentNotifications[id]
                if (spec != null) {
                    val replyAction = spec.attachedActions?.firstOrNull { it?.isReply == true }
                    if (replyAction != null) {
                        event.handle = replyAction.handle
                    } else {
                        event.phoneNumber = spec.phoneNumber
                    }
                }
                evaluateGBDeviceEvent(event)
            }
            OnePlusProtocol.CMD_NOTIFICATION_DISMISSED -> {
                val event = GBDeviceEventNotificationControl()
                event.handle = id.toLong()
                event.event = GBDeviceEventNotificationControl.Event.DISMISS
                evaluateGBDeviceEvent(event)
            }
        }
    }

    // ---------------------------------------------------------------- heart rate history

    override fun onFetchRecordedData(dataTypes: Int) {
        requestBattery()
        requestHeartRate()
        requestSpo2AndSleep()
    }

    /** 5.11: ask which packets exist for the time range; the watch answers with the packet count. */
    private fun requestHeartRate() {
        val now = System.currentTimeMillis() / 1000
        val lastSync = getDevicePrefs().getLong(PREF_LAST_HR_SYNC, 0L)
        val from = if (lastSync > 0) lastSync - 60 else now - 24 * 3600
        hrSamples = ArrayList()
        hrPacketsReceived = 0
        hrPacketsTotal = 0
        send("heart rate query", OnePlusProtocol.SERVICE_FITNESS, OnePlusProtocol.CMD_HR_QUERY, Proto.encode(1 to from, 2 to now))
    }

    /** 5.11 reply: 1 = packet count, 2 = start timestamp, 3 = max days. Then fetch each packet with 5.12. */
    private fun onHeartRateIndex(body: ByteArray) {
        val f = Proto.decode(body)
        hrPacketsTotal = Proto.long(f, 1)?.toInt() ?: 0
        hrStart = Proto.long(f, 2) ?: 0
        LOG.info("Heart rate: {} packets from {}", hrPacketsTotal, hrStart)
        if (hrPacketsTotal == 0) {
            finishHeartRate()
            return
        }
        for (i in 1..hrPacketsTotal) {
            send("heart rate packet $i", OnePlusProtocol.SERVICE_FITNESS, OnePlusProtocol.CMD_HR_DETAIL, Proto.encode(1 to i.toLong(), 2 to hrStart))
        }
    }

    /** 5.12: 1 = index, 2 = start time (s), 3 = repeated items {1 = minutes since start, 2 = bpm, 3 = reliability}. */
    private fun onHeartRatePacket(body: ByteArray) {
        val f = Proto.decode(body)
        val start = Proto.long(f, 2) ?: return
        val db = GBApplication.acquireDB()
        try {
            val session = db.daoSession
            val userId = DBHelper.getUser(session).id!!
            val deviceId = DBHelper.getDevice(gbDevice, session).id!!
            for (item in Proto.repeated(f, 3)) {
                val it = Proto.decode(item)
                val bpm = Proto.long(it, 2)?.toInt() ?: continue
                if (bpm !in 30..250) continue
                val ts = (start + 60 * (Proto.long(it, 1) ?: 0L)) * 1000
                hrSamples.add(GenericHeartRateSample(ts, deviceId, userId, bpm))
            }
        } finally {
            db.close()
        }
        hrPacketsReceived++
        if (hrPacketsReceived >= hrPacketsTotal) finishHeartRate()
    }

    private fun finishHeartRate() {
        if (hrSamples.isNotEmpty()) {
            try {
                GBApplication.acquireDB().use { db ->
                    GenericHeartRateSampleProvider(gbDevice, db.daoSession).addSamples(hrSamples)
                }
                LOG.info("Stored {} heart rate samples", hrSamples.size)
                getDevicePrefs().preferences.edit().putLong(PREF_LAST_HR_SYNC, System.currentTimeMillis() / 1000).apply()
                GB.signalActivityDataFinish(gbDevice)
            } catch (e: Exception) {
                LOG.error("Failed to store heart rate samples", e)
            }
        }
        hrSamples = ArrayList()
        GB.updateTransferNotification(null, "", false, 100, context)
    }

    // ---------------------------------------------------------------- heart-rate monitoring mode + polling

    private val handler = Handler(Looper.getMainLooper())

    private val hrPollTask = object : Runnable {
        override fun run() {
            requestHeartRate()
            handler.postDelayed(this, hrPollIntervalMs)
        }
    }
    private var hrPollIntervalMs = 0L

    override fun onSendConfiguration(config: String) {
        super.onSendConfiguration(config)
        when (config) {
            OnePlusWatchCoordinator.PREF_HR_MONITORING -> applyHeartRateMonitoring()
            OnePlusWatchCoordinator.PREF_DIAGNOSTICS -> if (diagnostics) probeAll()
        }
    }

    /**
     * Off / Smart / Continuous. The watch is told to measure automatically
     * (HeartRateMeasure{sw, interval}: interval 3 = AI smart monitoring, 4 = real-time monitoring),
     * and Gadgetbridge pulls the heart-rate history every 10 min / every minute.
     */
    private fun applyHeartRateMonitoring() {
        val prefs = getDevicePrefs()
        val mode = prefs.getString(OnePlusWatchCoordinator.PREF_HR_MONITORING, OnePlusWatchCoordinator.HR_MODE_OFF)
        val applied = prefs.getString(PREF_HR_APPLIED, "")
        val (type, pollMs) = when (mode) {
            OnePlusWatchCoordinator.HR_MODE_SMART -> 3L to 10 * 60_000L
            OnePlusWatchCoordinator.HR_MODE_REALTIME -> 4L to 60_000L
            else -> 3L to 0L
        }
        LOG.info("Heart-rate monitoring: {} (applied before: '{}', type={}, poll={} ms)", mode, applied, type, pollMs)

        handler.removeCallbacks(hrPollTask)
        hrPollIntervalMs = pollMs
        if (pollMs > 0) handler.postDelayed(hrPollTask, pollMs)

        // "Off" leaves the watch's own setting alone, unless we changed it earlier: then restore smart mode.
        val touchWatch = mode != OnePlusWatchCoordinator.HR_MODE_OFF || applied.isNotEmpty()
        if (!touchWatch || applied == mode) return

        // The watch acknowledges 5.4 with {1: 100000}; the "MCU" variant 5.180 is not used by this model.
        send("heart rate monitoring", OnePlusProtocol.SERVICE_FITNESS, OnePlusProtocol.CMD_HR_AUTO_MEASURE, Proto.encode(1 to 1L, 2 to type))
        prefs.preferences.edit().putString(PREF_HR_APPLIED, if (mode == OnePlusWatchCoordinator.HR_MODE_OFF) "" else mode).apply()
    }

    override fun dispose() {
        handler.removeCallbacksAndMessages(null)
        super.dispose()
    }

    // ---------------------------------------------------------------- SpO2 and sleep history

    private val spo2Samples = ArrayList<GenericSpo2Sample>()
    private val sleepSamples = ArrayList<GenericSleepStageSample>()
    private val storeSpo2 = Runnable { storeSpo2Now() }
    private val storeSleep = Runnable { storeSleepNow() }

    private fun requestSpo2AndSleep() {
        requestRange(OnePlusProtocol.CMD_SPO2_QUERY, PREF_LAST_SPO2_SYNC)
        requestRange(OnePlusProtocol.CMD_SLEEP_QUERY, PREF_LAST_SLEEP_SYNC)
    }

    /** First sync asks for 14 days; later syncs re-ask from one day before the last sync (sleep spans midnight). */
    private fun requestRange(queryCmd: Int, prefKey: String) {
        val now = System.currentTimeMillis() / 1000
        val last = getDevicePrefs().getLong(prefKey, 0L)
        val from = if (last > 0) last - 86400 else now - 14 * 86400
        send("fetch ${queryCmd}", OnePlusProtocol.SERVICE_FITNESS, queryCmd, Proto.encode(1 to from, 2 to now))
    }

    /** Index reply: {1: packet count, 2: start}. The watch only answers packets that hold data, so flush after a quiet period. */
    private fun onTypedIndex(msg: OnePlusProtocol.Message) {
        val f = Proto.decode(msg.body)
        val total = Proto.long(f, 1)?.toInt() ?: 0
        val start = Proto.long(f, 2) ?: return
        LOG.info("Fetch 5.{}: {} packets from {}", msg.command, total, start)
        val flush = if (msg.command == OnePlusProtocol.CMD_SPO2_QUERY) storeSpo2 else storeSleep
        for (i in 1..minOf(total, MAX_PACKETS)) {
            send("fetch ${msg.command + 1} #$i", OnePlusProtocol.SERVICE_FITNESS, msg.command + 1, Proto.encode(1 to i.toLong(), 2 to start))
        }
        handler.removeCallbacks(flush)
        handler.postDelayed(flush, 4000)
    }

    /**
     * 5.24: {2: start, 3: {1: second offsets, 2: SpO2 %, 3: reliability}} with packed arrays.
     * SpO2 of 0 means no reading at that second.
     */
    private fun onSpo2Packet(body: ByteArray) {
        val f = Proto.decode(body)
        val start = Proto.long(f, 2) ?: return
        val ids = idsOrNull() ?: return
        for (block in Proto.repeated(f, 3)) {
            val b = Proto.decode(block)
            val offsets = Proto.packed(Proto.bytes(b, 1) ?: continue)
            val values = Proto.packed(Proto.bytes(b, 2) ?: continue)
            for (i in 0 until minOf(offsets.size, values.size)) {
                val v = values[i].toInt()
                if (v !in 50..100) continue
                spo2Samples.add(GenericSpo2Sample((start + offsets[i]) * 1000, ids.second, ids.first, v))
            }
        }
        handler.removeCallbacks(storeSpo2)
        handler.postDelayed(storeSpo2, 1500)
    }

    /** 5.14: {1: index, 2: start, 3: one state byte per minute}. Collapsed into stage runs. */
    private fun onSleepPacket(body: ByteArray) {
        val f = Proto.decode(body)
        val start = Proto.long(f, 2) ?: return
        val states = Proto.bytes(f, 3) ?: return
        val ids = idsOrNull() ?: return
        var i = 0
        while (i < states.size) {
            val stage = states[i].toInt() and 0xFF
            var j = i
            while (j < states.size && (states[j].toInt() and 0xFF) == stage) j++
            if (stage != 0) {
                sleepSamples.add(GenericSleepStageSample((start + i * 60L) * 1000, ids.second, ids.first, j - i, stage))
            }
            i = j
        }
        handler.removeCallbacks(storeSleep)
        handler.postDelayed(storeSleep, 1500)
    }

    /** (userId, deviceId) */
    private fun idsOrNull(): Pair<Long, Long>? = try {
        GBApplication.acquireDB().use { db ->
            Pair(DBHelper.getUser(db.daoSession).id!!, DBHelper.getDevice(gbDevice, db.daoSession).id!!)
        }
    } catch (e: Exception) {
        LOG.error("Cannot resolve user/device ids", e)
        null
    }

    private fun storeSpo2Now() {
        val now = System.currentTimeMillis() / 1000
        if (spo2Samples.isNotEmpty()) {
            try {
                GBApplication.acquireDB().use { db -> GenericSpo2SampleProvider(gbDevice, db.daoSession).addSamples(ArrayList(spo2Samples)) }
                LOG.info("Stored {} SpO2 samples", spo2Samples.size)
                GB.signalActivityDataFinish(gbDevice)
            } catch (e: Exception) {
                LOG.error("Failed to store SpO2 samples", e)
            }
            spo2Samples.clear()
        }
        getDevicePrefs().preferences.edit().putLong(PREF_LAST_SPO2_SYNC, now).apply()
    }

    private fun storeSleepNow() {
        val now = System.currentTimeMillis() / 1000
        if (sleepSamples.isNotEmpty()) {
            try {
                GBApplication.acquireDB().use { db -> GenericSleepStageSampleProvider(gbDevice, db.daoSession).addSamples(ArrayList(sleepSamples)) }
                LOG.info("Stored {} sleep stage runs", sleepSamples.size)
                GB.signalActivityDataFinish(gbDevice)
            } catch (e: Exception) {
                LOG.error("Failed to store sleep samples", e)
            }
            sleepSamples.clear()
        }
        getDevicePrefs().preferences.edit().putLong(PREF_LAST_SLEEP_SYNC, now).apply()
    }

    // ---------------------------------------------------------------- diagnostics (decoding research)

    private val diagnostics: Boolean
        get() = getDevicePrefs().getBoolean(OnePlusWatchCoordinator.PREF_DIAGNOSTICS, false)

    private fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }

    /** Ask for 7 days of every other data type and log the raw replies, so their formats can be decoded. */
    private fun probeAll() {
        val now = System.currentTimeMillis() / 1000
        for (cmd in PROBE_QUERY_CMDS) {
            send("probe $cmd", OnePlusProtocol.SERVICE_FITNESS, cmd, Proto.encode(1 to now - 7 * 86400, 2 to now))
        }
    }

    private fun onProbeIndex(msg: OnePlusProtocol.Message) {
        val f = Proto.decode(msg.body)
        val total = Proto.long(f, 1)?.toInt() ?: 0
        val start = Proto.long(f, 2) ?: 0
        LOG.info("PROBE index 5.{} packets={} start={} body={}", msg.command, total, start, hex(msg.body))
        for (i in 1..minOf(total, 8)) {
            send("probe ${msg.command + 1} #$i", OnePlusProtocol.SERVICE_FITNESS, msg.command + 1, Proto.encode(1 to i.toLong(), 2 to start))
        }
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

        // research probe: activity, sleep, resting HR, SpO2, stress  (query cmd; detail cmd = query + 1)
        private val PROBE_QUERY_CMDS = setOf(9, 17, 34)
        private val PROBE_DETAIL_CMDS = PROBE_QUERY_CMDS.map { it + 1 }.toSet()
        private const val PREF_LAST_HR_SYNC = "oneplus_last_hr_sync"
        private const val PREF_LAST_SPO2_SYNC = "oneplus_last_spo2_sync"
        private const val PREF_LAST_SLEEP_SYNC = "oneplus_last_sleep_sync"
        private const val MAX_PACKETS = 60
        private const val PREF_HR_APPLIED = "oneplus_hr_monitoring_applied"
        private const val PREF_LAST_TIME_SYNC = "oneplus_last_time_sync"
        private const val PREF_LAST_TZ_OFFSET = "oneplus_last_tz_offset"
    }
}
