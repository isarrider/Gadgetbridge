package nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.GarminWatchCoordinator;

public class GarminVenuX1Coordinator extends GarminWatchCoordinator {
    @Inject
    public GarminVenuX1Coordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^Venu X1$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_venu_x1;
    }

    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_device_amazfit_bip;
    }

    @Override
    public boolean defaultNewSyncProtocol() {
        return true;
    }
}
