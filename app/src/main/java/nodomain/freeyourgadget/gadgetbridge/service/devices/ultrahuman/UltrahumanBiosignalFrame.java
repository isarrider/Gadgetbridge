/* This file is part of Gadgetbridge, licensed under the GNU Affero General
   Public License, version 3 or later. See LICENSE for details. */

package nodomain.freeyourgadget.gadgetbridge.service.devices.ultrahuman;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.util.CheckSums;

record UltrahumanBiosignalFrame(long batchTimestampMs, List<Ppg> ppg, List<Motion> motion) {
    static final int OPCODE = 0x90;
    private static final int HEADER_SIZE = 3;
    private static final int CRC_SIZE = 2;

    record Ppg(long green, long red, long infrared) {}

    record Motion(short axis1, short axis2, short axis3) {}

    /** Decodes a sample notification. Invalid and metadata-only packets return null. */
    @Nullable
    static UltrahumanBiosignalFrame parse(final byte[] data) {
        if (data.length < HEADER_SIZE + CRC_SIZE || Byte.toUnsignedInt(data[0]) != OPCODE) {
            return null;
        }

        final ByteBuffer bytes = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        final int payloadEnd = data.length - CRC_SIZE;
        final int payloadSize = Short.toUnsignedInt(bytes.getShort(1));
        // CheckSums.crc16_ccitt takes an exclusive end offset as its third argument.
        if (payloadSize != payloadEnd - HEADER_SIZE
                || CheckSums.crc16_ccitt(data, HEADER_SIZE, payloadEnd, 0xffff)
                != Short.toUnsignedInt(bytes.getShort(payloadEnd))) {
            return null;
        }

        bytes.position(HEADER_SIZE);
        bytes.limit(payloadEnd);
        long timestamp = -1;
        final List<Ppg> ppg = new ArrayList<>();
        final List<Motion> motion = new ArrayList<>();
        while (bytes.hasRemaining()) {
            if (bytes.remaining() < 2) {
                return null;
            }
            final int tag = Byte.toUnsignedInt(bytes.get());
            final int size = Byte.toUnsignedInt(bytes.get());
            if (size > bytes.remaining()) {
                return null;
            }
            final int end = bytes.position() + size;
            switch (tag) {
                case 0x08 -> {
                    if (size != Long.BYTES || timestamp != -1) {
                        return null;
                    }
                    timestamp = bytes.getLong();
                    if (timestamp < 0) {
                        return null;
                    }
                }
                case 0x09 -> {
                    if (size % (3 * Integer.BYTES) != 0) {
                        return null;
                    }
                    while (bytes.position() < end) {
                        ppg.add(new Ppg(Integer.toUnsignedLong(bytes.getInt()),
                                Integer.toUnsignedLong(bytes.getInt()), Integer.toUnsignedLong(bytes.getInt())));
                    }
                }
                case 0x0a -> {
                    if (size % (3 * Short.BYTES) != 0) {
                        return null;
                    }
                    while (bytes.position() < end) {
                        motion.add(new Motion(bytes.getShort(), bytes.getShort(), bytes.getShort()));
                    }
                }
                default -> bytes.position(end);
            }
        }
        return timestamp < 0 || (ppg.isEmpty() && motion.isEmpty())
                ? null : new UltrahumanBiosignalFrame(timestamp, ppg, motion);
    }
}
