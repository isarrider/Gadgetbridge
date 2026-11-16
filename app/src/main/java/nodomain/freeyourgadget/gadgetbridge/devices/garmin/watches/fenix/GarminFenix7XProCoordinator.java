package nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.GarminWatchCoordinator;

public class GarminFenix7XProCoordinator extends GarminWatchCoordinator {
    @Inject
    public GarminFenix7XProCoordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^fenix 7X Pro$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_fenix_7x_pro;
    }
}
