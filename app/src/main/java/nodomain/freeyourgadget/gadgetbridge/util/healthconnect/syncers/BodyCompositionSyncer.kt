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
package nodomain.freeyourgadget.gadgetbridge.util.healthconnect.syncers

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import androidx.health.connect.client.units.Power
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.util.BodyCompositionCalculator
import nodomain.freeyourgadget.gadgetbridge.util.BodyCompositionEstimates
import nodomain.freeyourgadget.gadgetbridge.util.healthconnect.HealthConnectUtils
import nodomain.freeyourgadget.gadgetbridge.util.healthconnect.SyncException
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId

private val LOG = LoggerFactory.getLogger("BodyCompositionSyncer")

private const val RECORD_TYPE = "BodyComposition"

/** Plausibility window for the body fat percentage, in percent. */
private const val MIN_BODY_FAT_PERCENT = 1.0
private const val MAX_BODY_FAT_PERCENT = 70.0

/** Plausibility windows for the derived masses, in kilograms. */
private const val MIN_BODY_WATER_MASS_KG = 1.0
private const val MAX_BODY_WATER_MASS_KG = 200.0
private const val MIN_BONE_MASS_KG = 0.1
private const val MAX_BONE_MASS_KG = 20.0
private const val MIN_LEAN_BODY_MASS_KG = 5.0
private const val MAX_LEAN_BODY_MASS_KG = 300.0

/** Plausibility window for the derived basal metabolic rate, in kcal/day. */
private const val MIN_BMR_KCAL = 500.0
private const val MAX_BMR_KCAL = 6000.0

/**
 * Syncs the body composition that [BodyCompositionCalculator] derives from the raw impedance a
 * bio-impedance scale stores on its weight samples.
 *
 * Gadgetbridge deliberately persists no derived body composition - only the raw impedance on the
 * weight sample - and computes the estimates when they are displayed. Health Connect has no
 * impedance record at all, so the derived values have to be built here, at sync time, from the
 * raw sample and the user profile as it was on the day of the measurement.
 *
 * One measurement produces up to five records, of different types, which is why this is not an
 * [AbstractTimeSampleSyncer]: that base class maps one sample to one record. Muscle mass is
 * intentionally not exported - Health Connect has no muscle mass record, and lean body mass is
 * not a substitute, as it also covers bone, water and organ mass.
 */
internal object BodyCompositionSyncer : HealthConnectSyncer {

    private data class EstimableSample(
        val timestamp: Long,
        val weightKg: Float,
        val composition: BodyCompositionCalculator.BodyComposition
    )

    override suspend fun sync(
        healthConnectClient: HealthConnectClient,
        gbDevice: GBDevice,
        metadata: Metadata,
        offset: ZoneId,
        sliceStartBoundary: Instant,
        sliceEndBoundary: Instant,
        grantedPermissions: Set<String>
    ): SyncerStatistics {
        val deviceName = gbDevice.aliasOrName

        val canWriteBodyFat = HealthPermission.getWritePermission(BodyFatRecord::class) in grantedPermissions
        val canWriteBodyWater = HealthPermission.getWritePermission(BodyWaterMassRecord::class) in grantedPermissions
        val canWriteBoneMass = HealthPermission.getWritePermission(BoneMassRecord::class) in grantedPermissions
        val canWriteLeanBodyMass = HealthPermission.getWritePermission(LeanBodyMassRecord::class) in grantedPermissions
        val canWriteBmr = HealthPermission.getWritePermission(BasalMetabolicRateRecord::class) in grantedPermissions

        if (!canWriteBodyFat && !canWriteBodyWater && !canWriteBoneMass && !canWriteLeanBodyMass && !canWriteBmr) {
            LOG.info("Skipping BodyComposition sync for device '$deviceName'; no body composition permission granted.")
            return SyncerStatistics(recordType = RECORD_TYPE)
        }

        // The estimate needs the profile of the measurement's day, so it has to be computed while
        // the database session is open - not from the plain sample list afterwards.
        val samples: List<EstimableSample> = try {
            GBApplication.acquireDbReadOnly().use { db ->
                val provider = gbDevice.deviceCoordinator.getWeightSampleProvider(gbDevice, db.daoSession)
                if (provider == null) {
                    LOG.info("Weight sample provider not available for device '$deviceName'. Skipping BodyComposition sync.")
                    emptyList()
                } else {
                    provider.getAllSamples(sliceStartBoundary.toEpochMilli(), sliceEndBoundary.toEpochMilli())
                        .mapNotNull { sample ->
                            val composition = BodyCompositionEstimates.composition(db.daoSession, sample)
                            if (composition == null) {
                                // No impedance on this sample - most scales never report one.
                                null
                            } else {
                                EstimableSample(sample.timestamp, sample.weightKg, composition)
                            }
                        }
                }
            }
        } catch (e: Exception) {
            throw SyncException(
                "Error fetching weight samples with impedance for device '$deviceName' " +
                        "for slice $sliceStartBoundary to $sliceEndBoundary.",
                e
            )
        }

        if (samples.isEmpty()) {
            LOG.info("No weight samples with impedance for device '$deviceName' in slice $sliceStartBoundary to $sliceEndBoundary.")
            return SyncerStatistics(recordType = RECORD_TYPE)
        }

        val recordsToInsert = mutableListOf<Record>()
        var skippedCount = 0
        var latestSyncedTimestamp: Instant? = null

        for (sample in samples) {
            val timestamp = Instant.ofEpochMilli(sample.timestamp)
            if (timestamp.isBefore(sliceStartBoundary) || timestamp.isAfter(sliceEndBoundary)) {
                LOG.trace("Skipping body composition sample at {} as it's outside the slice {} - {}.", timestamp, sliceStartBoundary, sliceEndBoundary)
                continue
            }

            val weightKg = sample.weightKg.toDouble()
            if (!weightKg.isFinite() || weightKg <= 0.0) {
                LOG.skipOutOfRange(deviceName, "BodyCompositionWeight", weightKg, ">0 kg")
                skippedCount++
                continue
            }

            val composition = sample.composition
            val zoneOffset = offset.rules.getOffset(timestamp)
            val bodyFatPercent = composition.bodyFatPercent.toDouble()
            val fatMassKg = weightKg * bodyFatPercent / 100.0
            val leanBodyMassKg = weightKg - fatMassKg
            val bodyWaterMassKg = weightKg * composition.bodyWaterPercent / 100.0
            val boneMassKg = composition.boneMassKg.toDouble()
            val bmrKcal = composition.basalMetabolicRate.toDouble()

            var produced = false

            if (canWriteBodyFat) {
                if (bodyFatPercent in MIN_BODY_FAT_PERCENT..MAX_BODY_FAT_PERCENT) {
                    recordsToInsert.add(
                        BodyFatRecord(
                            time = timestamp,
                            zoneOffset = zoneOffset,
                            percentage = Percentage(bodyFatPercent),
                            metadata = metadata
                        )
                    )
                    produced = true
                } else {
                    LOG.skipOutOfRange(deviceName, "BodyFat", bodyFatPercent, "$MIN_BODY_FAT_PERCENT..$MAX_BODY_FAT_PERCENT %")
                    skippedCount++
                }
            }

            if (canWriteBodyWater) {
                if (bodyWaterMassKg in MIN_BODY_WATER_MASS_KG..MAX_BODY_WATER_MASS_KG) {
                    recordsToInsert.add(
                        BodyWaterMassRecord(
                            time = timestamp,
                            zoneOffset = zoneOffset,
                            mass = Mass.kilograms(bodyWaterMassKg),
                            metadata = metadata
                        )
                    )
                    produced = true
                } else {
                    LOG.skipOutOfRange(deviceName, "BodyWaterMass", bodyWaterMassKg, "$MIN_BODY_WATER_MASS_KG..$MAX_BODY_WATER_MASS_KG kg")
                    skippedCount++
                }
            }

            if (canWriteBoneMass) {
                if (boneMassKg in MIN_BONE_MASS_KG..MAX_BONE_MASS_KG) {
                    recordsToInsert.add(
                        BoneMassRecord(
                            time = timestamp,
                            zoneOffset = zoneOffset,
                            mass = Mass.kilograms(boneMassKg),
                            metadata = metadata
                        )
                    )
                    produced = true
                } else {
                    LOG.skipOutOfRange(deviceName, "BoneMass", boneMassKg, "$MIN_BONE_MASS_KG..$MAX_BONE_MASS_KG kg")
                    skippedCount++
                }
            }

            if (canWriteLeanBodyMass) {
                if (leanBodyMassKg in MIN_LEAN_BODY_MASS_KG..MAX_LEAN_BODY_MASS_KG) {
                    recordsToInsert.add(
                        LeanBodyMassRecord(
                            time = timestamp,
                            zoneOffset = zoneOffset,
                            mass = Mass.kilograms(leanBodyMassKg),
                            metadata = metadata
                        )
                    )
                    produced = true
                } else {
                    LOG.skipOutOfRange(deviceName, "LeanBodyMass", leanBodyMassKg, "$MIN_LEAN_BODY_MASS_KG..$MAX_LEAN_BODY_MASS_KG kg")
                    skippedCount++
                }
            }

            if (canWriteBmr) {
                if (bmrKcal in MIN_BMR_KCAL..MAX_BMR_KCAL) {
                    recordsToInsert.add(
                        BasalMetabolicRateRecord(
                            time = timestamp,
                            zoneOffset = zoneOffset,
                            basalMetabolicRate = Power.kilocaloriesPerDay(bmrKcal),
                            metadata = metadata
                        )
                    )
                    produced = true
                } else {
                    LOG.skipOutOfRange(deviceName, "BasalMetabolicRate", bmrKcal, "$MIN_BMR_KCAL..$MAX_BMR_KCAL kcal/day")
                    skippedCount++
                }
            }

            if (produced && (latestSyncedTimestamp == null || timestamp.isAfter(latestSyncedTimestamp))) {
                latestSyncedTimestamp = timestamp
            }
        }

        if (recordsToInsert.isEmpty()) {
            LOG.info("No body composition records to insert for device '$deviceName' in slice $sliceStartBoundary to $sliceEndBoundary.")
            return SyncerStatistics(recordsSkipped = skippedCount, recordType = RECORD_TYPE)
        }

        LOG.info(
            "Inserting {} body composition record(s) derived from {} measurement(s) with impedance for device '{}' in slice {} to {}.",
            recordsToInsert.size,
            samples.size,
            deviceName,
            sliceStartBoundary,
            sliceEndBoundary
        )
        HealthConnectUtils.insertRecords(recordsToInsert, healthConnectClient)

        return SyncerStatistics(
            recordsSynced = recordsToInsert.size,
            recordsSkipped = skippedCount,
            recordType = RECORD_TYPE,
            latestRecordTimestamp = latestSyncedTimestamp
        )
    }
}
