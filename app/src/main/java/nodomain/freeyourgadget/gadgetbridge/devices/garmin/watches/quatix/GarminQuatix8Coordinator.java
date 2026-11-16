package nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.quatix;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.GarminWatchCoordinator;

public class GarminQuatix8Coordinator extends GarminWatchCoordinator {
    @Inject
    public GarminQuatix8Coordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^quatix 8( - [\\d+]+mm)?$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_quatix_8;
    }
}
