package nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class GarminEdge840Coordinator extends GarminBikeComputerCoordinator {
    @Inject
    public GarminEdge840Coordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^Edge 840$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_edge_840;
    }

    @Override
    public int getBatteryCount(final GBDevice device) {
        return 0; // Unconfirmed: does not seem to report the battery %
    }
}
