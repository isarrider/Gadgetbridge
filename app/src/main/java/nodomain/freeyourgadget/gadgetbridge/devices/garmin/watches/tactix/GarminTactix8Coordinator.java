package nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.tactix;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.GarminWatchCoordinator;

public class GarminTactix8Coordinator extends GarminWatchCoordinator {
    @Inject
    public GarminTactix8Coordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^tactix 8( - [\\d+]+mm)?$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_tactix_8;
    }
}
