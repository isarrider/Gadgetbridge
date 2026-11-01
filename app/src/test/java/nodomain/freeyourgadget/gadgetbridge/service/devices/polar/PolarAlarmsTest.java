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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.entities.Alarm;

public class PolarAlarmsTest {
    private static Alarm alarm(final boolean enabled, final int repetition, final int hour, final int minute) {
        final Alarm alarm = new Alarm();
        alarm.setEnabled(enabled);
        alarm.setRepetition(repetition);
        alarm.setHour(hour);
        alarm.setMinute(minute);
        return alarm;
    }

    @Test
    public void eventListsTheWatchAlarms() {
        final List<PolarAlarms.WatchAlarm> alarms = PolarAlarms.parseEvent(
                "{\"alarms_changed\":{\"ids\":[3,7],\"on\":[1,false],\"times\":[\"06:45\",\"09:00\"],\"repeats\":[[-1],[0,4]]}}");

        assertEquals(2, alarms.size());
        assertEquals(3, alarms.get(0).id);
        assertTrue(alarms.get(0).on);
        assertEquals("06:45", alarms.get(0).time);
        assertEquals(Arrays.asList(0, 4), alarms.get(1).repeat);
        assertNull(PolarAlarms.parseEvent("{\"something_else\":{}}"));
    }

    @Test
    public void alarmsAreEditedAddedAndRemoved() {
        final List<PolarAlarms.WatchAlarm> watch = Arrays.asList(
                new PolarAlarms.WatchAlarm(3, true, "06:45", Collections.singletonList(-1)),
                new PolarAlarms.WatchAlarm(7, false, "09:00", Collections.singletonList(-2)));

        final List<String> edit = PolarAlarms.commands(watch, Collections.singletonList(
                alarm(true, nodomain.freeyourgadget.gadgetbridge.model.Alarm.ALARM_MON
                        | nodomain.freeyourgadget.gadgetbridge.model.Alarm.ALARM_FRI, 7, 5)));
        assertEquals(Arrays.asList(
                "/REST/ALARM.API?cmd=post&endpoint=edit_alarm&params=[id=3,on=1,time=07:05,repeat=[0,4]]",
                "/REST/ALARM.API?cmd=post&endpoint=remove_alarm&params=[id=7]"), edit);

        final List<String> add = PolarAlarms.commands(Collections.emptyList(), Arrays.asList(
                alarm(false, nodomain.freeyourgadget.gadgetbridge.model.Alarm.ALARM_ONCE, 6, 0),
                alarm(true, nodomain.freeyourgadget.gadgetbridge.model.Alarm.ALARM_DAILY, 22, 30)));
        assertEquals(Arrays.asList(
                "/REST/ALARM.API?cmd=post&endpoint=add_alarm&params=[on=0,time=06:00,repeat=[-1]]",
                "/REST/ALARM.API?cmd=post&endpoint=add_alarm&params=[on=1,time=22:30,repeat=[-2]]"), add);
    }

    @Test
    public void watchAlarmsFillTheSlotsInOrder() {
        final List<Alarm> slots = Arrays.asList(alarm(true, 0, 7, 30), alarm(false, 0, 0, 0), alarm(true, 0, 8, 0));
        slots.get(1).setUnused(true);

        final List<Alarm> changed = PolarAlarms.mirror(Arrays.asList(
                new PolarAlarms.WatchAlarm(0, true, "7:30", Collections.singletonList(-1)),
                new PolarAlarms.WatchAlarm(1, true, "6:00", Arrays.asList(0, 4))), slots);

        // the first slot already matches, the second gets the alarm set on the watch, the third is gone
        assertEquals(Arrays.asList(slots.get(1), slots.get(2)), changed);
        assertEquals(6, slots.get(1).getHour());
        assertEquals(nodomain.freeyourgadget.gadgetbridge.model.Alarm.ALARM_MON
                | nodomain.freeyourgadget.gadgetbridge.model.Alarm.ALARM_FRI, slots.get(1).getRepetition());
        assertTrue(slots.get(2).getUnused());
    }
}
