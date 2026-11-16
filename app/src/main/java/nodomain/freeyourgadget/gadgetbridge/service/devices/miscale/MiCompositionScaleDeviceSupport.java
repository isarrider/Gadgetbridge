/*  Copyright (C) 2019-2024 Andreas Shimokawa, Arjan Schrijver, Damien Gaignon,
    Jean-François Greffier

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.miscale;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.SettingsActivity;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.MiScaleWeightSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.MiScaleWeightSample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.WeightUnit;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLESingleDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.IntentListener;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfo;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfoProfile;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.*;

public class MiCompositionScaleDeviceSupport extends AbstractBTLESingleDeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(MiCompositionScaleDeviceSupport.class);

    private static final long SCALE_RELEASE_DATE = 1521072000000L;
    private static final int IMPEDANCE_MEASURING = 0xFFFE;

    private static final UUID UUID_CHARACTERISTIC_CONFIG = UUID.fromString("00001542-0000-3512-2118-0009af100700");
    private static final UUID UUID_CHARACTERISTIC_WEIGHT_HISTORY = UUID.fromString("00002a2f-0000-3512-2118-0009af100700");

    private static final byte CMD_HISTORY_START = (byte) 0x01;
    private static final byte CMD_HISTORY_QUERY = (byte) 0x02;
    private static final byte CMD_HISTORY_COMPLETE = (byte) 0x03;
    private static final byte CMD_HISTORY_END = (byte) 0x04;

    private long userId = -1;

    // There's unfortunately no way to query the config options, they can only be set
    private static final byte CFG_WEIGHT_UNIT = (byte) 0x04;
    private static final byte CFG_SMALL_OBJECTS = (byte) 0x10;
    private static final byte CFG_RESET_HISTORY = (byte) 0x12;

    /** Measurements below this weight are treated as small objects. */
    private static final int SMALL_OBJECT_MAX_WEIGHT = 10;

    private final DeviceInfoProfile<MiCompositionScaleDeviceSupport> deviceInfoProfile;
    private final GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();

    public MiCompositionScaleDeviceSupport() {
        super(LOG);

        // Get unique user ID for weight history querying
        try (DBHandler db = GBApplication.acquireDB()) {
            userId = DBHelper.getUser(db.getDaoSession()).getId();
        } catch (final Exception e) {
            LOG.error("Error acquiring database", e);
        }

        addSupportedService(GattService.SERVICE_GAP.uuid);
        addSupportedService(GattService.SERVICE_GATT.uuid);
        addSupportedService(GattService.SERVICE_DEVICE_INFORMATION.uuid);
        addSupportedService(GattService.SERVICE_BODY_COMPOSITION.uuid);
        addSupportedService(UUID.fromString("00001530-0000-3512-2118-0009af100700"));
        addSupportedService(UUID_CHARACTERISTIC_CONFIG);

        deviceInfoProfile = new DeviceInfoProfile<>(this);
        final IntentListener mListener = intent -> {
            if (DeviceInfoProfile.ACTION_DEVICE_INFO.equals(intent.getAction())) {
                final Parcelable deviceInfo = intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO);
                if (deviceInfo != null) {
                    handleDeviceInfo((DeviceInfo) deviceInfo);
                }
            }
        };
        deviceInfoProfile.addListener(mListener);
        addSupportedProfile(deviceInfoProfile);
    }

    @NonNull
    @Override
    protected TransactionBuilder initializeDevice(final TransactionBuilder builder) {
        builder.setDeviceState(GBDevice.State.INITIALIZING);

        deviceInfoProfile.requestDeviceInfo(builder);

        // The scale has an RTC but nothing sets it: without this, measurements are stamped with
        // whatever the factory clock says (users have reported dates around the year 2800).
        if (GBApplication.getPrefs().syncTime())
            setTime(builder);

        builder.setDeviceState(GBDevice.State.INITIALIZED);

        // Weight and body composition
        builder.setCallback(this);
        builder.notify(GattCharacteristic.CHARACTERISTIC_BODY_COMPOSITION_MEASUREMENT.uuid, true);

        fetchWeightHistory(builder);

        return builder;
    }

    @Override
    public boolean onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] data) {
        if (super.onCharacteristicChanged(gatt, characteristic, data)) {
            return true;
        }

        final UUID characteristicUUID = characteristic.getUuid();
        if (characteristicUUID.equals(GattCharacteristic.CHARACTERISTIC_BODY_COMPOSITION_MEASUREMENT.uuid)) {
            final Measurement measurement = parseBodyComposition(data, true);

            if (measurement != null) {
                handleWeightInfo(measurement.timestamp, measurement.weightKg, measurement.impedanceOhm);
            }

            return true;
        }

        if (characteristicUUID.equals(UUID_CHARACTERISTIC_WEIGHT_HISTORY)) {
            handleHistoryData(data);
            return true;
        }

        return false;
    }

    /**
     * A single body composition measurement, as the scale reports it both live (0x2A9C) and from
     * its stored history (0x2A2F) - the records returned by the history query are the very same
     * 13-byte packets, impedance included.
     */
    private static final class Measurement {
        private final Date timestamp;
        private final float weightKg;
        private final Integer impedanceOhm;

        private Measurement(final Date timestamp, final float weightKg, final Integer impedanceOhm) {
            this.timestamp = timestamp;
            this.weightKg = weightKg;
            this.impedanceOhm = impedanceOhm;
        }
    }

    /**
     * Parses a Body Composition Measurement packet: protocol version, flags, a 7-byte timestamp,
     * a 2-byte impedance slot and the 2-byte weight. See
     * https://codeberg.org/Freeyourgadget/Gadgetbridge/issues/6393 for the layout this was
     * reverse-engineered from.
     *
     * @return the measurement, or null when the packet is a status frame or the reading has not
     * stabilized yet
     */
    private Measurement parseBodyComposition(final byte[] data, final boolean requireStable) {
        if (data.length < 13) {
            return null;
        }

        final byte flags = data[1];
        final boolean stabilized = testBit(flags, 5) && !testBit(flags, 7);

        // While a live reading settles the scale repeats weight-only packets, and those must not be
        // stored. History only holds completed measurements, some of which are weight-only too (the
        // user was not standing on the electrodes), so there the flag is not required.
        if (requireStable && !stabilized) {
            return null;
        }

        final int rawImpedance = BLETypeConversions.toUint16(data, 9);
        if (rawImpedance == IMPEDANCE_MEASURING) {
            LOG.debug("Impedance measurement in progress, ignoring packet");
            return null;
        }

        final int year = BLETypeConversions.toUint16(data, 2);
        final int month = BLETypeConversions.toUnsigned(data, 4);
        final int day = BLETypeConversions.toUnsigned(data, 5);
        final int hour = BLETypeConversions.toUnsigned(data, 6);
        final int minute = BLETypeConversions.toUnsigned(data, 7);
        final int second = BLETypeConversions.toUnsigned(data, 8);
        final Calendar c = GregorianCalendar.getInstance();
        c.set(year, month - 1, day, hour, minute, second);
        c.set(Calendar.MILLISECOND, 0);

        final float weightKg = WeightMeasurement.weightToKg(
                BLETypeConversions.toUint16(data, 11),
                flags
        );

        // The impedance slot is only meaningful once the reading has stabilized, which is
        // exactly the condition checked above for live packets
        final boolean hasImpedance = testBit(flags, 1) && rawImpedance > 0;

        return new Measurement(c.getTime(), weightKg, hasImpedance ? rawImpedance : null);
    }

    private boolean testBit(final byte value, final int offset) {
        return ((value >> offset) & 1) == 1;
    }

    private void handleDeviceInfo(final DeviceInfo info) {
        LOG.debug("Device info: {}", info);
        versionCmd.hwVersion = info.getHardwareRevision();
        versionCmd.fwVersion = info.getSoftwareRevision();
        handleGBDeviceEvent(versionCmd);
    }

    private void handleWeightInfo(final Date date, final float weightKg, final Integer impedanceOhm) {
        LOG.debug("Got weight {} kg, impedance {} ohm, at {}", weightKg, impedanceOhm, date);

        if (!allowSmallObjects() && weightKg < SMALL_OBJECT_MAX_WEIGHT) {
            LOG.debug("Ignoring measurement of a small object: {} kg", weightKg);
            return;
        }
        GB.toast(getContext().getString(R.string.weight_kg, weightKg), Toast.LENGTH_SHORT, GB.INFO);

        long sampleTs = date.getTime();
        if (sampleTs < SCALE_RELEASE_DATE || sampleTs > System.currentTimeMillis() + 86400_000L) {
            LOG.warn("Implausible sample time {}, sanitizing sample date to current time", date.getTime());
            sampleTs = System.currentTimeMillis();
        }

        try (DBHandler db = GBApplication.acquireDB()) {
            final MiScaleWeightSampleProvider provider = new MiScaleWeightSampleProvider(getDevice(), db.getDaoSession());

            final MiScaleWeightSample sample = new MiScaleWeightSample();
            sample.setTimestamp(sampleTs);
            sample.setWeightKg(weightKg);
            sample.setImpedanceOhm(impedanceOhm);
            provider.persistSamples(sample, getContext());
            // let the dashboard and charts know there is a new value to show
            GB.signalActivityDataFinish(getDevice());
        } catch (final Exception e) {
            LOG.error("Error saving weight sample", e);
        }
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    @Override
    public void onFactoryReset() {
        try {
            final TransactionBuilder builder = performInitialized("reset");

            setConfigValue(builder, CFG_RESET_HISTORY, (byte) 0x00);
            builder.queue();
        } catch (final IOException e) {
            LOG.error("Error resetting the scale", e);
        }
    }

    @Override
    public void onSendConfiguration(final String config) {
        final SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());

        try {
            final TransactionBuilder builder = performInitialized("config");

            if (config.equals(SettingsActivity.PREF_UNIT_WEIGHT)) {
                final WeightUnit weightUnit = GBApplication.getPrefs().getWeightUnit();
                final byte unitByte = switch (weightUnit) {
                    case POUND -> (byte) 1;
                    case KILOGRAM, STONE -> (byte) 0;
                    case JIN -> (byte) 2;
                };
                setConfigValue(builder, CFG_WEIGHT_UNIT, unitByte);
            } else if (config.equals(PREF_MISCALE_SMALL_OBJECTS)) {
                final boolean enabled = prefs.getBoolean(PREF_MISCALE_SMALL_OBJECTS, false);

                setConfigValue(builder, CFG_SMALL_OBJECTS, enabled ? (byte) 0x01 : (byte) 0x00);
            }

            builder.queue();
        } catch (final IOException e) {
            LOG.error("Error sending configuration", e);
        }
    }

    private boolean allowSmallObjects() {
        return GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress())
                .getBoolean(PREF_MISCALE_SMALL_OBJECTS, false);
    }

    private void setTime(final TransactionBuilder builder) {
        final GregorianCalendar now = BLETypeConversions.createCalendar();
        final byte[] time = BLETypeConversions.calendarToCurrentTime(now, 0);

        builder.write(GattCharacteristic.CHARACTERISTIC_CURRENT_TIME.uuid, time);
    }

    private void setConfigValue(final TransactionBuilder builder, final byte config, final byte value) {
        final byte[] data = new byte[] { (byte) 0x06, config, (byte) 0x00, value };

        builder.write(UUID_CHARACTERISTIC_CONFIG, data);
    }

    private void fetchWeightHistory(final TransactionBuilder builder) {
        builder.notify(UUID_CHARACTERISTIC_WEIGHT_HISTORY, true);
        sendHistoryCommand(builder, CMD_HISTORY_START, true);
        sendHistoryCommand(builder, CMD_HISTORY_QUERY, false);
    }

    private void sendHistoryCommand(final TransactionBuilder builder, final byte cmd, final boolean includeUserId) {
        final byte[] data;

        if (includeUserId) {
            data = new byte[] { cmd, (byte) 0, (byte) 0, (byte) 0, (byte) userId };
        } else {
            data = new byte[] { cmd };
        }

        builder.write(UUID_CHARACTERISTIC_WEIGHT_HISTORY, data);
    }

    private void handleHistoryData(final byte[] data) {
        if (data.length == 1 && data[0] == CMD_HISTORY_COMPLETE) {
            final TransactionBuilder builder = createTransactionBuilder("ack weight history");

            // Acknowledge the reception; the scale keeps resending until it is acknowledged
            sendHistoryCommand(builder, CMD_HISTORY_COMPLETE, false);
            sendHistoryCommand(builder, CMD_HISTORY_END, true);
            builder.notify(UUID_CHARACTERISTIC_WEIGHT_HISTORY, false);
            builder.queue();

            getDevice().unsetBusyTask();
            GB.updateTransferNotification(null, "", false, 100, getContext());
            getDevice().sendDeviceUpdateIntent(getContext());
            return;
        }

        // Every stored record observed so far arrives as a single 13-byte packet, one notification
        // per measurement. Shorter packets are the status replies to the start command.
        if (data.length < 13) {
            LOG.debug("Ignoring weight history frame ({} bytes): {}", data.length, GB.hexdump(data));
            return;
        }

        if (data.length > 13) {
            LOG.warn("Unexpected weight history frame of {} bytes, parsing the first record only: {}",
                    data.length, GB.hexdump(data));
        }

        final Measurement measurement = parseBodyComposition(data, false);

        if (measurement == null) {
            LOG.debug("Ignoring weight history frame ({} bytes): {}", data.length, GB.hexdump(data));
            return;
        }

        saveHistoryMeasurement(measurement);
    }

    private void saveHistoryMeasurement(final Measurement measurement) {
        if (!allowSmallObjects() && measurement.weightKg < SMALL_OBJECT_MAX_WEIGHT) {
            LOG.debug("Ignoring historical measurement of a small object: {} kg", measurement.weightKg);
            return;
        }

        final long sampleTs = measurement.timestamp.getTime();
        if (sampleTs < SCALE_RELEASE_DATE || sampleTs > System.currentTimeMillis() + 86400_000L) {
            LOG.warn("Implausible historical sample time {}, skipping", measurement.timestamp);
            return;
        }

        try (DBHandler db = GBApplication.acquireDB()) {
            final MiScaleWeightSampleProvider provider = new MiScaleWeightSampleProvider(getDevice(), db.getDaoSession());
            final Long dbUserId = DBHelper.getUser(db.getDaoSession()).getId();
            final Long deviceId = DBHelper.getDevice(getDevice(), db.getDaoSession()).getId();

            provider.addSamples(Collections.singletonList(new MiScaleWeightSample(
                    sampleTs,
                    deviceId,
                    dbUserId,
                    measurement.weightKg,
                    measurement.impedanceOhm
            )));

            LOG.info("Saved historical measurement: {} kg, impedance {} ohm, at {}",
                    measurement.weightKg, measurement.impedanceOhm, measurement.timestamp);            // let the dashboard and charts know there are new values to show
            GB.signalActivityDataFinish(getDevice());
        } catch (final Exception e) {
            LOG.error("Error saving a historical measurement", e);
        }
    }
}
