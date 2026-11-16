package nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;

public class GarminFenix7XCoordinator extends GarminFenix7Coordinator {
    @Inject
    public GarminFenix7XCoordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^fenix 7X$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_fenix_7x;
    }
}
