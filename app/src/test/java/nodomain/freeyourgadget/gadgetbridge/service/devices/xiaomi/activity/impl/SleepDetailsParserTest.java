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
import nodomain.freeyourgadget.gadgetbridge.entities.XiaomiSleepTimeSampleDao;
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
        assertEquals(Long.valueOf(bedTime * 1000L), summary.getIntoBedTime());
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
        final ByteBuffer buf = ByteBuffer.allocate(96).order(ByteOrder.LITTLE_ENDIAN);
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
        buf.put(new byte[9]); // 9-byte metadata
        buf.putInt(1700000000); // bedTime2 (intoBedTime)
        buf.putInt(1700000000 + 3600); // wakeupTime2
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
        // bedTime2 in capture is 1788660960 (04:16:00, 6 minutes before bedTime)
        assertEquals(Long.valueOf(1788660960L * 1000L), summary.getIntoBedTime());
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
        // bedTime2 in capture is 1788730140 (23:29:00, 9 minutes before bedTime)
        assertEquals(Long.valueOf(1788730140L * 1000L), summary.getIntoBedTime());
        assertTrue(summary.getIsAwake()); // isAwake byte is 1 in the capture
    }

    @Test
    public void v6RealSessionNov16ConsolidatedSnapshots() throws IOException {
        final byte[] bytes = readResource("xiaomi-sleep-details-v6-507min.bin");

        final int bedTime = 1794779640; // 2026-11-15 22:54:00 CET
        final int wakeupTime = 1794810060; // 2026-11-16 07:21:00 CET

        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));

        final List<XiaomiSleepStageSample> stages = orderedStages();
        // The file carries intermediate type 17 snapshots (25 and 31 words) before the final 35-word snapshot.
        // The final snapshot must supersede earlier checkpoints rather than accumulate into SQLite.
        assertEquals(35, stages.size());

        final Map<Integer, Long> minutes = minutesByStage(stages, wakeupTime * 1000L);
        assertEquals(376L, (long) minutes.getOrDefault(STAGE_LIGHT, 0L));
        assertEquals(85L, (long) minutes.getOrDefault(STAGE_DEEP, 0L));
        assertEquals(40L, (long) minutes.getOrDefault(STAGE_REM, 0L));
        assertEquals(6L, (long) minutes.getOrDefault(STAGE_AWAKE, 0L));
        assertEquals(507L, minutes.values().stream().mapToLong(Long::longValue).sum());

        assertEquals(bedTime * 1000L, stages.get(0).getTimestamp());

        final XiaomiSleepTimeSample summary = singleSummary();
        assertEquals(bedTime * 1000L, summary.getTimestamp());
        assertEquals(Long.valueOf(wakeupTime * 1000L), summary.getWakeupTime());
        // bedTime2 in capture is 1794779400 (22:50:00, 4 minutes before bedTime)
        assertEquals(Long.valueOf(1794779400L * 1000L), summary.getIntoBedTime());
        assertTrue(summary.getIsAwake());

        // Reprocessing the same file must be strictly idempotent
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));
        final List<XiaomiSleepStageSample> reprocessedStages = orderedStages();
        assertEquals("Reprocessing must produce exactly 35 stage samples with no duplicates", 35, reprocessedStages.size());
        final List<XiaomiSleepTimeSample> reprocessedSummaries = daoSession.getXiaomiSleepTimeSampleDao().loadAll();
        assertEquals("Reprocessing must not duplicate summary", 1, reprocessedSummaries.size());
    }

    @Test
    public void v6IntermediateSnapshotBoundaryRevision() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 120 * 60;

        // Packet 1: 30m light + 31m REM + 1m light (62m)
        // Packet 2: 30m light + 32m REM (revised from 31m+1m) + 58m deep (120m)
        final ByteBuffer buf = v6Header(bedTime, wakeupTime, 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) 60); // unit
        buf.putShort((short) 0);  // count

        appendStagePacket(buf, bedTime, new int[][]{{1, 30}, {3, 31}, {1, 1}});
        appendStagePacket(buf, bedTime, new int[][]{{1, 30}, {3, 32}, {2, 58}});

        final byte[] bytes = withCrc(trim(buf));

        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));

        final List<XiaomiSleepStageSample> stages = orderedStages();
        // The revised boundary in packet 2 must supersede packet 1 without leaving a phantom light sample
        assertEquals(3, stages.size());

        final Map<Integer, Long> minutes = minutesByStage(stages, wakeupTime * 1000L);
        assertEquals(30L, (long) minutes.getOrDefault(STAGE_LIGHT, 0L));
        assertEquals(32L, (long) minutes.getOrDefault(STAGE_REM, 0L));
        assertEquals(58L, (long) minutes.getOrDefault(STAGE_DEEP, 0L));
        assertEquals(120L, minutes.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    public void v6SnapshotWithFewerSegmentsButLongerDuration() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 180 * 60;

        // Packet 1: 4 segments, 120 minutes total (30m light, 30m deep, 30m light, 30m REM)
        // Packet 2: 3 segments, 180 minutes total (60m light [merged], 60m deep, 60m REM)
        // Packet 2 has fewer segments (3 < 4) but longer duration (180m > 120m).
        // It must supersede Packet 1.
        final ByteBuffer buf = v6Header(bedTime, wakeupTime, 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) 60); // unit
        buf.putShort((short) 0);  // count

        appendStagePacket(buf, bedTime, new int[][]{{1, 30}, {2, 30}, {1, 30}, {3, 30}});
        appendStagePacket(buf, bedTime, new int[][]{{1, 60}, {2, 60}, {3, 60}});

        final byte[] bytes = withCrc(trim(buf));

        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));

        final List<XiaomiSleepStageSample> stages = orderedStages();
        assertEquals(3, stages.size());

        final Map<Integer, Long> minutes = minutesByStage(stages, wakeupTime * 1000L);
        assertEquals(60L, (long) minutes.getOrDefault(STAGE_LIGHT, 0L));
        assertEquals(60L, (long) minutes.getOrDefault(STAGE_DEEP, 0L));
        assertEquals(60L, (long) minutes.getOrDefault(STAGE_REM, 0L));
        assertEquals(180L, minutes.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    public void v6AtomicRollbackOnPersistenceFailure() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 60 * 60;

        // Initial parse of a 60m session (2 segments)
        final byte[] initialBytes = v6File(
                bedTime,
                wakeupTime,
                0,
                new int[][]{{1, 30}, {2, 30}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), initialBytes));
        assertEquals(2, orderedStages().size());
        final XiaomiSleepTimeSample initialSummary = singleSummary();
        assertEquals(Long.valueOf(wakeupTime * 1000L), initialSummary.getWakeupTime());
        assertFalse(initialSummary.getIsAwake());

        // Prepare an updated session file (3 segments, 90m)
        final byte[] updatedBytes = v6File(
                bedTime,
                bedTime + 90 * 60,
                1,
                new int[][]{{1, 30}, {2, 30}, {3, 30}}
        );

        // Install a SQLite trigger that forces INSERT on XIAOMI_SLEEP_STAGE_SAMPLE to fail
        daoSession.getDatabase().execSQL("CREATE TRIGGER test_fail_stage_insert BEFORE INSERT ON XIAOMI_SLEEP_STAGE_SAMPLE BEGIN SELECT RAISE(ABORT, 'forced test failure'); END;");
        try {
            assertFalse(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), updatedBytes));
        } finally {
            daoSession.getDatabase().execSQL("DROP TRIGGER IF EXISTS test_fail_stage_insert;");
        }

        // The transaction must have rolled back: both stages and summary must be fully preserved
        final List<XiaomiSleepStageSample> stagesAfterRollback = orderedStages();
        assertEquals("Original samples must be preserved after rollback", 2, stagesAfterRollback.size());
        assertEquals(bedTime * 1000L, stagesAfterRollback.get(0).getTimestamp());
        assertEquals(Integer.valueOf(STAGE_LIGHT), stagesAfterRollback.get(0).getStage());
        assertEquals(Integer.valueOf(STAGE_DEEP), stagesAfterRollback.get(1).getStage());

        final XiaomiSleepTimeSample summaryAfterRollback = singleSummary();
        assertEquals("Summary wakeup time must be rolled back to initial value", Long.valueOf(wakeupTime * 1000L), summaryAfterRollback.getWakeupTime());
        assertFalse("Summary isAwake must be rolled back to initial value", summaryAfterRollback.getIsAwake());
    }

    @Test
    public void v6SessionBoundaryRevisionAndDisjointSessions() {
        // Device A: Initial Night session (bedTime = 1700000000, preliminary wakeup = +120m, 3 segments)
        final int bedTimeA = 1700000000;
        final int prelimWakeupA = bedTimeA + 120 * 60;
        final byte[] fileA1 = v6File(
                bedTimeA,
                prelimWakeupA,
                0,
                new int[][]{{1, 40}, {2, 40}, {3, 40}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTimeA), fileA1));

        // Device A: Afternoon Nap (bedTime = 1700050000, 40m, 1 segment)
        final int napBedTimeA = 1700050000;
        final int napWakeupA = napBedTimeA + 40 * 60;
        final byte[] napFileA = v6File(
                napBedTimeA,
                napWakeupA,
                1,
                new int[][]{{1, 40}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, napBedTimeA), napFileA));

        // Device B: Separate device with overlapping session (bedTime = 1700000000, 60m)
        final GBDevice gbDeviceB = createDummyGDevice("AA:BB:CC:DD:EE:FF");
        DBHelper.getDevice(gbDeviceB, daoSession);
        final byte[] fileB = v6File(
                bedTimeA,
                bedTimeA + 60 * 60,
                0,
                new int[][]{{2, 60}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDeviceB, sleepFileId(6, bedTimeA), fileB));

        // Verify initial counts
        final long deviceAId = DBHelper.findDevice(gbDevice, daoSession).getId();
        final long deviceBId = DBHelper.findDevice(gbDeviceB, daoSession).getId();

        assertEquals(4, daoSession.getXiaomiSleepStageSampleDao().queryBuilder()
                .where(XiaomiSleepStageSampleDao.Properties.DeviceId.eq(deviceAId)).count());
        assertEquals(1, daoSession.getXiaomiSleepStageSampleDao().queryBuilder()
                .where(XiaomiSleepStageSampleDao.Properties.DeviceId.eq(deviceBId)).count());

        assertEquals(2, daoSession.getXiaomiSleepTimeSampleDao().queryBuilder()
                .where(XiaomiSleepTimeSampleDao.Properties.DeviceId.eq(deviceAId)).count());
        assertEquals(1, daoSession.getXiaomiSleepTimeSampleDao().queryBuilder()
                .where(XiaomiSleepTimeSampleDao.Properties.DeviceId.eq(deviceBId)).count());

        // Now, update Device A Night session: wakeup revised backwards to +80m (2 segments of 40m)
        // The old tail sample at +80m must be purged, while Nap and Device B remain intact.
        final int revisedWakeupA = bedTimeA + 80 * 60;
        final byte[] fileA2 = v6File(
                bedTimeA,
                revisedWakeupA,
                1,
                new int[][]{{1, 40}, {2, 40}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTimeA), fileA2));

        // Verify Device A Night session now has exactly 2 segments
        final List<XiaomiSleepStageSample> nightStagesA = daoSession.getXiaomiSleepStageSampleDao().queryBuilder()
                .where(
                        XiaomiSleepStageSampleDao.Properties.DeviceId.eq(deviceAId),
                        XiaomiSleepStageSampleDao.Properties.Timestamp.ge(bedTimeA * 1000L),
                        XiaomiSleepStageSampleDao.Properties.Timestamp.lt(prelimWakeupA * 1000L)
                )
                .orderAsc(XiaomiSleepStageSampleDao.Properties.Timestamp)
                .list();
        assertEquals(2, nightStagesA.size());
        assertEquals(Integer.valueOf(STAGE_LIGHT), nightStagesA.get(0).getStage());
        assertEquals(Integer.valueOf(STAGE_DEEP), nightStagesA.get(1).getStage());

        // Verify Device A Night summary is updated to revised wakeup and isAwake=true
        final XiaomiSleepTimeSample nightSummaryA = daoSession.getXiaomiSleepTimeSampleDao().queryBuilder()
                .where(
                        XiaomiSleepTimeSampleDao.Properties.DeviceId.eq(deviceAId),
                        XiaomiSleepTimeSampleDao.Properties.Timestamp.eq(bedTimeA * 1000L)
                )
                .unique();
        assertNotNull(nightSummaryA);
        assertEquals(Long.valueOf(revisedWakeupA * 1000L), nightSummaryA.getWakeupTime());
        assertTrue(nightSummaryA.getIsAwake());

        // Verify Device A Nap is completely untouched (1 segment + summary)
        final List<XiaomiSleepStageSample> napStagesA = daoSession.getXiaomiSleepStageSampleDao().queryBuilder()
                .where(
                        XiaomiSleepStageSampleDao.Properties.DeviceId.eq(deviceAId),
                        XiaomiSleepStageSampleDao.Properties.Timestamp.ge(napBedTimeA * 1000L)
                )
                .list();
        assertEquals(1, napStagesA.size());
        assertEquals(Integer.valueOf(STAGE_LIGHT), napStagesA.get(0).getStage());

        final XiaomiSleepTimeSample napSummaryA = daoSession.getXiaomiSleepTimeSampleDao().queryBuilder()
                .where(
                        XiaomiSleepTimeSampleDao.Properties.DeviceId.eq(deviceAId),
                        XiaomiSleepTimeSampleDao.Properties.Timestamp.eq(napBedTimeA * 1000L)
                )
                .unique();
        assertNotNull(napSummaryA);
        assertEquals(Long.valueOf(napWakeupA * 1000L), napSummaryA.getWakeupTime());

        // Verify Device B is completely untouched (1 segment + summary)
        final List<XiaomiSleepStageSample> stagesB = daoSession.getXiaomiSleepStageSampleDao().queryBuilder()
                .where(XiaomiSleepStageSampleDao.Properties.DeviceId.eq(deviceBId))
                .list();
        assertEquals(1, stagesB.size());
        assertEquals(Integer.valueOf(STAGE_DEEP), stagesB.get(0).getStage());

        final XiaomiSleepTimeSample summaryB = daoSession.getXiaomiSleepTimeSampleDao().queryBuilder()
                .where(XiaomiSleepTimeSampleDao.Properties.DeviceId.eq(deviceBId))
                .unique();
        assertNotNull(summaryB);
        assertEquals(Long.valueOf((bedTimeA + 60 * 60) * 1000L), summaryB.getWakeupTime());
    }

    @Test
    public void v6OlderIntermediateFileDoesNotOverwriteFinalSession() {
        final int bedTime = 1794779640; // 22:54
        final int intermediateWakeup = 1794804300; // 05:45 (diff: 411 min = 24660 s)
        final int finalWakeup = 1794810060; // 07:21 (diff: 507 min = 30420 s)

        // Step 1: Import final file (5 segments matching 507 min, wakeup 07:21, isAwake = 1)
        final byte[] finalBytes = v6File(
                bedTime,
                finalWakeup,
                1,
                new int[][]{{1, 100}, {2, 100}, {1, 100}, {3, 100}, {1, 107}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), finalBytes));
        final List<XiaomiSleepStageSample> stagesBefore = orderedStages();
        assertEquals(5, stagesBefore.size());
        final XiaomiSleepTimeSample summaryBefore = singleSummary();
        assertEquals(Long.valueOf(finalWakeup * 1000L), summaryBefore.getWakeupTime());
        assertTrue(summaryBefore.getIsAwake());

        // Step 2: Later import of older intermediate file (4 segments matching 411 min, wakeup 05:45, isAwake = 0)
        final byte[] intermediateBytes = v6File(
                bedTime,
                intermediateWakeup,
                0,
                new int[][]{{1, 100}, {2, 100}, {1, 100}, {3, 111}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), intermediateBytes));

        // Expectation: Both summary and all (timestamp, stage) pairs remain unchanged from final session!
        final XiaomiSleepTimeSample summaryAfter = singleSummary();
        assertEquals(Long.valueOf(finalWakeup * 1000L), summaryAfter.getWakeupTime());
        assertTrue(summaryAfter.getIsAwake());

        final List<XiaomiSleepStageSample> stagesAfter = orderedStages();
        assertEquals(stagesBefore.size(), stagesAfter.size());
        for (int i = 0; i < stagesBefore.size(); i++) {
            assertEquals("Timestamp mismatch at index " + i, stagesBefore.get(i).getTimestamp(), stagesAfter.get(i).getTimestamp());
            assertEquals("Stage mismatch at index " + i, stagesBefore.get(i).getStage(), stagesAfter.get(i).getStage());
        }
    }

    @Test
    public void v6FinalizedSessionEarlierWakeupCorrectionAndCheckpointRejection() {
        final int bedTime = 1700000000; // 22:00
        final int initialFinalWakeup = bedTime + 120 * 60; // 00:00 (120 min)
        final int checkpointWakeup1 = bedTime + 60 * 60; // 23:00 (60 min)
        final int revisedFinalWakeup = bedTime + 80 * 60; // 23:20 (80 min)
        final int checkpointWakeup2 = bedTime + 50 * 60; // 22:50 (50 min)

        // Step 1: Initial finalized session (120 min, 3 segments, isAwake = 1)
        final byte[] initialFinalBytes = v6File(
                bedTime,
                initialFinalWakeup,
                1,
                new int[][]{{1, 40}, {2, 40}, {1, 40}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), initialFinalBytes));
        assertEquals(3, orderedStages().size());
        final XiaomiSleepTimeSample summaryStep1 = singleSummary();
        assertEquals(Long.valueOf(initialFinalWakeup * 1000L), summaryStep1.getWakeupTime());
        assertTrue(summaryStep1.getIsAwake());

        // Step 2: Intermediate checkpoint (60 min, isAwake = 0) is rejected
        final byte[] checkpoint1Bytes = v6File(
                bedTime,
                checkpointWakeup1,
                0,
                new int[][]{{1, 30}, {2, 30}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), checkpoint1Bytes));
        assertEquals(3, orderedStages().size());
        final XiaomiSleepTimeSample summaryStep2 = singleSummary();
        assertEquals(Long.valueOf(initialFinalWakeup * 1000L), summaryStep2.getWakeupTime());
        assertTrue(summaryStep2.getIsAwake());

        // Step 3: Legitimate finalized correction with earlier wakeup (80 min, 2 segments, isAwake = 1) is accepted
        final byte[] revisedFinalBytes = v6File(
                bedTime,
                revisedFinalWakeup,
                1,
                new int[][]{{1, 40}, {2, 40}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), revisedFinalBytes));
        final XiaomiSleepTimeSample summaryStep3 = singleSummary();
        assertEquals(Long.valueOf(revisedFinalWakeup * 1000L), summaryStep3.getWakeupTime());
        assertTrue(summaryStep3.getIsAwake());
        // The obsolete 3rd segment beyond 23:20 is purged; exactly 2 segments remain
        final List<XiaomiSleepStageSample> stagesStep3 = orderedStages();
        assertEquals(2, stagesStep3.size());
        assertEquals(bedTime * 1000L, (long) stagesStep3.get(0).getTimestamp());
        assertEquals(Integer.valueOf(STAGE_LIGHT), stagesStep3.get(0).getStage());
        assertEquals((bedTime + 40 * 60) * 1000L, (long) stagesStep3.get(1).getTimestamp());
        assertEquals(Integer.valueOf(STAGE_DEEP), stagesStep3.get(1).getStage());

        // Step 4: Another intermediate checkpoint (50 min, isAwake = 0) is still rejected
        final byte[] checkpoint2Bytes = v6File(
                bedTime,
                checkpointWakeup2,
                0,
                new int[][]{{1, 50}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), checkpoint2Bytes));
        assertEquals(2, orderedStages().size());
        final XiaomiSleepTimeSample summaryStep4 = singleSummary();
        assertEquals(Long.valueOf(revisedFinalWakeup * 1000L), summaryStep4.getWakeupTime());
        assertTrue(summaryStep4.getIsAwake());
    }

    @Test
    public void v6FinalizedSessionReimportBehaviorAndOrderDependencyDocumentation() {
        final int bedTime = 1700000000; // 22:00
        final int wakeup120m = bedTime + 120 * 60; // 00:00 (+120 min)
        final int wakeup80m = bedTime + 80 * 60;   // 23:20 (+80 min)

        final byte[] final120mBytes = v6File(
                bedTime,
                wakeup120m,
                1,
                new int[][]{{1, 40}, {2, 40}, {1, 40}}
        );

        final byte[] final80mBytes = v6File(
                bedTime,
                wakeup80m,
                1,
                new int[][]{{1, 40}, {2, 40}}
        );

        // Step 1: Initial finalized session (+120 min) is imported
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), final120mBytes));
        assertEquals(Long.valueOf(wakeup120m * 1000L), singleSummary().getWakeupTime());
        assertEquals(3, orderedStages().size());

        // Step 2: Legitimate finalized correction (+80 min) arrives:
        // Updates wakeup to +80 min, and purges the obsolete tail stage beyond +80 min
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), final80mBytes));
        assertEquals(Long.valueOf(wakeup80m * 1000L), singleSummary().getWakeupTime());
        assertEquals(2, orderedStages().size());

        // Step 3: Reimport of the earlier finalized file (+120 min):
        // Protocol Limitation Documentation:
        // The Xiaomi activity file format lacks any file generation timestamp, revision sequence
        // number, or sync counter across distinct files. Both files carry isAwake=1 and the identical
        // fileId bedtime. Consequently, the parser cannot determine whether the +120m file is an older
        // superseded version or a subsequent extension (e.g. user fell back asleep).
        // Each incoming finalized file is therefore accepted as an authoritative state update from the watch.
        // Reimporting the +120 min file re-establishes the +120 min session.
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), final120mBytes));
        assertEquals(Long.valueOf(wakeup120m * 1000L), singleSummary().getWakeupTime());
        assertEquals(3, orderedStages().size());
    }

    @Test
    public void v6OlderCheckpointWithDifferentBedtimeDoesNotDeleteFinalizedSession() {
        final int finalBedTime = 1700000000; // 22:00
        final int wakeupTime = finalBedTime + 120 * 60; // 00:00 (120 min)
        final int checkpointBedTime = finalBedTime + 30 * 60; // 22:30 (different bedtime)

        // Step 1: Finalized session at 22:00 (3 segments, wakeup 00:00, isAwake = 1)
        final byte[] finalBytes = v6File(
                finalBedTime,
                wakeupTime,
                1,
                new int[][]{{1, 30}, {2, 30}, {1, 60}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, finalBedTime), finalBytes));
        final List<XiaomiSleepStageSample> stagesBefore = orderedStages();
        assertEquals(3, stagesBefore.size());
        final XiaomiSleepTimeSample summaryBefore = singleSummary();
        assertEquals(finalBedTime * 1000L, summaryBefore.getTimestamp());
        assertEquals(Long.valueOf(wakeupTime * 1000L), summaryBefore.getWakeupTime());
        assertTrue(summaryBefore.getIsAwake());

        // Step 2: An unfinalized checkpoint arrives with different bedtime (22:30) but same wakeupTime (isAwake = 0)
        final byte[] checkpointBytes = v6File(
                checkpointBedTime,
                wakeupTime,
                0,
                new int[][]{{2, 30}, {1, 60}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, checkpointBedTime), checkpointBytes));

        // Expectation: The finalized session at finalBedTime (22:00) MUST NOT be deleted or modified!
        final long deviceId = DBHelper.findDevice(gbDevice, daoSession).getId();
        final XiaomiSleepTimeSample finalSummaryAfter = daoSession.getXiaomiSleepTimeSampleDao().queryBuilder()
                .where(
                        XiaomiSleepTimeSampleDao.Properties.DeviceId.eq(deviceId),
                        XiaomiSleepTimeSampleDao.Properties.Timestamp.eq(finalBedTime * 1000L)
                ).unique();
        assertNotNull(finalSummaryAfter);
        assertEquals(Long.valueOf(wakeupTime * 1000L), finalSummaryAfter.getWakeupTime());
        assertTrue(finalSummaryAfter.getIsAwake());

        // All couples (timestamp, stage) of the finalized session must be completely intact
        final List<XiaomiSleepStageSample> stagesFinal = daoSession.getXiaomiSleepStageSampleDao().queryBuilder()
                .where(
                        XiaomiSleepStageSampleDao.Properties.DeviceId.eq(deviceId),
                        XiaomiSleepStageSampleDao.Properties.Timestamp.ge(finalBedTime * 1000L),
                        XiaomiSleepStageSampleDao.Properties.Timestamp.lt(wakeupTime * 1000L)
                )
                .orderAsc(XiaomiSleepStageSampleDao.Properties.Timestamp)
                .list();
        assertEquals(stagesBefore.size(), stagesFinal.size());
        for (int i = 0; i < stagesBefore.size(); i++) {
            assertEquals("Timestamp mismatch at index " + i, stagesBefore.get(i).getTimestamp(), stagesFinal.get(i).getTimestamp());
            assertEquals("Stage mismatch at index " + i, stagesBefore.get(i).getStage(), stagesFinal.get(i).getStage());
        }
    }

    @Test
    public void v6StagesParseFailureDoesNotModifyFinalizedSummary() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 60 * 60;

        // Step 1: Save valid finalized session
        final byte[] validBytes = v6File(
                bedTime,
                wakeupTime,
                1,
                new int[][]{{1, 30}, {2, 30}}
        );
        assertTrue(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), validBytes));
        assertEquals(2, orderedStages().size());
        assertEquals(Long.valueOf(wakeupTime * 1000L), singleSummary().getWakeupTime());

        // Step 2: Incoming truncated file for same bedTime causes BufferUnderflowException during stage parsing
        final ByteBuffer buf = v6Header(bedTime, wakeupTime + 30 * 60, 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) 60); // HR unit
        buf.putShort((short) 0);  // HR count = 0
        // Stage packet with claimed dataLen longer than buffer
        buf.put(new byte[]{(byte) 0xfb, (byte) 0xfa, (byte) 0xfc, (byte) 0xff}); // magic
        buf.put((byte) 17); // header length
        buf.putLong(bedTime);
        buf.put((byte) 1);
        buf.put((byte) 17); // stages
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putShort((short) 100); // dataLen = 100 bytes, but buffer only has 10 bytes
        buf.put(new byte[10]);
        final byte[] truncatedBytes = withCrc(trim(buf));

        // Expectation: parse returns false, and existing finalized summary + stages are untouched!
        assertFalse(new SleepDetailsParser().parse(getContext(), gbDevice, sleepFileId(6, bedTime), truncatedBytes));
        assertEquals(Long.valueOf(wakeupTime * 1000L), singleSummary().getWakeupTime());
        assertEquals(2, orderedStages().size());
    }

    @Test
    public void deviceNotFoundReturnsFalse() {
        final GBDevice unknownGbDevice = createDummyGDevice("99:88:77:66:55:44");
        final int bedTime = 1700000000;
        final byte[] bytes = v6File(bedTime, bedTime + 60 * 60, 1, new int[][]{{1, 60}});

        assertFalse(new SleepDetailsParser().parse(getContext(), unknownGbDevice, sleepFileId(6, bedTime), bytes));
    }

    @Test
    public void v6HeaderWithHeartRateOnly() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 60 * 60;
        // headerByte2 = 0x10 -> bit 19 set (HR only)
        final ByteBuffer buf = v6Header(bedTime, wakeupTime, 1, (byte) 0x10);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) 60); // unit
        buf.putShort((short) 4);  // count = 4
        buf.putInt(bedTime);      // firstRecordTime
        // Include canary magic bytes inside sample payload to ensure parser actually consumes bytes
        // and doesn't rely on readStagePacketHeader magic scanning
        buf.put(new byte[]{(byte) 0xfb, (byte) 0xfa, (byte) 0xfc, (byte) 0xff}); // canary
        appendStagePacket(buf, bedTime, new int[][]{{1, 60}});
        final byte[] bytes = withCrc(trim(buf));

        final SleepDetailsParser parser = new SleepDetailsParser();
        assertTrue(parser.parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));
        // Offset: 62 (v6 header) + 12 (HR: 2 unit + 2 count + 4 firstRecordTime + 4 samples) = 74
        assertEquals(74, parser.getStagePacketsOffset());
        assertEquals(1, orderedStages().size());
        assertEquals(Long.valueOf(wakeupTime * 1000L), singleSummary().getWakeupTime());
    }

    @Test
    public void v6HeaderWithoutHeartRate() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 60 * 60;
        // headerByte2 = 0x00 -> bit 19, 20, 21 not set (No HR, no SpO2, no Snore)
        final ByteBuffer buf = v6Header(bedTime, wakeupTime, 1, (byte) 0x00);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        // Stage packet directly follows extended header at offset 62
        appendStagePacket(buf, bedTime, new int[][]{{1, 60}});
        final byte[] bytes = withCrc(trim(buf));

        final SleepDetailsParser parser = new SleepDetailsParser();
        assertTrue(parser.parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));
        assertEquals(62, parser.getStagePacketsOffset());
        assertEquals(1, orderedStages().size());
        assertEquals(Long.valueOf(wakeupTime * 1000L), singleSummary().getWakeupTime());
    }

    @Test
    public void v6HeaderWithSpO2() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 60 * 60;
        // headerByte2 = 0x18 -> bit 19 (0x10) and bit 20 (0x08) set (HR + SpO2)
        final ByteBuffer buf = v6Header(bedTime, wakeupTime, 1, (byte) 0x18);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        // HR block (12 bytes)
        buf.putShort((short) 60); // unit
        buf.putShort((short) 4);  // count
        buf.putInt(bedTime);      // firstRecordTime
        buf.put(new byte[]{70, 75, 72, 71}); // 4 HR samples
        // SpO2 block (12 bytes) with canary magic bytes in sample payload
        buf.putShort((short) 60); // unit
        buf.putShort((short) 4);  // count
        buf.putInt(bedTime);      // firstRecordTime
        buf.put(new byte[]{(byte) 0xfb, (byte) 0xfa, (byte) 0xfc, (byte) 0xff}); // canary
        appendStagePacket(buf, bedTime, new int[][]{{1, 60}});
        final byte[] bytes = withCrc(trim(buf));

        final SleepDetailsParser parser = new SleepDetailsParser();
        assertTrue(parser.parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));
        // Offset: 62 (v6 header) + 12 (HR) + 12 (SpO2) = 86
        assertEquals(86, parser.getStagePacketsOffset());
        assertEquals(1, orderedStages().size());
        assertEquals(Long.valueOf(wakeupTime * 1000L), singleSummary().getWakeupTime());
    }

    @Test
    public void v6HeaderWithSnore() {
        final int bedTime = 1700000000;
        final int wakeupTime = bedTime + 60 * 60;
        // headerByte2 = 0x14 -> bit 19 (0x10) and bit 21 (0x04) set (HR + Snore)
        final ByteBuffer buf = v6Header(bedTime, wakeupTime, 1, (byte) 0x14);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        // HR block (12 bytes)
        buf.putShort((short) 60); // unit
        buf.putShort((short) 4);  // count
        buf.putInt(bedTime);      // firstRecordTime
        buf.put(new byte[]{70, 75, 72, 71}); // 4 HR samples
        // Snore block (16 bytes: 8 bytes metadata + 2 * 4 bytes float)
        buf.putShort((short) 60); // unit
        buf.putShort((short) 2);  // count
        buf.putInt(bedTime);      // firstRecordTime
        buf.putFloat(12.5f);      // sample 1
        buf.putFloat(14.0f);      // sample 2
        appendStagePacket(buf, bedTime, new int[][]{{1, 60}});
        final byte[] bytes = withCrc(trim(buf));

        final SleepDetailsParser parser = new SleepDetailsParser();
        assertTrue(parser.parse(getContext(), gbDevice, sleepFileId(6, bedTime), bytes));
        // Offset: 62 (v6 header) + 12 (HR) + 16 (Snore) = 90
        assertEquals(90, parser.getStagePacketsOffset());
        assertEquals(1, orderedStages().size());
        assertEquals(Long.valueOf(wakeupTime * 1000L), singleSummary().getWakeupTime());
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
        return v6Header(bedTime, wakeupTime, isAwake, (byte) 0x10);
    }

    private static ByteBuffer v6Header(final int bedTime, final int wakeupTime, final int isAwake, final byte headerByte2) {
        final ByteBuffer buf = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(bedTime); // fileId timestamp
        buf.put((byte) 8); // timezone, 15-minute blocks
        buf.put((byte) 6); // version
        buf.put((byte) 0x21); // activity / sleep / summary
        buf.put((byte) 0); // padding
        // 3-byte section bitmask:
        // byte 0: 0xdf (bit 3 sleep quality set)
        // byte 1: 0x00
        // byte 2: headerByte2 (bit 19 heart rate = 0x10, bit 20 SpO2 = 0x08, bit 21 Snore = 0x04)
        buf.put((byte) 0xdf);
        buf.put((byte) 0x00);
        buf.put(headerByte2);
        buf.put((byte) isAwake);
        buf.putInt(bedTime);
        buf.putInt(wakeupTime);
        buf.put((byte) 80); // sleep quality, consumed when bit 3 is set
        buf.put(new byte[9]); // 9-byte metadata
        buf.putInt(bedTime); // bedTime2 (intoBedTime)
        buf.putInt(wakeupTime); // wakeupTime2
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
