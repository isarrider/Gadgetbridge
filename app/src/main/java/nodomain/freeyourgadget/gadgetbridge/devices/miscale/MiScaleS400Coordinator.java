/*  Copyright (C) 2026 Viktor Karpov

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
package nodomain.freeyourgadget.gadgetbridge.devices.miscale;

import android.bluetooth.le.ScanFilter;
import android.content.SharedPreferences;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsCustomizer;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractBLEDeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.MiScaleWeightSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.TimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.model.GenericItem;
import nodomain.freeyourgadget.gadgetbridge.model.ItemWithDetails;
import nodomain.freeyourgadget.gadgetbridge.model.WeightSample;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.devices.miscale.MiScaleS400Decryptor;
import nodomain.freeyourgadget.gadgetbridge.service.devices.miscale.MiScaleS400DeviceSupport;

public class MiScaleS400Coordinator extends AbstractBLEDeviceCoordinator {

    public static final String PREF_LAST_MEASUREMENT_TIMESTAMP = "pref_miscale_s400_last_measurement_timestamp";
    public static final String PREF_LAST_WEIGHT_KG = "pref_miscale_s400_last_weight_kg";
    public static final String PREF_LAST_HEART_RATE = "pref_miscale_s400_last_heart_rate";

    @Override
    public String getManufacturer() {
        return "Xiaomi";
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile(".*(SCALE S400|XMTZC14HM|MJTZC01YM).*", Pattern.CASE_INSENSITIVE);
    }

    @NonNull
    @Override
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        return Collections.singletonList(
                new ScanFilter.Builder()
                        .setServiceUuid(new ParcelUuid(GattService.SERVICE_BODY_COMPOSITION.uuid))
                        .build()
        );
    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(final GBDevice device) {
        return new int[]{R.xml.devicesettings_miscales400};
    }

    @Override
    public DeviceSpecificSettingsCustomizer getDeviceSpecificSettingsCustomizer(final GBDevice device) {
        return new MiScaleS400SettingsCustomizer();
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_miscales400;
    }

    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_device_miscale;
    }

    @Override
    public int getBatteryCount(final GBDevice device) {
        return 0;
    }

    @Override
    public int getBondingStyle() {
        return BONDING_STYLE_NONE;
    }

    @Override
    public boolean requiresAuthKey() {
        return true;
    }

    @Override
    public boolean validateAuthKey(@NonNull final String authKey) {
        return MiScaleS400Decryptor.isValidBindKey(authKey);
    }

    @Override
    public int[] getSupportedDeviceSpecificAuthenticationSettings() {
        return new int[]{R.xml.devicesettings_pairingkey};
    }

    @Override
    public GBDevice createDevice(final GBDeviceCandidate candidate, final DeviceType deviceType) {
        final GBDevice gbDevice = super.createDevice(candidate, deviceType);
        applyStoredMeasurementInfo(gbDevice);
        return gbDevice;
    }

    @Override
    public GBDevice createDevice(final Device dbDevice, final DeviceType deviceType) {
        final GBDevice gbDevice = super.createDevice(dbDevice, deviceType);
        applyStoredMeasurementInfo(gbDevice);
        return gbDevice;
    }

    @Override
    public TimeSampleProvider<? extends WeightSample> getWeightSampleProvider(final GBDevice device, final DaoSession session) {
        return new MiScaleWeightSampleProvider(device, session);
    }

    @Override
    public boolean supportsWeightMeasurement(@NonNull final GBDevice device) {
        return true;
    }

    @NonNull
    @Override
    public boolean isConnectable() {
        // The scale broadcasts its measurements: there is nothing to connect to.
        return false;
    }

    @Override
    public boolean handlesScanResults() {
        return true;
    }

    @Override
    public DeviceSupport createDeviceSupport(final GBDevice device) {
        return new MiScaleS400DeviceSupport();
    }

    @Override
    public DeviceKind getDeviceKind(@NonNull final GBDevice device) {
        return DeviceKind.SCALE;
    }

    public static void updateStoredMeasurementInfo(final GBDevice device,
                                                   final long timestamp,
                                                   final float weightKg,
                                                   final Integer heartRate) {
        final SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(device.getAddress());
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PREF_LAST_MEASUREMENT_TIMESTAMP, timestamp);
        editor.putFloat(PREF_LAST_WEIGHT_KG, weightKg);

        if (heartRate != null) {
            editor.putInt(PREF_LAST_HEART_RATE, heartRate);
        } else {
            editor.remove(PREF_LAST_HEART_RATE);
        }

        editor.apply();
        applyStoredMeasurementInfo(device);
    }

    private static void applyStoredMeasurementInfo(final GBDevice device) {
        final SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(device.getAddress());
        final long timestamp = prefs.getLong(PREF_LAST_MEASUREMENT_TIMESTAMP, 0);
        if (timestamp <= 0 || !prefs.contains(PREF_LAST_WEIGHT_KG)) {
            return;
        }

        final List<ItemWithDetails> infos = new ArrayList<>();
        final DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        infos.add(new GenericItem(
                GBApplication.getContext().getString(R.string.miscale_s400_last_measurement),
                dateTimeFormat.format(new Date(timestamp))
        ));
        infos.add(new GenericItem(
                GBApplication.getContext().getString(R.string.miscale_s400_last_weight),
                GBApplication.getContext().getString(R.string.weight_kg, prefs.getFloat(PREF_LAST_WEIGHT_KG, 0))
        ));

        if (prefs.contains(PREF_LAST_HEART_RATE)) {
            infos.add(new GenericItem(
                    GBApplication.getContext().getString(R.string.miscale_s400_last_heart_rate),
                    GBApplication.getContext().getString(R.string.bpm_value_unit, prefs.getInt(PREF_LAST_HEART_RATE, 0))
            ));
        }

        for (final ItemWithDetails info : infos) {
            device.addDeviceInfo(info);
        }
    }
}
