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
package nodomain.freeyourgadget.gadgetbridge.devices.garmin;

import static nodomain.freeyourgadget.gadgetbridge.model.ActivitySummaryEntries.*;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nodomain.freeyourgadget.gadgetbridge.activities.workouts.entries.ActivitySummaryTableBuilder;
import nodomain.freeyourgadget.gadgetbridge.activities.workouts.entries.ActivitySummaryValue;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySummaryData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.messages.FitSplit;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.messages.FitSplitSummary;

/**
 * Climbing / bouldering details from Garmin {@code split} and {@code split_summary} messages.
 * <p>
 * The watch writes one split of type climb_active per attempt, followed by a climb_rest split.
 * The FIT profile names the grade fields, but does not define their enums; the values below
 * were verified against a bouldering session from a Garmin watch:
 * <ul>
 *     <li>{@code climb_grading_scale} 9 = Fontainebleau</li>
 *     <li>{@code climb_grade_value} = index into {@link #FONTAINEBLEAU_GRADES}</li>
 *     <li>{@code status} 2 = attempted (not sent), 3 = completed (sent)</li>
 * </ul>
 * Rest statistics only use rests <em>between</em> two attempts: the rest after the last attempt
 * is usually just the time until the activity was stopped.
 */
public final class GarminClimbingSummary {
    public static final int SPLIT_TYPE_CLIMB_ACTIVE = 9;
    public static final int SPLIT_TYPE_CLIMB_REST = 10;

    public static final int GRADING_SCALE_FONTAINEBLEAU = 9;

    public static final int STATUS_ATTEMPTED = 2;
    public static final int STATUS_COMPLETED = 3;

    static final String[] FONTAINEBLEAU_GRADES = {
            "1", "2", "3", "4", "4+", "5", "5+",
            "6a", "6a+", "6b", "6b+", "6c", "6c+",
            "7a", "7a+", "7b", "7b+", "7c", "7c+",
            "8a", "8a+", "8b", "8b+", "8c", "8c+",
            "9a"
    };

    /** One attempt and the rest until the next attempt (null after the last attempt). */
    private record Attempt(FitSplit split, @Nullable Double restAfter) {
    }

    private GarminClimbingSummary() {
    }

    /**
     * @return the grade name for a known scale, {@code "#<value>"} for an unknown scale,
     * or null if there is no grade value
     */
    @Nullable
    public static String gradeName(@Nullable final Integer gradingScale, @Nullable final Number gradeValue) {
        if (gradeValue == null) {
            return null;
        }
        final int index = gradeValue.intValue();
        if (gradingScale != null && gradingScale == GRADING_SCALE_FONTAINEBLEAU
                && index >= 0 && index < FONTAINEBLEAU_GRADES.length) {
            return FONTAINEBLEAU_GRADES[index];
        }
        return "#" + index;
    }

    /**
     * @return true for a sent route/problem, false for an attempt, null if unknown
     */
    @Nullable
    public static Boolean isCompleted(final FitSplit split) {
        final Boolean send = split.getClimbSend();
        if (send != null) {
            return send;
        }
        final Integer status = split.getStatus();
        if (status == null) {
            return null;
        }
        return switch (status) {
            case STATUS_COMPLETED -> true;
            case STATUS_ATTEMPTED -> false;
            default -> null;
        };
    }

    private static boolean isType(final FitSplit split, final int type) {
        final Integer splitType = split.getSplitType();
        return splitType != null && splitType == type;
    }

    private static double elapsed(final FitSplit split) {
        final Double seconds = split.getTotalElapsedTime();
        return seconds != null ? seconds : 0;
    }

    /** Attempts in file order, each with the rest splits up to the next attempt. */
    private static List<Attempt> attempts(final List<FitSplit> splits) {
        final List<Attempt> attempts = new ArrayList<>();
        for (int i = 0; i < splits.size(); i++) {
            if (!isType(splits.get(i), SPLIT_TYPE_CLIMB_ACTIVE)) {
                continue;
            }
            double rest = 0;
            boolean hasNextAttempt = false;
            for (int j = i + 1; j < splits.size(); j++) {
                final FitSplit next = splits.get(j);
                if (isType(next, SPLIT_TYPE_CLIMB_ACTIVE)) {
                    hasNextAttempt = true;
                    break;
                }
                if (isType(next, SPLIT_TYPE_CLIMB_REST)) {
                    rest += elapsed(next);
                }
            }
            attempts.add(new Attempt(splits.get(i), hasNextAttempt ? rest : null));
        }
        return attempts;
    }

    @Nullable
    static Double median(final List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        final List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        final int mid = sorted.size() / 2;
        return sorted.size() % 2 == 1 ? sorted.get(mid) : (sorted.get(mid - 1) + sorted.get(mid)) / 2;
    }

    /**
     * Adds the climbing key figures, rest statistics, grade pyramid and list of attempts to the
     * summary. Does nothing if there are no climb splits.
     */
    public static void addToSummary(final List<FitSplit> splits,
                                    final List<FitSplitSummary> splitSummaries,
                                    final ActivitySummaryData summaryData) {
        final List<Attempt> attempts = attempts(splits);
        if (attempts.isEmpty()) {
            return;
        }

        addKeyFigures(attempts, splitSummaries, summaryData);
        addRestStatistics(attempts, summaryData);
        addGradePyramid(attempts, summaryData);
        addAttemptsTable(attempts, summaryData);
    }

    /** Overview tiles: sends, unsent attempts, hardest send, time on wall, typical rest. */
    private static void addKeyFigures(final List<Attempt> attempts,
                                      final List<FitSplitSummary> splitSummaries,
                                      final ActivitySummaryData summaryData) {
        // Prefer the totals the watch computed, fall back to counting the splits
        final FitSplitSummary summary = splitSummaries.stream()
                .filter(s -> s.getSplitType() != null && s.getSplitType() == SPLIT_TYPE_CLIMB_ACTIVE)
                .findFirst()
                .orElse(null);

        Integer sends = summary != null ? summary.getNumClimbsCompleted() : null;
        Integer unsent = summary != null ? summary.getNumClimbsAttempted() : null;
        String hardestSend = summary != null
                ? gradeName(summary.getClimbGradingScale(), summary.getClimbMaxGradeValue())
                : null;

        int countSends = 0;
        int countUnsent = 0;
        FitSplit hardest = null;
        double timeOnWall = 0;
        for (final Attempt attempt : attempts) {
            final FitSplit split = attempt.split();
            timeOnWall += elapsed(split);
            final Boolean done = isCompleted(split);
            if (Boolean.TRUE.equals(done)) {
                countSends++;
                if (split.getClimbGradeValue() != null
                        && (hardest == null || split.getClimbGradeValue() > hardest.getClimbGradeValue())) {
                    hardest = split;
                }
            } else if (Boolean.FALSE.equals(done)) {
                countUnsent++;
            }
        }
        if (sends == null) {
            sends = countSends;
        }
        if (unsent == null) {
            unsent = countUnsent;
        }
        if (hardestSend == null && hardest != null) {
            hardestSend = gradeName(hardest.getClimbGradingScale(), hardest.getClimbGradeValue());
        }

        final List<Double> rests = new ArrayList<>();
        for (final Attempt attempt : attempts) {
            if (attempt.restAfter() != null) {
                rests.add(attempt.restAfter());
            }
        }
        final Double medianRest = median(rests);

        // 0 sends / 0 unsent attempts is meaningful, so force those two to display
        summaryData.add(GROUP_CLIMBING, CLIMBS_COMPLETED, sends, UNIT_NONE, true);
        summaryData.add(GROUP_CLIMBING, CLIMBS_ATTEMPTED, unsent, UNIT_NONE, true);
        summaryData.add(GROUP_CLIMBING, CLIMB_MAX_GRADE, hardestSend);
        summaryData.add(GROUP_CLIMBING, CLIMB_TIME_ON_WALL, Math.round(timeOnWall), UNIT_SECONDS);
        summaryData.add(GROUP_CLIMBING, CLIMB_REST_MEDIAN, medianRest != null ? Math.round(medianRest) : null, UNIT_SECONDS);
    }

    /** Details: rest total/average, average attempt, share of time on the wall, falls. */
    private static void addRestStatistics(final List<Attempt> attempts, final ActivitySummaryData summaryData) {
        double timeOnWall = 0;
        double restTotal = 0;
        int restCount = 0;
        Integer falls = null;
        for (final Attempt attempt : attempts) {
            timeOnWall += elapsed(attempt.split());
            if (attempt.restAfter() != null) {
                restTotal += attempt.restAfter();
                restCount++;
            }
            final Integer attemptFalls = attempt.split().getNumFalls();
            if (attemptFalls != null) {
                falls = (falls != null ? falls : 0) + attemptFalls;
            }
        }

        summaryData.add(GROUP_CLIMBING_TIMES, CLIMB_AVG_DURATION, Math.round(timeOnWall / attempts.size()), UNIT_SECONDS);
        if (restCount > 0) {
            summaryData.add(GROUP_CLIMBING_TIMES, CLIMB_REST_TOTAL, Math.round(restTotal), UNIT_SECONDS);
            summaryData.add(GROUP_CLIMBING_TIMES, CLIMB_REST_AVG, Math.round(restTotal / restCount), UNIT_SECONDS);
            summaryData.add(GROUP_CLIMBING_TIMES, CLIMB_WALL_SHARE,
                    Math.round(100 * timeOnWall / (timeOnWall + restTotal)), UNIT_PERCENTAGE);
        }
        summaryData.add(GROUP_CLIMBING_TIMES, CLIMB_FALLS, falls, UNIT_NONE);
    }

    /** Table of grades, hardest first, with sends and unsent attempts per grade. */
    private static void addGradePyramid(final List<Attempt> attempts, final ActivitySummaryData summaryData) {
        // grade value -> {sends, unsent}; all attempts of a session use the same grading scale
        final Map<Long, int[]> counts = new TreeMap<>(Collections.reverseOrder());
        Integer scale = null;
        for (final Attempt attempt : attempts) {
            final FitSplit split = attempt.split();
            final Long grade = split.getClimbGradeValue();
            if (grade == null) {
                continue;
            }
            if (scale == null) {
                scale = split.getClimbGradingScale();
            }
            final int[] count = counts.computeIfAbsent(grade, g -> new int[2]);
            final Boolean done = isCompleted(split);
            if (Boolean.TRUE.equals(done)) {
                count[0]++;
            } else if (Boolean.FALSE.equals(done)) {
                count[1]++;
            }
        }
        if (counts.isEmpty()) {
            return;
        }

        final ActivitySummaryTableBuilder tableBuilder = new ActivitySummaryTableBuilder(CLIMB_GRADES, "climb_grades_header", Arrays.asList(
                "climb_grade",
                CLIMBS_COMPLETED,
                CLIMBS_ATTEMPTED
        ));
        for (final Map.Entry<Long, int[]> e : counts.entrySet()) {
            final int[] count = e.getValue();
            tableBuilder.addRow(
                    "climb_grade_" + e.getKey(),
                    Arrays.asList(
                            new ActivitySummaryValue(gradeName(scale, e.getKey()), UNIT_NONE),
                            // empty instead of 0, so the pyramid is easy to read
                            new ActivitySummaryValue(count[0] > 0 ? count[0] : null, UNIT_NONE),
                            new ActivitySummaryValue(count[1] > 0 ? count[1] : null, UNIT_NONE)
                    )
            );
        }
        tableBuilder.addToSummaryData(summaryData);
    }

    /** One row per attempt. */
    private static void addAttemptsTable(final List<Attempt> attempts, final ActivitySummaryData summaryData) {
        final ActivitySummaryTableBuilder tableBuilder = new ActivitySummaryTableBuilder(CLIMBS, "climbs_header", Arrays.asList(
                "climb",
                "climb_grade",
                "climb_result",
                "climb_falls",
                "activity_detail_duration_label",
                "climb_rest_after",
                HR_MAX
        ));

        int i = 1;
        for (final Attempt attempt : attempts) {
            final FitSplit split = attempt.split();
            final Boolean completed = isCompleted(split);
            final String result = completed == null ? null
                    : completed ? "climb_result_completed" : "climb_result_attempted";
            final Double duration = split.getTotalElapsedTime();

            tableBuilder.addRow(
                    "climb_" + i,
                    Arrays.asList(
                            new ActivitySummaryValue(i, UNIT_NONE),
                            new ActivitySummaryValue(gradeName(split.getClimbGradingScale(), split.getClimbGradeValue()), UNIT_NONE),
                            new ActivitySummaryValue(result, UNIT_NONE),
                            new ActivitySummaryValue(split.getNumFalls(), UNIT_NONE),
                            new ActivitySummaryValue(duration != null ? Math.round(duration) : null, UNIT_SECONDS),
                            new ActivitySummaryValue(attempt.restAfter() != null ? Math.round(attempt.restAfter()) : null, UNIT_SECONDS),
                            new ActivitySummaryValue(split.getMaxHeartRate(), UNIT_BPM)
                    )
            );
            i++;
        }

        tableBuilder.addToSummaryData(summaryData);
    }
}
