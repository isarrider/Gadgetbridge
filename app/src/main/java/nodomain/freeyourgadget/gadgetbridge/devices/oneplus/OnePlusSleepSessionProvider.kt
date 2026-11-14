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

import nodomain.freeyourgadget.gadgetbridge.devices.GenericSleepStageSampleProvider
import nodomain.freeyourgadget.gadgetbridge.devices.SleepSessionProvider
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind
import nodomain.freeyourgadget.gadgetbridge.model.SleepSession
import nodomain.freeyourgadget.gadgetbridge.model.SleepStage

/** Builds sleep sessions from the per-minute sleep stages synced from the OnePlus Watch. */
class OnePlusSleepSessionProvider(
    private val device: GBDevice,
    private val session: DaoSession
) : SleepSessionProvider {
    override fun getSleepSessions(tsFrom: Int, tsTo: Int): List<SleepSession> {
        val fromMillis = (tsFrom - LOOK_BACK_SECONDS) * 1000L
        val toMillis = (tsTo + LOOK_AHEAD_SECONDS) * 1000L
        val stages = GenericSleepStageSampleProvider(device, session).getAllSamples(fromMillis, toMillis).map {
            SleepStage(stageToActivityKind(it.stage), it.timestamp, it.timestamp + it.duration * 60 * 1000L)
        }
        return SleepSessionProvider.groupIntoSessions(stages)
            .filter { (it.startTime / 1000L) in tsFrom..<tsTo }
            .sortedBy { it.startTime }
    }

    companion object {
        private const val LOOK_BACK_SECONDS: Int = 12 * 60 * 60
        private const val LOOK_AHEAD_SECONDS: Int = 12 * 60 * 60

        /**
         * Watch sleep-state codes. 1 = light and 2 = deep are confirmed against real nights. 4 has been seen once, as a
         * 4-minute run between light sleep, and is taken as awake (unconfirmed). REM has never been seen, so it is not mapped.
         */
        fun stageToActivityKind(stage: Int): ActivityKind = when (stage) {
            1 -> ActivityKind.LIGHT_SLEEP
            2 -> ActivityKind.DEEP_SLEEP
            4 -> ActivityKind.AWAKE_SLEEP
            else -> ActivityKind.UNKNOWN
        }
    }
}
