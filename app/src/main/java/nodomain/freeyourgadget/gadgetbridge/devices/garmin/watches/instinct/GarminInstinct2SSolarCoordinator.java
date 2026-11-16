package nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.GarminWatchCoordinator;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class GarminInstinct2SSolarCoordinator extends GarminWatchCoordinator {
    @Inject
    public GarminInstinct2SSolarCoordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^Instinct 2S Solar$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_instinct_2s_solar;
    }

    @Override
    public boolean supportsSolarCharging(@NonNull final GBDevice device) {
        return true;
    }
}
