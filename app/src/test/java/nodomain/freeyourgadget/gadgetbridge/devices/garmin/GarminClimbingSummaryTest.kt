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
package nodomain.freeyourgadget.gadgetbridge.devices.garmin

import nodomain.freeyourgadget.gadgetbridge.activities.workouts.entries.ActivitySummarySimpleEntry
import nodomain.freeyourgadget.gadgetbridge.activities.workouts.entries.ActivitySummaryTableRowEntry
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySummaryData
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySummaryEntries
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.messages.FitSplit
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.messages.FitSplitSummary
import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GarminClimbingSummaryTest : TestBase() {

    private fun climb(grade: Long, status: Int, seconds: Double): FitSplit = FitSplit.Builder()
        .setSplitType(GarminClimbingSummary.SPLIT_TYPE_CLIMB_ACTIVE)
        .setTotalElapsedTime(seconds)
        .setClimbGradingScale(GarminClimbingSummary.GRADING_SCALE_FONTAINEBLEAU)
        .setClimbGradeValue(grade)
        .setStatus(status)
        .setAvgHeartRate(90)
        .setMaxHeartRate(110)
        .build()

    private fun rest(seconds: Double): FitSplit = FitSplit.Builder()
        .setSplitType(GarminClimbingSummary.SPLIT_TYPE_CLIMB_REST)
        .setTotalElapsedTime(seconds)
        .build()

    private fun ActivitySummaryData.row(key: String) =
        (get(key) as ActivitySummaryTableRowEntry).columns.map { it.value() }

    private fun ActivitySummaryData.simple(key: String) =
        (get(key) as ActivitySummarySimpleEntry).value

    private fun ActivitySummaryData.int(key: String) = getNumber(key, -1).toInt()

    /** 5+ sent (38 s), rest 27 s, 7a not sent (24 s), rest 600 s until the activity is stopped. */
    private fun twoAttempts(): ActivitySummaryData {
        val data = ActivitySummaryData()
        GarminClimbingSummary.addToSummary(
            listOf(climb(6, 3, 38.0), rest(27.0), climb(13, 2, 24.0), rest(600.0)),
            emptyList(),
            data
        )
        return data
    }

    @Test
    fun fontainebleauGrades() {
        assertEquals("1", GarminClimbingSummary.gradeName(9, 0))
        assertEquals("5+", GarminClimbingSummary.gradeName(9, 6))
        assertEquals("6a+", GarminClimbingSummary.gradeName(9, 8))
        assertEquals("7a", GarminClimbingSummary.gradeName(9, 13))
        assertEquals("9a", GarminClimbingSummary.gradeName(9, 25))
        assertEquals("#26", GarminClimbingSummary.gradeName(9, 26))
        assertEquals("#6", GarminClimbingSummary.gradeName(1, 6))
        assertNull(GarminClimbingSummary.gradeName(9, null))
    }

    @Test
    fun median() {
        assertNull(GarminClimbingSummary.median(emptyList()))
        assertEquals(2.0, GarminClimbingSummary.median(listOf(3.0, 1.0, 2.0))!!, 0.0)
        assertEquals(2.5, GarminClimbingSummary.median(listOf(4.0, 1.0, 3.0, 2.0))!!, 0.0)
    }

    @Test
    fun keyFigures_countedFromSplits_trailingRestIgnored() {
        val data = twoAttempts()

        assertEquals(1, data.int(ActivitySummaryEntries.CLIMBS_COMPLETED))
        assertEquals(1, data.int(ActivitySummaryEntries.CLIMBS_ATTEMPTED))
        assertEquals("5+", data.simple(ActivitySummaryEntries.CLIMB_MAX_GRADE))
        assertEquals(62, data.int(ActivitySummaryEntries.CLIMB_TIME_ON_WALL))
        // only the rest between the two attempts counts, not the 600 s at the end
        assertEquals(27, data.int(ActivitySummaryEntries.CLIMB_REST_MEDIAN))
    }

    @Test
    fun restStatistics() {
        val data = twoAttempts()

        assertEquals(31, data.int(ActivitySummaryEntries.CLIMB_AVG_DURATION))
        assertEquals(27, data.int(ActivitySummaryEntries.CLIMB_REST_TOTAL))
        assertEquals(27, data.int(ActivitySummaryEntries.CLIMB_REST_AVG))
        assertEquals(70, data.int(ActivitySummaryEntries.CLIMB_WALL_SHARE)) // 62 / (62 + 27)
        assertFalse(data.has(ActivitySummaryEntries.CLIMB_FALLS))
    }

    @Test
    fun gradePyramid_hardestFirst_emptyInsteadOfZero() {
        val data = twoAttempts()

        val rowKeys = data.keys.filter { it.startsWith("climb_grade_") }
        assertEquals(listOf("climb_grade_13", "climb_grade_6"), rowKeys)
        assertEquals(listOf("7a", null, 1), data.row("climb_grade_13"))
        assertEquals(listOf("5+", 1, null), data.row("climb_grade_6"))
    }

    @Test
    fun attemptsTable_restsAreNotRows() {
        val data = twoAttempts()

        assertTrue(data.has("climbs_header"))
        assertFalse(data.has("climb_3"))
        // falls column is dropped because no attempt has falls:
        // #, grade, result, duration, rest after, max HR
        assertEquals(listOf(1, "5+", "climb_result_completed", 38L, 27L, 110), data.row("climb_1"))
        assertEquals(listOf(2, "7a", "climb_result_attempted", 24L, null, 110), data.row("climb_2"))
    }

    @Test
    fun keyFigures_comeFromTheSplitSummary() {
        val summary = FitSplitSummary.Builder()
            .setSplitType(GarminClimbingSummary.SPLIT_TYPE_CLIMB_ACTIVE)
            .setClimbGradingScale(GarminClimbingSummary.GRADING_SCALE_FONTAINEBLEAU)
            .setClimbMaxGradeValue(11L)
            .setNumClimbsAttempted(4)
            .setNumClimbsCompleted(13)
            .build()

        val data = ActivitySummaryData()
        GarminClimbingSummary.addToSummary(listOf(climb(6, 3, 38.0)), listOf(summary), data)

        assertEquals(13, data.int(ActivitySummaryEntries.CLIMBS_COMPLETED))
        assertEquals(4, data.int(ActivitySummaryEntries.CLIMBS_ATTEMPTED))
        assertEquals("6c", data.simple(ActivitySummaryEntries.CLIMB_MAX_GRADE))
        // a single attempt has no rest between attempts
        assertFalse(data.has(ActivitySummaryEntries.CLIMB_REST_MEDIAN))
        assertFalse(data.has(ActivitySummaryEntries.CLIMB_REST_TOTAL))
    }

    @Test
    fun noClimbSplits_addsNothing() {
        val interval = FitSplit.Builder().setSplitType(3).setTotalElapsedTime(60.0).build()
        val data = ActivitySummaryData()
        GarminClimbingSummary.addToSummary(listOf(interval), emptyList(), data)

        assertTrue(data.keys.isEmpty())
    }
}
