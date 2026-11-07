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
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.DeviceSettingsSpec;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblProtocol;

/** Coordinator for the JBL Tune Buds 2 (BLE control channel). */
public class JblTuneBuds2Coordinator extends AbstractJblTwsCoordinator {
    @Override
    protected int getProductId() {
        return JblProtocol.TUNE_BUDS_2_PRODUCT_ID;
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^JBL Tune Buds 2$", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public DeviceSettingsSpec getDeviceSettings(@NonNull final GBDevice device) {
        return JblTwsSettingsKt.jblTuneBuds2Settings();
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_jbl_tune_buds_2;
    }
}
