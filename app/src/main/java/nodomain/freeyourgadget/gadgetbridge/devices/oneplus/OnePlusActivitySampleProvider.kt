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
package nodomain.freeyourgadget.gadgetbridge.devices.oneplus

import nodomain.freeyourgadget.gadgetbridge.database.DBHelper
import nodomain.freeyourgadget.gadgetbridge.devices.GenericHeartRateSampleProvider
import nodomain.freeyourgadget.gadgetbridge.devices.GenericSleepStageSampleProvider
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession
import nodomain.freeyourgadget.gadgetbridge.entities.GenericActivitySample
import nodomain.freeyourgadget.gadgetbridge.entities.GenericHeartRateSampleDao
import nodomain.freeyourgadget.gadgetbridge.entities.OnePlusActivitySample
import nodomain.freeyourgadget.gadgetbridge.entities.OnePlusActivitySampleDao
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind
import java.util.TreeMap

/**
 * Read-only provider that merges the per-minute steps, distance and calories ([OnePlusActivitySample]), the
 * heart-rate samples ([GenericHeartRateSampleProvider]) and the sleep stages ([GenericSleepStageSampleProvider])
 * into one activity sample per minute. Charts and the dashboard sleep widgets read sleep from the activity kind.
 */
class OnePlusActivitySampleProvider(private val device: GBDevice, private val session: DaoSession) :
    SampleProvider<GenericActivitySample> {

    private val heartRateProvider = GenericHeartRateSampleProvider(device, session)
    private val sleepStageProvider = GenericSleepStageSampleProvider(device, session)

    override fun normalizeType(rawType: Int): ActivityKind = ActivityKind.fromCode(rawType)

    override fun toRawActivityKind(activityKind: ActivityKind): Int = activityKind.code

    /** The raw intensity is the number of steps in the minute; about 120 steps per minute is a brisk run. */
    override fun normalizeIntensity(rawIntensity: Int): Float = (rawIntensity / MAX_STEPS_PER_MINUTE).coerceIn(0f, 1f)

    override fun getAllActivitySamples(timestampFrom: Int, timestampTo: Int): MutableList<GenericActivitySample> {
        val byMinute = TreeMap<Long, GenericActivitySample>()
        fun sampleAt(seconds: Long): GenericActivitySample = byMinute.getOrPut(seconds / 60) {
            GenericActivitySample().also {
                it.provider = this
                it.timestamp = (seconds / 60 * 60).toInt()
            }
        }
        for (row in rows(timestampFrom, timestampTo)) {
            val s = sampleAt(row.timestamp.toLong())
            s.steps = row.steps
            s.distanceCm = row.distanceCm
            s.activeCalories = row.activeCalories
            s.rawIntensity = row.steps
            if (row.steps > 0) s.rawKind = toRawActivityKind(ActivityKind.ACTIVITY)
        }
        for (hr in heartRateProvider.getAllSamples(timestampFrom * 1000L, timestampTo * 1000L)) {
            sampleAt(hr.timestamp / 1000L).heartRate = hr.heartRate
        }
        // Sleep stages are runs of whole minutes. A run can start before the range, so look back and clip.
        for (stage in sleepStageProvider.getAllSamples((timestampFrom - SLEEP_LOOK_BACK_SECONDS) * 1000L, timestampTo * 1000L)) {
            val rawKind = toRawActivityKind(OnePlusSleepSessionProvider.stageToActivityKind(stage.stage))
            val start = stage.timestamp / 1000L
            for (minute in 0 until stage.duration) {
                val seconds = start + minute * 60L
                if (seconds < timestampFrom || seconds > timestampTo) continue
                sampleAt(seconds).rawKind = rawKind
            }
        }
        return ArrayList(byMinute.values)
    }

    override fun getAllActivitySamplesHighRes(timestampFrom: Int, timestampTo: Int): MutableList<GenericActivitySample> =
        getAllActivitySamples(timestampFrom, timestampTo)

    override fun hasHighResData(): Boolean = true

    override fun getActivitySamples(timestampFrom: Int, timestampTo: Int): MutableList<GenericActivitySample> =
        getAllActivitySamples(timestampFrom, timestampTo).filter { it.kind == ActivityKind.ACTIVITY }.toMutableList()

    override fun addGBActivitySample(activitySample: GenericActivitySample) {
        throw UnsupportedOperationException("Read-only sample provider")
    }

    override fun addGBActivitySamples(activitySamples: List<GenericActivitySample>) {
        throw UnsupportedOperationException("Read-only sample provider")
    }

    override fun createActivitySample(): GenericActivitySample = GenericActivitySample()

    override fun getLatestActivitySample(): GenericActivitySample? = latest(Int.MAX_VALUE)

    override fun getLatestActivitySample(until: Int): GenericActivitySample? = latest(until)

    override fun getFirstActivitySample(): GenericActivitySample? = first(0)

    override fun getFirstActivitySample(after: Int): GenericActivitySample? = first(after)

    private fun latest(until: Int): GenericActivitySample? {
        val deviceId = DBHelper.findDevice(device, session)?.id
        val rowTs = deviceId?.let {
            session.onePlusActivitySampleDao.queryBuilder()
                .where(OnePlusActivitySampleDao.Properties.DeviceId.eq(it), OnePlusActivitySampleDao.Properties.Timestamp.le(until))
                .orderDesc(OnePlusActivitySampleDao.Properties.Timestamp).limit(1).build().list().firstOrNull()?.timestamp
        }
        val hrTs = heartRateProvider.getLatestSample(until * 1000L)?.let { (it.timestamp / 1000L).toInt() }
        val ts = listOfNotNull(rowTs, hrTs).maxOrNull() ?: return null
        return getAllActivitySamples(ts - 59, minOf(ts + 59, until)).lastOrNull()
    }

    private fun first(after: Int): GenericActivitySample? {
        val deviceId = DBHelper.findDevice(device, session)?.id ?: return null
        val rowTs = session.onePlusActivitySampleDao.queryBuilder()
            .where(OnePlusActivitySampleDao.Properties.DeviceId.eq(deviceId), OnePlusActivitySampleDao.Properties.Timestamp.gt(after))
            .orderAsc(OnePlusActivitySampleDao.Properties.Timestamp).limit(1).build().list().firstOrNull()?.timestamp
        val hrTs = heartRateProvider.sampleDao.queryBuilder()
            .where(GenericHeartRateSampleDao.Properties.Timestamp.gt(after * 1000L), GenericHeartRateSampleDao.Properties.DeviceId.eq(deviceId))
            .orderAsc(GenericHeartRateSampleDao.Properties.Timestamp).limit(1).build().list().firstOrNull()
            ?.let { (it.timestamp / 1000L).toInt() }
        val ts = listOfNotNull(rowTs, hrTs).minOrNull() ?: return null
        return getAllActivitySamples(ts - 59, ts + 59).firstOrNull()
    }

    private fun rows(fromSeconds: Int, toSeconds: Int): List<OnePlusActivitySample> {
        val deviceId = DBHelper.findDevice(device, session)?.id ?: return emptyList()
        return session.onePlusActivitySampleDao.queryBuilder()
            .where(
                OnePlusActivitySampleDao.Properties.DeviceId.eq(deviceId),
                OnePlusActivitySampleDao.Properties.Timestamp.ge(fromSeconds),
                OnePlusActivitySampleDao.Properties.Timestamp.le(toSeconds)
            )
            .orderAsc(OnePlusActivitySampleDao.Properties.Timestamp)
            .build().list()
    }

    companion object {
        private const val MAX_STEPS_PER_MINUTE = 120f
        private const val SLEEP_LOOK_BACK_SECONDS = 6 * 60 * 60
    }
}
