/*  Copyright (C) 2026

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/
package nodomain.freeyourgadget.gadgetbridge.devices.jbl;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.generic_headphones.GenericHeadphonesCoordinator;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryConfig;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BluetoothCompanyIdentifiers;
import nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblTwsSupport;

/**
 * Shared coordinator for JBL TWS headsets using Harman Protocol 3 over BLE.
 *
 * <p>Subclasses only provide the product ID, the Bluetooth name pattern, the
 * device name resource and the settings spec. Transport, bonding and battery
 * handling are identical across the models.</p>
 */
public abstract class AbstractJblTwsCoordinator extends GenericHeadphonesCoordinator {
    protected abstract int getProductId();

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
    public boolean suggestUnbindBeforePair() {
        return false;
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
        final byte[] product = candidate.getManufacturerSpecificData().get(
                BluetoothCompanyIdentifiers.ZHONGSHAN_CITY_RICHSOUND_ELECTRONIC_INDUSTRIAL_LTD);
        if (product != null && product.length >= 2
                && BLETypeConversions.toUint16(product) == getProductId()) {
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
        return new JblTwsSupport(getProductId());
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

    @Override
    protected abstract Pattern getSupportedDeviceName();
}
