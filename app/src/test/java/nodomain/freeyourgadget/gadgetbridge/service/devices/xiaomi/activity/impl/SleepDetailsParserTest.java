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
package nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.activity.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.entities.XiaomiSleepStageSample;
import nodomain.freeyourgadget.gadgetbridge.entities.XiaomiSleepStageSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.XiaomiSleepTimeSample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.activity.XiaomiActivityFileId;
import nodomain.freeyourgadget.gadgetbridge.test.TestBase;

/**
 * Tests for {@link SleepDetailsParser}, covering sleep details version 6
 * (observed on Xiaomi Watch S5) and the unchanged behavior of earlier versions.
 *
 * <p>Real version 6 captures are stored under {@code app/src/test/resources}:
 * <ul>
 *   <li>{@code xiaomi-sleep-details-v6-327min.bin}: 28 stage intervals, 327 minutes.</li>
 *   <li>{@code xiaomi-sleep-details-v6-487min-cross-midnight.bin}: 43 stage intervals,
 *   487 minutes across midnight, including an intermediate type 17 snapshot packet.</li>
 * </ul>
 * Both files are exact on-device payloads (CRC32 verified) containing only timestamps
 * and sensor data - no bluetooth addresses or other personal identifiers.
 */
public class SleepDetailsParserTest extends TestBase {
    // Stage values stored in XiaomiSleepStageSample, see SleepDetailsParser.decodeStage
    private static final int STAGE_NOT_SLEEP = 0;
    private static final int STAGE_UNKNOWN = 1;
    private static final int STAGE_DEEP = 2;
    private static final int STAGE_LIGHT = 3;
    private static final int STAGE_REM = 4;
    private static final int STAGE_AWAKE = 5;

    private static final int TYPE_ACTIVITY = 0;
    private static final int SUBTYPE_SLEEP = 0x08;
    private static final int DETAIL_SUMMARY = 0x01;

    private GBDevice gbDevice;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        gbDevice = createDummyGDevice("00:11:22:33:44:55");
        DBHelper.getDevice(gbDevice, daoSession);
        DBHelper.getUser(daoSession);
    }

    @Test
    public void unknownVersionIsRejected() {
        final XiaomiActivityFileId fileId = sleepFileId(7, 1700000000);
        assertFalse(new SleepDetailsParser().parse(getContext(), gbDevice, fileId, new byte[8]));
    }

    @Test
    public void v6SyntheticParsesAllPhases() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 90 * 60;

        final byte[] bytes = v6File(
                bedTime,
                wakeupTime,
                0,
                new int[][]{
                        {0, 3}, // awake
                        {1, 25}, // light
                        {2, 30}, // deep
                        {3, 20}, // rem
                        {4, 5}, // not sleep, preserved as-is (not mapped to awake)
                        {6, 7}, // unknown code -> N/A
                        {1, 0}, // zero duration must not break parsing
                }
        );

        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));

        final List<XiaomiSleepStageSample> stages = orderedStages();
        assertEquals(7, stages.size());

        final int[] expectedStages = {
                STAGE_AWAKE, STAGE_LIGHT, STAGE_DEEP, STAGE_REM, STAGE_NOT_SLEEP, STAGE_UNKNOWN, STAGE_LIGHT,
        };
        final long[] expectedDurationsMin = {3, 25, 30, 20, 5, 7, 0};
        long timestampMs = bedTime * 1000L;
        for (int i = 0; i < expectedStages.length; i++) {
            assertEquals("stage " + i, Integer.valueOf(expectedStages[i]), stages.get(i).getStage());
            assertEquals("timestamp " + i, timestampMs, stages.get(i).getTimestamp());
            timestampMs += expectedDurationsMin[i] * 60000L;
        }
        assertEquals(wakeupTime * 1000L, timestampMs);

        final XiaomiSleepTimeSample summary = singleSummary();
        assertEquals(bedTime * 1000L, summary.getTimestamp());
        assertEquals(Long.valueOf(wakeupTime * 1000L), summary.getWakeupTime());
        // version 6 carries no into-bed time in the parsed header layout
        assertNull(summary.getIntoBedTime());
        assertFalse(summary.getIsAwake());
    }

    @Test
    public void v6SyntheticAcrossMidnight() {
        // 23:50 UTC -> 00:20 UTC next day
        final int bedTime = 1725926400 - 600;
        final int wakeupTime = bedTime + 30 * 60;

        final byte[] bytes = v6File(
                bedTime,
                wakeupTime,
                1,
                new int[][]{{1, 10}, {2, 5}, {3, 15}}
        );

        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));

        final List<XiaomiSleepStageSample> stages = orderedStages();
        assertEquals(3, stages.size());
        assertEquals(bedTime * 1000L, stages.get(0).getTimestamp());
        assertEquals(1725926400L * 1000L, stages.get(1).getTimestamp()); // exactly at midnight
        assertEquals(wakeupTime * 1000L, stages.get(2).getTimestamp() + 15 * 60000L);

        final XiaomiSleepTimeSample summary = singleSummary();
        assertEquals(bedTime * 1000L, summary.getTimestamp());
        assertEquals(Long.valueOf(wakeupTime * 1000L), summary.getWakeupTime());
        assertTrue(summary.getIsAwake());
    }

    @Test
    public void v6TruncatedHeaderIsRejected() {
        final byte[] bytes = new byte[28]; // fileId + padding + 20 bytes, needs 3-byte header + 24-byte v6 block
        final ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(1700000000);
        buf.put((byte) 8);
        buf.put((byte) 6);
        buf.put((byte) 0x21);

        assertFalse(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, 1700000000), bytes));
    }

    @Test
    public void v6TruncatedExtendedHeaderIsRejected() {
        // standard v6 fields, but only 2 of the 24 extended-header bytes present
        final ByteBuffer buf = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(1700000000); // fileId timestamp
        buf.put((byte) 8); // timezone
        buf.put((byte) 6); // version
        buf.put((byte) 0x21); // activity / sleep / summary
        buf.put((byte) 0); // padding
        buf.put((byte) 0xdf); // 3-byte header, bit 3 (sleep quality) set
        buf.put((byte) 0x00);
        buf.put((byte) 0x10); // bit 19 (heart rate samples) set
        buf.put((byte) 1); // isAwake
        buf.putInt(1700000000); // bedTime
        buf.putInt(1700000000 + 3600); // wakeupTime
        buf.put((byte) 80); // sleep quality
        buf.putShort((short) 60); // only 2 of the 24 extended-header bytes present

        assertFalse(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, 1700000000), trim(buf)));
    }

    @Test
    public void v6TruncatedStagePacketIsRejectedWithoutThrowing() {
        final ByteBuffer buf = v6Header(1700000000, 1700000000 + 60 * 60, 0);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) 60); // unit
        buf.putShort((short) 0); // no heart-rate samples
        appendStagePacket(buf, 1700000000, new int[][]{{1, 10}, {2, 20}});

        final byte[] full = trim(buf);
        // cut the payload of the stage packet in half
        final byte[] truncated = new byte[full.length - 2];

        System.arraycopy(full, 0, truncated, 0, truncated.length);
        assertFalse(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, 1700000000), truncated));
    }

    @Test
    public void v4LegacyPathStillParsesStages() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 40 * 60;

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeFileId(out, 4, bedTime);
        out.write(0); // padding
        out.write(0x00); // header bitmask: no optional sections
        out.write(0); // isAwake
        writeIntLE(out, bedTime);
        writeIntLE(out, wakeupTime);

        final ByteBuffer packet = framedPacketBuffer();
        appendStagePacket(packet, bedTime, new int[][]{{1, 10}, {2, 20}, {3, 10}});
        final byte[] packetBytes = trim(packet);
        out.write(packetBytes, 0, packetBytes.length);
        appendCrc(out);

        final byte[] bytes = out.toByteArray();
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(4, bedTime), bytes));

        final List<XiaomiSleepStageSample> stages = orderedStages();
        assertEquals(3, stages.size());
        assertEquals(Integer.valueOf(STAGE_LIGHT), stages.get(0).getStage());
        assertEquals(Integer.valueOf(STAGE_DEEP), stages.get(1).getStage());
        assertEquals(Integer.valueOf(STAGE_REM), stages.get(2).getStage());
        assertEquals(bedTime * 1000L, stages.get(0).getTimestamp());
        assertEquals(wakeupTime * 1000L, stages.get(2).getTimestamp() + 10 * 60000L);

        final XiaomiSleepTimeSample summary = singleSummary();
        assertEquals(bedTime * 1000L, summary.getTimestamp());
        assertEquals(Long.valueOf(wakeupTime * 1000L), summary.getWakeupTime());
    }

    @Test
    public void v6RealSession1() throws IOException {
        final byte[] bytes = readResource("xiaomi-sleep-details-v6-327min.bin");

        final int bedTime = 1788661320; // 2026-09-06 04:22:00 CEST
        final int wakeupTime = 1788680940; // 2026-09-06 09:49:00 CEST

        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));

        final List<XiaomiSleepStageSample> stages = orderedStages();
        assertEquals(28, stages.size());

        final Map<Integer, Long> minutes = minutesByStage(stages, wakeupTime * 1000L);
        assertEquals(215L, (long) minutes.getOrDefault(STAGE_LIGHT, 0L));
        assertEquals(71L, (long) minutes.getOrDefault(STAGE_DEEP, 0L));
        assertEquals(41L, (long) minutes.getOrDefault(STAGE_REM, 0L));
        assertEquals(0L, (long) minutes.getOrDefault(STAGE_AWAKE, 0L));
        assertEquals(327L, minutes.values().stream().mapToLong(Long::longValue).sum());

        assertEquals(bedTime * 1000L, stages.get(0).getTimestamp());

        final XiaomiSleepTimeSample summary = singleSummary();
        assertEquals(bedTime * 1000L, summary.getTimestamp());
        assertEquals(Long.valueOf(wakeupTime * 1000L), summary.getWakeupTime());
        // version 6 carries no into-bed time in the parsed header layout
        assertNull(summary.getIntoBedTime());
        assertTrue(summary.getIsAwake()); // isAwake byte is 1 in the capture
    }

    @Test
    public void v6RealSession2AcrossMidnight() throws IOException {
        final byte[] bytes = readResource("xiaomi-sleep-details-v6-487min-cross-midnight.bin");

        final int bedTime = 1788730680; // 2026-09-06 23:38:00 CEST
        final int wakeupTime = 1788759900; // 2026-09-07 07:45:00 CEST

        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));

        // The file carries an intermediate type 17 snapshot plus the final packet;
        // overlapping samples replace each other, leaving the 43 final intervals
        final List<XiaomiSleepStageSample> stages = orderedStages();
        assertEquals(43, stages.size());

        final Map<Integer, Long> minutes = minutesByStage(stages, wakeupTime * 1000L);
        assertEquals(262L, (long) minutes.getOrDefault(STAGE_LIGHT, 0L));
        assertEquals(119L, (long) minutes.getOrDefault(STAGE_DEEP, 0L));
        assertEquals(90L, (long) minutes.getOrDefault(STAGE_REM, 0L));
        assertEquals(16L, (long) minutes.getOrDefault(STAGE_AWAKE, 0L));
        assertEquals(487L, minutes.values().stream().mapToLong(Long::longValue).sum());

        assertEquals(bedTime * 1000L, stages.get(0).getTimestamp());

        final XiaomiSleepTimeSample summary = singleSummary();
        assertEquals(bedTime * 1000L, summary.getTimestamp());
        assertEquals(Long.valueOf(wakeupTime * 1000L), summary.getWakeupTime());
        // version 6 carries no into-bed time in the parsed header layout
        assertNull(summary.getIntoBedTime());
        assertTrue(summary.getIsAwake()); // isAwake byte is 1 in the capture
    }

    private XiaomiActivityFileId sleepFileId(final int version, final int timestampSeconds) {
        return new XiaomiActivityFileId(
                new Date(timestampSeconds * 1000L),
                8, // 15-minute blocks, CEST
                TYPE_ACTIVITY,
                SUBTYPE_SLEEP,
                DETAIL_SUMMARY,
                version
        );
    }

    private List<XiaomiSleepStageSample> orderedStages() {
        return daoSession.getXiaomiSleepStageSampleDao().queryBuilder()
                .orderAsc(XiaomiSleepStageSampleDao.Properties.Timestamp)
                .list();
    }

    private XiaomiSleepTimeSample singleSummary() {
        final List<XiaomiSleepTimeSample> summaries = daoSession.getXiaomiSleepTimeSampleDao()
                .queryBuilder()
                .list();
        assertEquals(1, summaries.size());
        return summaries.get(0);
    }

    private static Map<Integer, Long> minutesByStage(final List<XiaomiSleepStageSample> stages, final long wakeupTimeMs) {
        final Map<Integer, Long> minutes = new HashMap<>();
        for (int i = 0; i < stages.size(); i++) {
            final long start = stages.get(i).getTimestamp();
            final long end = (i + 1 < stages.size()) ? stages.get(i + 1).getTimestamp() : wakeupTimeMs;
            assertTrue("stages must be contiguous, gap at index " + i, end >= start);
            minutes.merge(stages.get(i).getStage(), (end - start) / 60000L, Long::sum);
        }
        return minutes;
    }

    private static byte[] readResource(final String name) throws IOException {
        try (final InputStream in = SleepDetailsParserTest.class.getResourceAsStream("/" + name)) {
            assertNotNull("missing test resource " + name, in);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) {
                out.write(chunk, 0, read);
            }
            return out.toByteArray();
        }
    }

    private static byte[] v6File(final int bedTime, final int wakeupTime, final int isAwake, final int[][] words) {
        final ByteBuffer buf = v6Header(bedTime, wakeupTime, isAwake);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) 60); // heart-rate unit
        buf.putShort((short) 4); // heart-rate count
        buf.putInt(bedTime + 60); // firstRecordTime
        buf.put(new byte[]{70, 72, 68, 71}); // heart-rate samples
        appendStagePacket(buf, bedTime, words);
        final byte[] bytes = trim(buf);
        return withCrc(bytes);
    }

    private static ByteBuffer v6Header(final int bedTime, final int wakeupTime, final int isAwake) {
        final ByteBuffer buf = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(bedTime); // fileId timestamp
        buf.put((byte) 8); // timezone, 15-minute blocks
        buf.put((byte) 6); // version
        buf.put((byte) 0x21); // activity / sleep / summary
        buf.put((byte) 0); // padding
        // 3-byte section bitmask, as observed in real v6 captures:
        // bit 3 (sleep quality) and bit 19 (heart rate samples) set
        buf.put((byte) 0xdf);
        buf.put((byte) 0x00);
        buf.put((byte) 0x10);
        buf.put((byte) isAwake);
        buf.putInt(bedTime);
        buf.putInt(wakeupTime);
        buf.put((byte) 80); // sleep quality, consumed when bit 3 is set
        buf.put(new byte[24]); // unknown extended header, all zeros in the seen v6 files
        return buf;
    }

    private static void appendStagePacket(final ByteBuffer buf, final long packetTimestampSeconds, final int[][] words) {
        buf.put((byte) 0xfb);
        buf.put((byte) 0xfa);
        buf.put((byte) 0xfc);
        buf.put((byte) 0xff); // magic
        buf.put((byte) 0x11); // header length
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putLong(packetTimestampSeconds); // seconds for type 17
        buf.put((byte) 1); // parity
        buf.put((byte) 17); // stages
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putShort((short) (words.length * 2)); // data length
        for (final int[] word : words) {
            buf.putShort((short) ((word[0] << 12) | word[1]));
        }
        buf.order(ByteOrder.LITTLE_ENDIAN);
    }

    private static ByteBuffer framedPacketBuffer() {
        return ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static byte[] trim(final ByteBuffer buf) {
        final byte[] bytes = new byte[buf.position()];
        buf.rewind();
        buf.get(bytes);
        return bytes;
    }

    private static byte[] withCrc(final byte[] bytes) {
        final CRC32 crc = new CRC32();
        crc.update(bytes);
        final ByteBuffer buf = ByteBuffer.allocate(bytes.length + 4).order(ByteOrder.LITTLE_ENDIAN);
        buf.put(bytes);
        buf.putInt((int) crc.getValue());
        return buf.array();
    }

    private static void appendCrc(final ByteArrayOutputStream out) {
        final byte[] bytes = out.toByteArray();
        final CRC32 crc = new CRC32();
        crc.update(bytes);
        writeIntLE(out, (int) crc.getValue());
    }

    private static void writeFileId(final ByteArrayOutputStream out, final int version, final int timestamp) {
        writeIntLE(out, timestamp);
        out.write(8); // timezone
        out.write(version);
        out.write(0x21); // activity / sleep / summary
    }

    private static void writeIntLE(final ByteArrayOutputStream out, final int value) {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
        out.write((value >> 16) & 0xff);
        out.write((value >> 24) & 0xff);
    }
}
