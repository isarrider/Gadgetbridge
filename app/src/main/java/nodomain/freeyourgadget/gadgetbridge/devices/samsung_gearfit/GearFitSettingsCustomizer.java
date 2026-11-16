/*  Copyright (C) 2026 MedusasSphinx

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.devices.samsung_gearfit;

import android.content.SharedPreferences;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import java.util.Collections;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsCustomizer;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsHandler;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class GearFitSettingsCustomizer implements DeviceSpecificSettingsCustomizer {
    private static final String PREF_GEARFIT_CALENDAR_DEFAULT_APPLIED = "gearfit_calendar_default_applied";
    private static final String DEFAULT_CALENDAR_LOOKAHEAD_DAYS = "1";

    @Override
    public void onPreferenceChange(final Preference preference, final DeviceSpecificSettingsHandler handler) {
    }

    @Override
    public void customizeSettings(final DeviceSpecificSettingsHandler handler,
                                  final Prefs prefs,
                                  final String rootKey) {
        if (prefs.getBoolean(PREF_GEARFIT_CALENDAR_DEFAULT_APPLIED, false)) {
            return;
        }

        final String key = DeviceSettingsPreferenceConst.PREF_CALENDAR_LOOKAHEAD_DAYS;
        final SharedPreferences sharedPreferences = prefs.getPreferences();
        // The Gear Fit event clock is intended for near-term events, so default to one day.
        sharedPreferences.edit().putString(key, DEFAULT_CALENDAR_LOOKAHEAD_DAYS).apply();
        final Preference preference = handler.findPreference(key);
        if (preference instanceof EditTextPreference) {
            ((EditTextPreference) preference).setText(DEFAULT_CALENDAR_LOOKAHEAD_DAYS);
        }
        sharedPreferences.edit().putBoolean(PREF_GEARFIT_CALENDAR_DEFAULT_APPLIED, true).apply();
    }

    @Override
    public Set<String> getPreferenceKeysWithSummary() {
        return Collections.emptySet();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
    }

    public static final Creator<GearFitSettingsCustomizer> CREATOR = new Creator<GearFitSettingsCustomizer>() {
        @Override
        public GearFitSettingsCustomizer createFromParcel(final Parcel source) {
            return new GearFitSettingsCustomizer();
        }

        @Override
        public GearFitSettingsCustomizer[] newArray(final int size) {
            return new GearFitSettingsCustomizer[size];
        }
    };
}
