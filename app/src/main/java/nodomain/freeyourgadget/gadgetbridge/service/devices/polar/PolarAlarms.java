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
package nodomain.freeyourgadget.gadgetbridge.service.devices.polar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.model.Alarm;

/**
 * The alarms of watches with the {@value #CAPABILITY} capability. They are not kept in a file but
 * behind the watch's REST service: commands are written as the path of an empty file, and the
 * watch reports its alarms in an "alarms_changed" event once subscribed.
 */
final class PolarAlarms {
    static final String CAPABILITY = "multiple_alarms";
    static final int MAX_ALARMS = 15;

    private static final String PATH = "/REST/ALARM.API";
    private static final String EVENT = "alarms_changed";
    private static final int REPEAT_ONCE = -1;
    private static final int REPEAT_EVERY_DAY = -2;

    /** One alarm as the watch knows it. */
    static final class WatchAlarm {
        final int id;
        final boolean on;
        final String time;
        final List<Integer> repeat;

        WatchAlarm(final int id, final boolean on, final String time, final List<Integer> repeat) {
            this.id = id;
            this.on = on;
            this.time = time;
            this.repeat = repeat;
        }
    }

    private PolarAlarms() {
    }

    static String subscribe() {
        return PATH + "?cmd=subscribe&event=" + EVENT + "&details=[ids,on,times,repeats]&resend=1";
    }

    /** The watch's alarms from a REST event, or null if the event is about something else. */
    @Nullable
    static List<WatchAlarm> parseEvent(final String json) {
        final JsonObject event = JsonParser.parseString(json).getAsJsonObject();
        if (!event.has(EVENT)) {
            return null;
        }
        final JsonObject alarms = event.getAsJsonObject(EVENT);
        final JsonArray ids = alarms.getAsJsonArray("ids");
        final JsonArray on = alarms.getAsJsonArray("on");
        final JsonArray times = alarms.getAsJsonArray("times");
        final JsonArray repeats = alarms.getAsJsonArray("repeats");
        final List<WatchAlarm> result = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            final JsonElement enabled = on.get(i);
            final List<Integer> repeat = new ArrayList<>();
            for (final JsonElement day : repeats.get(i).getAsJsonArray()) {
                repeat.add(day.getAsInt());
            }
            result.add(new WatchAlarm(ids.get(i).getAsInt(),
                    enabled.getAsJsonPrimitive().isBoolean() ? enabled.getAsBoolean() : enabled.getAsInt() != 0,
                    times.get(i).getAsString(), repeat));
        }
        return result;
    }

    /**
     * The commands that turn the watch's alarms into the given ones: existing alarms are edited in
     * order, the rest added or removed. Unused alarms are left out.
     */
    @NonNull
    static List<String> commands(final List<WatchAlarm> watch, final List<? extends Alarm> wanted) {
        final List<Alarm> used = new ArrayList<>();
        for (final Alarm alarm : wanted) {
            if (!alarm.getUnused() && used.size() < MAX_ALARMS) {
                used.add(alarm);
            }
        }
        final List<String> commands = new ArrayList<>();
        for (int i = 0; i < Math.max(watch.size(), used.size()); i++) {
            if (i >= used.size()) {
                commands.add(command("remove_alarm", "id=" + watch.get(i).id));
                continue;
            }
            final Alarm alarm = used.get(i);
            final String params = "on=" + (alarm.getEnabled() ? 1 : 0)
                    + ",time=" + String.format(Locale.ROOT, "%02d:%02d", alarm.getHour(), alarm.getMinute())
                    + ",repeat=" + repeat(alarm.getRepetition());
            if (i < watch.size()) {
                commands.add(command("edit_alarm", "id=" + watch.get(i).id + "," + params));
            } else {
                commands.add(command("add_alarm", params));
            }
        }
        return commands;
    }

    /**
     * Shows the watch's alarms in the app, the n-th alarm of the watch in the n-th slot and the
     * other slots unused, so that alarms set on the watch survive the next change made in the app.
     *
     * @return the slots that changed and need to be stored
     */
    @NonNull
    static List<nodomain.freeyourgadget.gadgetbridge.entities.Alarm> mirror(
            final List<WatchAlarm> watch, final List<nodomain.freeyourgadget.gadgetbridge.entities.Alarm> slots) {
        final List<nodomain.freeyourgadget.gadgetbridge.entities.Alarm> changed = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            final nodomain.freeyourgadget.gadgetbridge.entities.Alarm slot = slots.get(i);
            if (i >= watch.size()) {
                if (!slot.getUnused()) {
                    slot.setUnused(true);
                    changed.add(slot);
                }
                continue;
            }
            final WatchAlarm alarm = watch.get(i);
            final String[] time = alarm.time.split(":");
            if (time.length != 2) {
                continue;
            }
            final int hour = Integer.parseInt(time[0].trim());
            final int minute = Integer.parseInt(time[1].trim());
            final int repetition = repetition(alarm.repeat);
            if (slot.getUnused() || slot.getEnabled() != alarm.on || slot.getHour() != hour
                    || slot.getMinute() != minute || slot.getRepetition() != repetition) {
                slot.setUnused(false);
                slot.setEnabled(alarm.on);
                slot.setHour(hour);
                slot.setMinute(minute);
                slot.setRepetition(repetition);
                changed.add(slot);
            }
        }
        return changed;
    }

    private static int repetition(final List<Integer> repeat) {
        if (repeat.contains(REPEAT_EVERY_DAY)) {
            return Alarm.ALARM_DAILY;
        }
        int repetition = Alarm.ALARM_ONCE;
        for (final int day : repeat) {
            if (day >= 0 && day < 7) {
                repetition |= 1 << day;
            }
        }
        return repetition;
    }

    private static String command(final String endpoint, final String params) {
        return PATH + "?cmd=post&endpoint=" + endpoint + "&params=[" + params + "]";
    }

    /** Days as the watch counts them, Monday first (assumed: Polar Flow keeps no names for them). */
    private static String repeat(final int repetition) {
        if (repetition == Alarm.ALARM_ONCE) {
            return "[" + REPEAT_ONCE + "]";
        }
        if (repetition == Alarm.ALARM_DAILY) {
            return "[" + REPEAT_EVERY_DAY + "]";
        }
        final List<String> days = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            if ((repetition & (1 << day)) != 0) {
                days.add(Integer.toString(day));
            }
        }
        return "[" + String.join(",", days) + "]";
    }
}
