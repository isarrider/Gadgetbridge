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

import java.io.ByteArrayOutputStream

/**
 * Wire protocol of the OnePlus Watch (2021, W301GB), spoken over a Bluetooth Classic
 * RFCOMM link (SDP service UUID db764ac8-4b08-7f25-aafe-59d03c27bae3).
 *
 * Frame: `AA | len (varint) | 00 00 | service | command | protobuf body | CRC16 (LE)`
 *  - `len` counts the bytes after the two `00 00` bytes (service .. CRC inclusive)
 *  - CRC is CRC-16/XMODEM (poly 0x1021, init 0) over every preceding byte including `AA`
 */
object OnePlusProtocol {
    const val SERVICE_DEVICE = 1
    const val SERVICE_NOTIFICATION = 2
    const val SERVICE_CALL = 6           // "HFP extend" in the vendor app: call state both ways
    const val SERVICE_FITNESS = 5
    const val SERVICE_WEATHER = 9
    const val SERVICE_OOBE = 11   // out-of-box setup ("pairing" screen) handshake

    const val CMD_BIND_REQUEST = 26      // service 1: phone asks the watch to allow binding -> reply {1: bind_result (0 = ok)}
    const val CMD_OOBE_HAND = 1          // service 11: wrist side {1: hand, 2: result}
    const val CMD_OOBE_SYNC_START = 2
    const val CMD_OOBE_SYNC_END = 3
    const val CMD_OOBE_CONNECT_RESULT = 6  // {1: connect_result, 2: connect_result_sync, 3: ssoid}
    const val CMD_OOBE_START_SETTING = 7   // {1: start_setting, 2: start_setting_result}
    const val CMD_OOBE_SYNC_FINISH = 9
    const val CMD_OOBE_GET_STATUS = 11
    const val CMD_OOBE_WAITING = 12        // watch -> phone, empty body, repeated every ~6.5 s while the watch waits for setup
    const val CMD_OOBE_TRANSFER_STATE = 26 // {1: state}

    const val CMD_HELLO = 1
    const val CMD_TIME_SYNC = 5          // service 1: {1: 4 (time profile), 5: {1: {time, old_time}} | {2: {time, old_time, tz, old_tz, id}}}
    const val CMD_DEVICE_INFO = 7        // service 1: request {1: mac, 13: mac}; reply has versions, serial, model name
    const val CMD_FIND_PHONE = 15        // service 1, watch -> phone, empty body, repeated about once a second while the watch's find-phone screen is active
    const val CMD_BATTERY = 8            // service 1: request {1: mac}; reply/push {1: percent, 2: mac, 3: charging}
    const val CMD_CALL_STATE = 1         // service 6, phone -> watch: PhoneCallChange {1: status, 2: number, 4: subscription id, 9: is contact, 10: display name, 13: primary}
    const val CMD_SIM_STATE = 6          // service 6, phone -> watch: SIM slots; the watch also sends it empty to ask for them
    const val CMD_CALL_WATCH_EVENT = 2   // service 6, watch -> phone: WatchCallChange {1: status, 2: number, 3: mute}
    const val CMD_NOTIFICATION_POST = 1
    const val CMD_NOTIFICATION_REMOVE = 3
    const val CMD_NOTIFICATION_DISMISSED = 4
    const val CMD_NOTIFICATION_REPLY = 7
    const val CMD_HR_QUERY = 11
    const val CMD_HR_DETAIL = 12
    const val CMD_HR_AUTO_MEASURE = 4        // {1: switch, 2: type (3 = smart, 4 = real-time)}; watch acks {1: 100000}
    const val CMD_ACTIVITY_QUERY = 9
    const val CMD_ACTIVITY_DETAIL = 10
    const val CMD_SLEEP_QUERY = 13
    const val CMD_SLEEP_DETAIL = 14
    const val CMD_SPO2_QUERY = 23
    const val CMD_SPO2_DETAIL = 24

    /** Constant "hello" the phone sends first (service 1, command 1); the watch answers with its limits. */
    val HELLO: ByteArray = frame(SERVICE_DEVICE, CMD_HELLO, Proto.encode(1 to 5L, 2 to 5940L, 3 to 990L, 5 to 3L))

    /** One minute of activity: steps, distance in metres, calories in kcal. */
    class ActivityMinute(val timestamp: Long, val steps: Int, val distanceMeters: Int, val calories: Int)

    /**
     * 5.10: `{1: index, 2: start (s), 3: repeated item}`, item = `{1: minute offset (absent = 0), 2: calories,
     * 3: distance (m), 5: sport type, 7: steps}`. Only minutes with data are present. Minutes without steps,
     * distance or calories are dropped.
     */
    fun parseActivity(body: ByteArray): List<ActivityMinute> {
        val f = Proto.decode(body)
        val start = Proto.long(f, 2) ?: return emptyList()
        return Proto.repeated(f, 3).mapNotNull {
            val item = Proto.decode(it)
            val steps = Proto.long(item, 7)?.toInt() ?: 0
            val distance = Proto.long(item, 3)?.toInt() ?: 0
            val calories = Proto.long(item, 2)?.toInt() ?: 0
            if (steps == 0 && distance == 0 && calories == 0) null
            else ActivityMinute(start + 60 * (Proto.long(item, 1) ?: 0L), steps, distance, calories)
        }
    }

    fun crc16Xmodem(data: ByteArray, length: Int = data.size): Int {
        var crc = 0
        for (i in 0 until length) {
            crc = crc xor ((data[i].toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if (crc and 0x8000 != 0) ((crc shl 1) xor 0x1021) and 0xFFFF else (crc shl 1) and 0xFFFF
            }
        }
        return crc
    }

    fun frame(service: Int, command: Int, body: ByteArray = ByteArray(0)): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(0xAA)
        Proto.writeVarint(out, (body.size + 4).toLong())
        out.write(0)
        out.write(0)
        out.write(service)
        out.write(command)
        out.write(body)
        val partial = out.toByteArray()
        val crc = crc16Xmodem(partial)
        out.write(crc and 0xFF)
        out.write((crc shr 8) and 0xFF)
        return out.toByteArray()
    }

    class Message(val service: Int, val command: Int, val body: ByteArray) {
        override fun toString() = "Message($service.$command, ${body.size}B)"
    }

    /** Streaming parser: bytes in, complete and CRC-valid [Message]s out. Resynchronises on bad frames. */
    class Deframer {
        private var buf = ByteArray(0)

        fun reset() {
            buf = ByteArray(0)
        }

        fun feed(data: ByteArray): List<Message> {
            buf += data
            val out = ArrayList<Message>()
            while (true) {
                val start = buf.indexOf(0xAA.toByte())
                if (start < 0) {
                    buf = ByteArray(0)
                    return out
                }
                if (start > 0) buf = buf.copyOfRange(start, buf.size)
                if (buf.size < 3) return out

                var pos = 1
                var len = 0L
                var shift = 0
                var complete = false
                while (pos < buf.size) {
                    val b = buf[pos++].toInt() and 0xFF
                    len = len or ((b and 0x7F).toLong() shl shift)
                    shift += 7
                    if (b < 0x80) {
                        complete = true
                        break
                    }
                    if (shift > 28) break
                }
                if (!complete) {
                    if (shift > 28) buf = buf.copyOfRange(1, buf.size) // garbage, resync
                    return out
                }
                // after len: 2 flag bytes, then `len` bytes (service, command, body, crc)
                val total = pos + 2 + len.toInt()
                if (len < 4 || len > 65536) { // malformed
                    buf = buf.copyOfRange(1, buf.size)
                    continue
                }
                if (buf.size < total) return out

                val expected = (buf[total - 2].toInt() and 0xFF) or ((buf[total - 1].toInt() and 0xFF) shl 8)
                if (crc16Xmodem(buf, total - 2) != expected) {
                    buf = buf.copyOfRange(1, buf.size) // bad CRC: skip this AA and resync
                    continue
                }
                val svc = buf[pos + 2].toInt() and 0xFF
                val cmd = buf[pos + 3].toInt() and 0xFF
                out.add(Message(svc, cmd, buf.copyOfRange(pos + 4, total - 2)))
                buf = buf.copyOfRange(total, buf.size)
            }
        }
    }

    /** Just enough protobuf for this protocol (varint + length-delimited fields). */
    object Proto {
        fun writeVarint(out: ByteArrayOutputStream, value: Long) {
            var v = value
            while (true) {
                val b = (v and 0x7F).toInt()
                v = v ushr 7
                if (v == 0L) {
                    out.write(b)
                    return
                }
                out.write(b or 0x80)
            }
        }

        /** Values: Long/Int = varint, String/ByteArray = length-delimited, Double = fixed64. */
        fun encode(vararg fields: Pair<Int, Any>): ByteArray {
            val out = ByteArrayOutputStream()
            for ((number, value) in fields) {
                when (value) {
                    is Long -> { writeVarint(out, (number shl 3).toLong()); writeVarint(out, value) }
                    is Int -> { writeVarint(out, (number shl 3).toLong()); writeVarint(out, value.toLong()) }
                    is String -> bytes(out, number, value.toByteArray(Charsets.UTF_8))
                    is ByteArray -> bytes(out, number, value)
                    is Double -> {
                        writeVarint(out, ((number shl 3) or 1).toLong())
                        val bits = java.lang.Double.doubleToLongBits(value)
                        for (i in 0 until 8) out.write(((bits ushr (8 * i)) and 0xff).toInt())
                    }
                    else -> throw IllegalArgumentException("unsupported type ${value::class}")
                }
            }
            return out.toByteArray()
        }

        private fun bytes(out: ByteArrayOutputStream, number: Int, value: ByteArray) {
            writeVarint(out, ((number shl 3) or 2).toLong())
            writeVarint(out, value.size.toLong())
            out.write(value)
        }

        /** Decoded field list; values are Long (varint, fixed32/64) or ByteArray (length-delimited). */
        fun decode(data: ByteArray): List<Pair<Int, Any>> {
            val out = ArrayList<Pair<Int, Any>>()
            var i = 0
            fun varint(): Long {
                var v = 0L
                var shift = 0
                while (true) {
                    if (i >= data.size || shift > 63) throw IllegalArgumentException("truncated varint")
                    val b = data[i++].toInt() and 0xFF
                    v = v or ((b and 0x7F).toLong() shl shift)
                    shift += 7
                    if (b < 0x80) return v
                }
            }
            while (i < data.size) {
                val key = varint()
                val number = (key shr 3).toInt()
                when ((key and 7).toInt()) {
                    0 -> out.add(number to varint())
                    2 -> {
                        val len = varint().toInt()
                        if (len < 0 || i + len > data.size) throw IllegalArgumentException("truncated field")
                        out.add(number to data.copyOfRange(i, i + len))
                        i += len
                    }
                    1 -> { if (i + 8 > data.size) throw IllegalArgumentException("truncated"); var v = 0L; for (k in 7 downTo 0) v = (v shl 8) or (data[i + k].toLong() and 0xFF); out.add(number to v); i += 8 }
                    5 -> { if (i + 4 > data.size) throw IllegalArgumentException("truncated"); var v = 0L; for (k in 3 downTo 0) v = (v shl 8) or (data[i + k].toLong() and 0xFF); out.add(number to v); i += 4 }
                    else -> throw IllegalArgumentException("unsupported wire type")
                }
            }
            return out
        }

        /** Packed repeated varints (proto3 default for repeated ints). */
        fun packed(data: ByteArray): List<Long> {
            val out = ArrayList<Long>()
            var i = 0
            while (i < data.size) {
                var v = 0L
                var shift = 0
                while (true) {
                    if (i >= data.size || shift > 63) throw IllegalArgumentException("truncated packed varint")
                    val b = data[i++].toInt() and 0xFF
                    v = v or ((b and 0x7F).toLong() shl shift)
                    shift += 7
                    if (b < 0x80) break
                }
                out.add(v)
            }
            return out
        }

        fun bytes(fields: List<Pair<Int, Any>>, number: Int): ByteArray? = fields.firstOrNull { it.first == number }?.second as? ByteArray

        fun long(fields: List<Pair<Int, Any>>, number: Int): Long? = fields.firstOrNull { it.first == number }?.second as? Long
        fun string(fields: List<Pair<Int, Any>>, number: Int): String? =
            (fields.firstOrNull { it.first == number }?.second as? ByteArray)?.toString(Charsets.UTF_8)
        fun repeated(fields: List<Pair<Int, Any>>, number: Int): List<ByteArray> =
            fields.filter { it.first == number }.mapNotNull { it.second as? ByteArray }
    }
}
