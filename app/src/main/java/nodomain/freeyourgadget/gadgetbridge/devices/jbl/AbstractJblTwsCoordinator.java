/*  Copyright (C) 2026

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/
package nodomain.freeyourgadget.gadgetbridge.devices.jbl;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.generic_headphones.GenericHeadphonesCoordinator;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryConfig;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BluetoothCompanyIdentifiers;
import nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblProtocol;
import nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblTwsProduct;
import nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblTwsSupport;
import nodomain.freeyourgadget.gadgetbridge.util.CheckSums;

/**
 * Shared coordinator for JBL TWS headsets using Harman Protocol 3 over BLE.
 *
 * <p>Subclasses provide the product (which bundles the product ID and protocol
 * capabilities), the Bluetooth name pattern, the device name resource and the
 * settings spec. Transport, bonding and battery handling are identical across
 * the models.</p>
 */
public abstract class AbstractJblTwsCoordinator extends GenericHeadphonesCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJblTwsCoordinator.class);

    private static final int[] NAME_CRC_PREFIX_LENGTHS = {31, 50};
    private static final int[] NAME_CRC_OFFSETS = {3, 5};

    @NonNull
    protected abstract JblTwsProduct getProduct();

    @Override
    public int getOrderPriority() {
        return 0;
    }

    @Override
    public DeviceCoordinator.ConnectionType getConnectionType() {
        return ConnectionType.BLE;
    }

    @Override
    public int getBondingStyle() {
        // The headset is already bonded at the Android level. Skip the
        // Gadgetbridge pairing prompt; the support registers the device in the
        // Gadgetbridge database on the first successful GATT connect.
        return BONDING_STYLE_NONE;
    }

    @Override
    public boolean usesRotatingAddress() {
        return true;
    }

    /**
     * Returns the filters that match the advertisement payload of the headset: the product ID,
     * followed by a CRC of the phone Bluetooth name at offset 3 or 5. The payload is in the
     * manufacturer data or in the Harman service data.
     */
    @NonNull
    @Override
    public List<ScanFilter> createReconnectScanFilters(@NonNull final GBDevice device) {
        final String phoneName = getPhoneBluetoothName();
        if (phoneName == null) {
            LOG.warn("Failed to get phone's Bluetooth name, no reconnect scan filters for {}", device.getAddress());
            return Collections.emptyList();
        }

        final byte[] nameBytes = phoneName.getBytes(StandardCharsets.UTF_8);
        final Set<Integer> nameCrcs = new LinkedHashSet<>();
        for (final int prefixLength : NAME_CRC_PREFIX_LENGTHS) {
            nameCrcs.add(CheckSums.getCRC16ansi(Arrays.copyOf(nameBytes, Math.min(nameBytes.length, prefixLength)), 0));
        }

        final int productId = getProduct().getProductId();
        final List<ScanFilter> filters = new ArrayList<>();
        for (final int nameCrc : nameCrcs) {
            for (final int offset : NAME_CRC_OFFSETS) {
                final byte[] data = new byte[offset + 2];
                final byte[] mask = new byte[offset + 2];
                data[0] = (byte) productId;
                data[1] = (byte) (productId >> 8);
                data[offset] = (byte) nameCrc;
                data[offset + 1] = (byte) (nameCrc >> 8);
                mask[0] = mask[1] = mask[offset] = mask[offset + 1] = (byte) 0xff;
                filters.add(new ScanFilter.Builder()
                    .setManufacturerData(BluetoothCompanyIdentifiers.ZHONGSHAN_CITY_RICHSOUND_ELECTRONIC_INDUSTRIAL_LTD, data, mask)
                    .build());
                filters.add(new ScanFilter.Builder()
                    .setServiceData(JblProtocol.HARMAN_SERVICE_DATA_UUID, data, mask)
                    .build());
            }
        }
        return filters;
    }

    @Nullable
    @SuppressLint("MissingPermission")
    private static String getPhoneBluetoothName() {
        final BluetoothManager manager = ContextCompat.getSystemService(GBApplication.getContext(), BluetoothManager.class);
        final BluetoothAdapter adapter = manager != null ? manager.getAdapter() : null;
        if (adapter == null) {
            return null;
        }
        try {
            return adapter.getName();
        } catch (final SecurityException e) {
            return null;
        }
    }

    @Override
    public boolean supportsFindDevice(@NonNull final GBDevice device) {
        return true;
    }

    @Override
    public boolean supports(@NonNull final GBDeviceCandidate candidate) {
        // The manufacturer ID is shared by many JBL devices, and the Harman
        // service UUID is not model-specific. Match the product ID from the
        // manufacturer data first, then fall back to the Bluetooth name.
        final byte[] manufacturerData = candidate.getManufacturerSpecificData().get(
                BluetoothCompanyIdentifiers.ZHONGSHAN_CITY_RICHSOUND_ELECTRONIC_INDUSTRIAL_LTD);
        if (manufacturerData != null && manufacturerData.length >= 2
                && BLETypeConversions.toUint16(manufacturerData) == getProduct().getProductId()) {
            return true;
        }

        final String name = candidate.getName();
        return name != null && getSupportedDeviceName().matcher(name).matches();
    }

    @Override
    public int getBatteryCount(@NonNull final GBDevice device) {
        return 3;
    }

    @Override
    public BatteryConfig[] getBatteryConfig(@NonNull final GBDevice device) {
        return new BatteryConfig[]{
                new BatteryConfig(0, R.drawable.ic_nothing_ear_l, R.string.left_earbud),
                new BatteryConfig(1, R.drawable.ic_nothing_ear_r, R.string.right_earbud),
                new BatteryConfig(2, R.drawable.ic_buds_pro_case, R.string.battery_case)
        };
    }

    @Override
    public DeviceSupport createDeviceSupport(@NonNull final GBDevice device) {
        return new JblTwsSupport(getProduct());
    }

    @Override
    public String getManufacturer() {
        return "JBL";
    }

    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_device_headphones;
    }

    @Override
    public DeviceCoordinator.DeviceKind getDeviceKind(@NonNull final GBDevice device) {
        return DeviceKind.EARBUDS;
    }

    @NonNull
    @Override
    protected abstract Pattern getSupportedDeviceName();
}
