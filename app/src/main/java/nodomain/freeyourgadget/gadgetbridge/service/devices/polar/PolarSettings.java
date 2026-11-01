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

import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_ALWAYS;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_END;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_OFF;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_SCHEDULED;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_START;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_INACTIVITY_ENABLE_NOSHED;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_TIMEFORMAT_12H;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_VIBRATION_ENABLE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_WEARLOCATION;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityUser;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.DistanceUnit;
import nodomain.freeyourgadget.gadgetbridge.proto.polar.PolarProto;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import nodomain.freeyourgadget.gadgetbridge.util.preferences.DevicePrefs;

/**
 * Maps Gadgetbridge preferences to the watch's settings files and back.
 *
 * <ul>
 *   <li>{@code /U/0/S/UDEVSET.BPB} (PbUserDeviceSettings): watch settings. The watch is the
 *   source of truth: they are imported into the preferences on connect, and preference changes
 *   are written back.</li>
 *   <li>{@code /U/0/S/PREFS.BPB} (PbGeneralPreferences): time format and units, taken from
 *   Gadgetbridge.</li>
 *   <li>{@code /U/0/S/PHYSDATA.BPB} (PbUserPhysData): body data from the Gadgetbridge user
 *   profile, which the watch's calorie and training algorithms rely on. Only values the user
 *   actually entered are written, the profile defaults would overwrite real data.</li>
 * </ul>
 * The apply methods return null when the file would not change, so nothing is written needlessly.
 */
final class PolarSettings {
    static final String PREF_WATCH_NOTIFICATIONS = "polar_watch_notifications";
    static final String PREF_WATCH_NOTIFICATION_PREVIEW = "polar_watch_notification_preview";
    static final String PREF_WATCH_NOTIFICATIONS_TRAINING = "polar_watch_notifications_training";
    static final String PREF_CONTINUOUS_HR = "polar_continuous_hr";
    private static final String CONTINUOUS_HR_OFF = "off";
    private static final String CONTINUOUS_HR_ALWAYS = "always";
    private static final String CONTINUOUS_HR_NIGHT = "night";
    static final String PREF_TAP_SENSITIVITY = "polar_tap_sensitivity";
    static final String PREF_AUTO_BUTTON_LOCK = "polar_auto_button_lock";
    static final String PREF_MEDIA_CONTROLS = "polar_media_controls";
    static final String PREF_SATELLITES = "polar_satellites";
    static final String PREF_DATE_FORMAT = "polar_date_format";
    static final String PREF_DATE_SEPARATOR = "polar_date_separator";
    static final String PREF_FIRST_DAY_OF_WEEK = "polar_first_day_of_week";
    /** GPS combined with one other system; SBAS is kept as the watch has it */
    private static final String SATELLITES_GLONASS = "gps_glonass";
    private static final String SATELLITES_GALILEO = "gps_galileo";
    private static final String SATELLITES_BEIDOU = "gps_beidou";
    private static final String SATELLITES_QZSS = "gps_qzss";
    private static final String WEAR_RIGHT = "right";

    /** Preference keys stored in UDEVSET.BPB */
    static final Set<String> DEVICE_SETTINGS_KEYS = new HashSet<>(Arrays.asList(
            PREF_WATCH_NOTIFICATIONS,
            PREF_WATCH_NOTIFICATION_PREVIEW,
            PREF_WATCH_NOTIFICATIONS_TRAINING,
            PREF_DO_NOT_DISTURB,
            PREF_DO_NOT_DISTURB_START,
            PREF_DO_NOT_DISTURB_END,
            PREF_CONTINUOUS_HR,
            PREF_INACTIVITY_ENABLE_NOSHED,
            PREF_WEARLOCATION,
            PREF_VIBRATION_ENABLE,
            PREF_TAP_SENSITIVITY,
            PREF_AUTO_BUTTON_LOCK,
            PREF_MEDIA_CONTROLS,
            PREF_SATELLITES
    ));

    /** Preference keys stored in PREFS.BPB */
    static final Set<String> GENERAL_PREFERENCES_KEYS = new HashSet<>(Arrays.asList(
            DeviceSettingsPreferenceConst.PREF_TIMEFORMAT,
            PREF_DATE_FORMAT,
            PREF_DATE_SEPARATOR,
            PREF_FIRST_DAY_OF_WEEK
    ));

    private PolarSettings() {
    }

    static void importDeviceSettings(final PolarProto.PbUserDeviceSettings settings, final SharedPreferences.Editor editor) {
        final PolarProto.PbUserSmartWatchNotificationSettings notifications = settings.getSmartWatchNotificationSettings();
        editor.putBoolean(PREF_WATCH_NOTIFICATIONS, notifications.getEnabled());
        editor.putBoolean(PREF_WATCH_NOTIFICATION_PREVIEW, notifications.getPreviewEnabled());
        editor.putBoolean(PREF_WATCH_NOTIFICATIONS_TRAINING, notifications.getOnDuringTraining());

        final PolarProto.PbUserDeviceDoNotDisturbSettings_v2 dnd = settings.getDoNotDisturbSettingsV2();
        switch (dnd.getState()) {
            case ON:
                editor.putString(PREF_DO_NOT_DISTURB, PREF_DO_NOT_DISTURB_ALWAYS);
                break;
            case TIMED:
                editor.putString(PREF_DO_NOT_DISTURB, PREF_DO_NOT_DISTURB_SCHEDULED);
                break;
            default:
                editor.putString(PREF_DO_NOT_DISTURB, PREF_DO_NOT_DISTURB_OFF);
        }
        if (dnd.hasTimedSettings()) {
            editor.putString(PREF_DO_NOT_DISTURB_START, timeString(dnd.getTimedSettings().getStart()));
            editor.putString(PREF_DO_NOT_DISTURB_END, timeString(dnd.getTimedSettings().getEnd()));
        }

        final PolarProto.PbAutomaticMeasurementSettings hr = settings.getAutomaticMeasurementSettings().getAutomaticOhrMeasurement();
        switch (hr.getState()) {
            case OFF:
                editor.putString(PREF_CONTINUOUS_HR, CONTINUOUS_HR_OFF);
                break;
            case INTELLIGENT_TIMED:
            case TIMED:
                editor.putString(PREF_CONTINUOUS_HR, CONTINUOUS_HR_NIGHT);
                break;
            default:
                editor.putString(PREF_CONTINUOUS_HR, CONTINUOUS_HR_ALWAYS);
        }

        final PolarProto.PbUserDeviceGeneralSettings general = settings.getGeneralSettings();
        editor.putBoolean(PREF_INACTIVITY_ENABLE_NOSHED, general.getInactivityAlert()
                == PolarProto.PbUserDeviceGeneralSettings.PbInactivityAlert.INACTIVITY_ALERT_ON);
        editor.putString(PREF_WEARLOCATION, general.getDeviceLocation() == PolarProto.PbDeviceLocation.DEVICE_LOCATION_WRIST_RIGHT
                ? WEAR_RIGHT : "left");
        editor.putBoolean(PREF_VIBRATION_ENABLE, general.getVibrationFeedbackMode()
                == PolarProto.PbUserDeviceGeneralSettings.PbVibrationFeedbackMode.VIBRATION_FEEDBACK_ENABLED);
        if (general.hasTapButtonSensitivity()) {
            editor.putString(PREF_TAP_SENSITIVITY, general.getTapButtonSensitivity().name());
        }
        editor.putBoolean(PREF_AUTO_BUTTON_LOCK, general.getButtonLockMode()
                == PolarProto.PbUserDeviceGeneralSettings.PbButtonLockMode.AUTO);
        if (settings.hasMediaControlSettings()) {
            editor.putString(PREF_MEDIA_CONTROLS, settings.getMediaControlSettings().getMode().name());
        }
        if (settings.getGnssSettings().hasSatelliteSystemSettings()) {
            final PolarProto.PbSatelliteSystemSettings satellites = settings.getGnssSettings().getSatelliteSystemSettings();
            final String value = satellites.getGalileoEnabled() ? SATELLITES_GALILEO
                    : satellites.getBeidouEnabled() ? SATELLITES_BEIDOU
                    : satellites.getQzssEnabled() ? SATELLITES_QZSS
                    : SATELLITES_GLONASS;
            editor.putString(PREF_SATELLITES, value);
        }
    }

    static void importGeneralPreferences(final PolarProto.PbGeneralPreferences preferences, final SharedPreferences.Editor editor) {
        final PolarProto.PbLocalizationPreferences localization = preferences.getLocalizationPreferences();
        if (localization.hasDateFormat()) {
            editor.putString(PREF_DATE_FORMAT, localization.getDateFormat().name());
        }
        if (localization.hasDateFormatSeparator()) {
            editor.putString(PREF_DATE_SEPARATOR, localization.getDateFormatSeparator().name());
        }
        if (localization.hasFirstdayOfWeek()) {
            editor.putString(PREF_FIRST_DAY_OF_WEEK, localization.getFirstdayOfWeek().name());
        }
    }

    /** The enum constant named by a preference, null if unset or unknown so the watch's value stays */
    @Nullable
    private static <E extends Enum<E>> E enumPref(final DevicePrefs prefs, final String key, final Class<E> type) {
        final String value = prefs.getString(key, null);
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    static PolarProto.PbUserDeviceSettings applyDeviceSettings(final PolarProto.PbUserDeviceSettings current, final DevicePrefs prefs) {
        final PolarProto.PbUserDeviceSettings.Builder builder = current.toBuilder();

        final boolean notificationsEnabled = prefs.getBoolean(PREF_WATCH_NOTIFICATIONS, true);
        final String dndMode = prefs.getString(PREF_DO_NOT_DISTURB, PREF_DO_NOT_DISTURB_OFF);
        final PolarProto.PbTime dndStart = pbTime(prefs.getLocalTime(PREF_DO_NOT_DISTURB_START, "22:00"));
        final PolarProto.PbTime dndEnd = pbTime(prefs.getLocalTime(PREF_DO_NOT_DISTURB_END, "07:00"));

        final PolarProto.PbUserSmartWatchNotificationSettings.Builder notifications = builder.getSmartWatchNotificationSettings().toBuilder()
                .setEnabled(notificationsEnabled)
                .setPreviewEnabled(prefs.getBoolean(PREF_WATCH_NOTIFICATION_PREVIEW, true))
                .setOnDuringTraining(notificationsEnabled && prefs.getBoolean(PREF_WATCH_NOTIFICATIONS_TRAINING, true))
                .setOnWhenNotTraining(notificationsEnabled);
        // The do_not_disturb_settings in here are left alone: Polar Flow keeps them independent of
        // the do_not_disturb_settings_v2 the watch actually uses
        builder.setSmartWatchNotificationSettings(notifications);

        final PolarProto.PbUserDeviceDoNotDisturbSettings_v2.PbDoNotDisturbState dndState;
        switch (dndMode) {
            case PREF_DO_NOT_DISTURB_ALWAYS:
                dndState = PolarProto.PbUserDeviceDoNotDisturbSettings_v2.PbDoNotDisturbState.ON;
                break;
            case PREF_DO_NOT_DISTURB_SCHEDULED:
                dndState = PolarProto.PbUserDeviceDoNotDisturbSettings_v2.PbDoNotDisturbState.TIMED;
                break;
            default:
                dndState = PolarProto.PbUserDeviceDoNotDisturbSettings_v2.PbDoNotDisturbState.OFF;
        }
        builder.setDoNotDisturbSettingsV2(builder.getDoNotDisturbSettingsV2().toBuilder()
                .setState(dndState)
                .setTimedSettings(PolarProto.PbTimedDoNotDisturbSettings.newBuilder().setStart(dndStart).setEnd(dndEnd)));

        final PolarProto.PbAutomaticMeasurementSettings.PbAutomaticMeasurementState hrState;
        switch (prefs.getString(PREF_CONTINUOUS_HR, CONTINUOUS_HR_ALWAYS)) {
            case CONTINUOUS_HR_OFF:
                hrState = PolarProto.PbAutomaticMeasurementSettings.PbAutomaticMeasurementState.OFF;
                break;
            case CONTINUOUS_HR_NIGHT:
                hrState = PolarProto.PbAutomaticMeasurementSettings.PbAutomaticMeasurementState.INTELLIGENT_TIMED;
                break;
            default:
                hrState = PolarProto.PbAutomaticMeasurementSettings.PbAutomaticMeasurementState.ALWAYS_ON;
        }
        final PolarProto.PbUserAutomaticMeasurementSettings.Builder measurement = builder.getAutomaticMeasurementSettings().toBuilder();
        final PolarProto.PbAutomaticMeasurementSettings.Builder ohr = measurement.getAutomaticOhrMeasurement().toBuilder();
        // "night time only" is shown for both timed modes, keep the one the watch has
        if (!(hrState == PolarProto.PbAutomaticMeasurementSettings.PbAutomaticMeasurementState.INTELLIGENT_TIMED
                && ohr.getState() == PolarProto.PbAutomaticMeasurementSettings.PbAutomaticMeasurementState.TIMED)) {
            ohr.setState(hrState);
        }
        if (ohr.getState() == PolarProto.PbAutomaticMeasurementSettings.PbAutomaticMeasurementState.INTELLIGENT_TIMED
                && !ohr.hasIntelligentTimedSettings()) {
            ohr.setIntelligentTimedSettings(PolarProto.PbIntelligentTimedMeasurementSettings.newBuilder()
                    .setStartTime(pbTime(LocalTime.of(22, 0))));
        }
        builder.setAutomaticMeasurementSettings(measurement.setAutomaticOhrMeasurement(ohr));

        final PolarProto.PbUserDeviceGeneralSettings.Builder general = builder.getGeneralSettings().toBuilder()
                .setInactivityAlert(prefs.getBoolean(PREF_INACTIVITY_ENABLE_NOSHED, true)
                        ? PolarProto.PbUserDeviceGeneralSettings.PbInactivityAlert.INACTIVITY_ALERT_ON
                        : PolarProto.PbUserDeviceGeneralSettings.PbInactivityAlert.INACTIVITY_ALERT_OFF)
                .setDeviceLocation(WEAR_RIGHT.equals(prefs.getString(PREF_WEARLOCATION, "left"))
                        ? PolarProto.PbDeviceLocation.DEVICE_LOCATION_WRIST_RIGHT
                        : PolarProto.PbDeviceLocation.DEVICE_LOCATION_WRIST_LEFT)
                .setVibrationFeedbackMode(prefs.getBoolean(PREF_VIBRATION_ENABLE, true)
                        ? PolarProto.PbUserDeviceGeneralSettings.PbVibrationFeedbackMode.VIBRATION_FEEDBACK_ENABLED
                        : PolarProto.PbUserDeviceGeneralSettings.PbVibrationFeedbackMode.VIBRATION_FEEDBACK_DISABLED)
                .setButtonLockMode(prefs.getBoolean(PREF_AUTO_BUTTON_LOCK, false)
                        ? PolarProto.PbUserDeviceGeneralSettings.PbButtonLockMode.AUTO
                        : PolarProto.PbUserDeviceGeneralSettings.PbButtonLockMode.MANUAL);
        final PolarProto.PbUserDeviceGeneralSettings.PbTapButtonSensitivity tap = enumPref(prefs, PREF_TAP_SENSITIVITY,
                PolarProto.PbUserDeviceGeneralSettings.PbTapButtonSensitivity.class);
        if (tap != null) {
            general.setTapButtonSensitivity(tap);
        }
        builder.setGeneralSettings(general);

        final PolarProto.PbMediaControlSettings.PbMediaControlMode media = enumPref(prefs, PREF_MEDIA_CONTROLS,
                PolarProto.PbMediaControlSettings.PbMediaControlMode.class);
        if (media != null) {
            builder.setMediaControlSettings(builder.getMediaControlSettings().toBuilder().setMode(media));
        }

        final String satellites = prefs.getString(PREF_SATELLITES, null);
        if (satellites != null && builder.getGnssSettings().hasSatelliteSystemSettings()) {
            builder.setGnssSettings(builder.getGnssSettings().toBuilder().setSatelliteSystemSettings(
                    builder.getGnssSettings().getSatelliteSystemSettings().toBuilder()
                            .setGpsEnabled(true)
                            .setGlonassEnabled(SATELLITES_GLONASS.equals(satellites))
                            .setGalileoEnabled(SATELLITES_GALILEO.equals(satellites))
                            .setBeidouEnabled(SATELLITES_BEIDOU.equals(satellites))
                            .setQzssEnabled(SATELLITES_QZSS.equals(satellites))));
        }

        setNextDaylightSavingTransition(builder);

        if (builder.build().equals(current)) {
            return null;
        }
        return builder.setLastModified(now()).build();
    }

    /** The watch switches between summer and winter time itself, at the time given here */
    private static void setNextDaylightSavingTransition(final PolarProto.PbUserDeviceSettings.Builder builder) {
        final ZoneOffsetTransition transition = ZoneId.systemDefault().getRules().nextTransition(Instant.now());
        if (transition == null) {
            builder.clearDaylightSaving();
            return;
        }
        final ZonedDateTime utc = transition.getInstant().atZone(ZoneOffset.UTC);
        builder.setDaylightSaving(PolarProto.PbUserDeviceDaylightSaving.newBuilder()
                .setNextDaylightSavingTime(PolarProto.PbSystemDateTime.newBuilder()
                        .setDate(PolarProto.PbDate.newBuilder()
                                .setYear(utc.getYear())
                                .setMonth(utc.getMonthValue())
                                .setDay(utc.getDayOfMonth()))
                        .setTime(PolarProto.PbTime.newBuilder()
                                .setHour(utc.getHour())
                                .setMinute(utc.getMinute())
                                .setSeconds(utc.getSecond())
                                .setMillis(0))
                        .setTrusted(true))
                .setOffset(transition.getOffsetAfter().getTotalSeconds() - transition.getOffsetBefore().getTotalSeconds()));
    }

    @Nullable
    static PolarProto.PbGeneralPreferences applyGeneralPreferences(final PolarProto.PbGeneralPreferences current,
                                                                   final DevicePrefs prefs,
                                                                   final DistanceUnit distanceUnit) {
        final PolarProto.PbLocalizationPreferences.Builder localization = current.getLocalizationPreferences().toBuilder()
                .setTimeFormat(PREF_TIMEFORMAT_12H.equals(prefs.getTimeFormat())
                        ? PolarProto.PbTimeFormat.TIME_FORMAT_12H
                        : PolarProto.PbTimeFormat.TIME_FORMAT_24H)
                .setUnitSystem(distanceUnit == DistanceUnit.IMPERIAL
                        ? PolarProto.PbUnitSystem.IMPERIAL
                        : PolarProto.PbUnitSystem.METRIC);
        final PolarProto.PbDateFormat dateFormat = enumPref(prefs, PREF_DATE_FORMAT, PolarProto.PbDateFormat.class);
        if (dateFormat != null) {
            localization.setDateFormat(dateFormat);
        }
        final PolarProto.PbDateFormatSeparator separator = enumPref(prefs, PREF_DATE_SEPARATOR, PolarProto.PbDateFormatSeparator.class);
        if (separator != null) {
            localization.setDateFormatSeparator(separator);
        }
        final PolarProto.PbStartDayOfWeek firstDay = enumPref(prefs, PREF_FIRST_DAY_OF_WEEK, PolarProto.PbStartDayOfWeek.class);
        if (firstDay != null) {
            localization.setFirstdayOfWeek(firstDay);
        }
        if (localization.build().equals(current.getLocalizationPreferences())) {
            return null;
        }
        return current.toBuilder()
                .setLocalizationPreferences(localization)
                .setLastModified(now())
                .build();
    }

    @Nullable
    static PolarProto.PbUserPhysData applyUserProfile(final PolarProto.PbUserPhysData current,
                                                      final ActivityUser user,
                                                      final Prefs userPrefs) {
        final PolarProto.PbUserPhysData.Builder builder = current.toBuilder();
        final PolarProto.PbSystemDateTime now = now();
        boolean changed = false;

        final LocalDate dateOfBirth = user.getDateOfBirth();
        final PolarProto.PbDate birthday = PolarProto.PbDate.newBuilder()
                .setYear(dateOfBirth.getYear())
                .setMonth(dateOfBirth.getMonthValue())
                .setDay(dateOfBirth.getDayOfMonth())
                .build();
        if (userPrefs.contains(ActivityUser.PREF_USER_DATE_OF_BIRTH) && !birthday.equals(current.getBirthday().getValue())) {
            builder.setBirthday(PolarProto.PbUserBirthday.newBuilder().setValue(birthday).setLastModified(now));
            changed = true;
            // An age based maximum heart rate has to follow the birthday
            if (current.getMaximumHeartrate().getSettingSource() != PolarProto.PbUserHrAttribute.HrSettingSource.SOURCE_USER
                    && current.getMaximumHeartrate().getSettingSource() != PolarProto.PbUserHrAttribute.HrSettingSource.SOURCE_MEASURED) {
                builder.setMaximumHeartrate(PolarProto.PbUserHrAttribute.newBuilder()
                        .setValue(220 - user.getAge())
                        .setLastModified(now)
                        .setSettingSource(PolarProto.PbUserHrAttribute.HrSettingSource.SOURCE_AGE_BASED));
            }
        }

        // The watch has no "other", keep what it has then
        final PolarProto.PbUserGender.Gender gender = user.getGender() == ActivityUser.GENDER_FEMALE
                ? PolarProto.PbUserGender.Gender.FEMALE
                : PolarProto.PbUserGender.Gender.MALE;
        if (userPrefs.contains(ActivityUser.PREF_USER_GENDER)
                && user.getGender() != ActivityUser.GENDER_OTHER
                && (!current.getGender().hasValue() || current.getGender().getValue() != gender)) {
            builder.setGender(PolarProto.PbUserGender.newBuilder().setValue(gender).setLastModified(now));
            changed = true;
        }

        if (userPrefs.contains(ActivityUser.PREF_USER_WEIGHT_KG) && current.getWeight().getValue() != user.getWeightKg()) {
            builder.setWeight(PolarProto.PbUserWeight.newBuilder()
                    .setValue(user.getWeightKg())
                    .setLastModified(now)
                    .setSettingSource(PolarProto.PbUserWeight.WeightSettingSource.SOURCE_USER));
            changed = true;
        }

        if (userPrefs.contains(ActivityUser.PREF_USER_HEIGHT_CM) && current.getHeight().getValue() != user.getHeightCm()) {
            builder.setHeight(PolarProto.PbUserHeight.newBuilder().setValue(user.getHeightCm()).setLastModified(now));
            changed = true;
        }

        if (userPrefs.contains(ActivityUser.PREF_USER_SLEEP_DURATION_MINUTES)
                && current.getSleepGoal().getSleepGoalMinutes() != user.getSleepDurationGoal()) {
            builder.setSleepGoal(PolarProto.PbSleepGoal.newBuilder()
                    .setSleepGoalMinutes(user.getSleepDurationGoal())
                    .setLastModified(now));
            changed = true;
        }

        if (!changed) {
            return null;
        }
        return builder.setLastModified(now).build();
    }

    private static String timeString(final PolarProto.PbTime time) {
        return String.format(Locale.ROOT, "%02d:%02d", time.getHour(), time.getMinute());
    }

    private static final int WEEKDAYS = Alarm.ALARM_MON | Alarm.ALARM_TUE | Alarm.ALARM_WED | Alarm.ALARM_THU | Alarm.ALARM_FRI;

    /**
     * The watch has a single alarm that rings once, Monday to Friday or every day. Other day
     * patterns are widened to the smallest of those that covers them.
     */
    static PolarProto.PbUserDeviceSettings applyAlarm(final PolarProto.PbUserDeviceSettings current, @Nullable final Alarm alarm) {
        final PolarProto.PbUserDeviceAlarmSettings.PbAlarmMode mode;
        if (alarm == null || !alarm.getEnabled() || alarm.getUnused()) {
            mode = PolarProto.PbUserDeviceAlarmSettings.PbAlarmMode.ALARM_MODE_OFF;
        } else if (alarm.getRepetition() == Alarm.ALARM_ONCE) {
            mode = PolarProto.PbUserDeviceAlarmSettings.PbAlarmMode.ALARM_MODE_ONCE;
        } else if ((alarm.getRepetition() & ~WEEKDAYS) == 0) {
            mode = PolarProto.PbUserDeviceAlarmSettings.PbAlarmMode.ALARM_MODE_MON_TO_FRI;
        } else {
            mode = PolarProto.PbUserDeviceAlarmSettings.PbAlarmMode.ALARM_MODE_EVERY_DAY;
        }
        final PolarProto.PbUserDeviceAlarmSettings.Builder settings = current.getAlarmSettings().toBuilder().setAlarmMode(mode);
        if (alarm != null) {
            settings.setAlarmTime(pbTime(LocalTime.of(alarm.getHour(), alarm.getMinute())));
        }
        return current.toBuilder().setAlarmSettings(settings).build();
    }

    private static PolarProto.PbTime pbTime(final LocalTime time) {
        return PolarProto.PbTime.newBuilder()
                .setHour(time.getHour())
                .setMinute(time.getMinute())
                .setSeconds(0)
                .setMillis(0)
                .build();
    }

    /** last_modified is UTC, like Polar Flow writes it */
    static PolarProto.PbSystemDateTime now() {
        final GregorianCalendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        return PolarProto.PbSystemDateTime.newBuilder()
                .setDate(PolarProto.PbDate.newBuilder()
                        .setYear(utc.get(Calendar.YEAR))
                        .setMonth(utc.get(Calendar.MONTH) + 1)
                        .setDay(utc.get(Calendar.DAY_OF_MONTH)))
                .setTime(PolarProto.PbTime.newBuilder()
                        .setHour(utc.get(Calendar.HOUR_OF_DAY))
                        .setMinute(utc.get(Calendar.MINUTE))
                        .setSeconds(utc.get(Calendar.SECOND))
                        .setMillis(utc.get(Calendar.MILLISECOND)))
                .setTrusted(true)
                .build();
    }
}
