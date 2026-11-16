package nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.GarminWatchCoordinator;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class GarminVenuSq2Coordinator extends GarminWatchCoordinator {
    @Inject
    public GarminVenuSq2Coordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^Venu Sq 2$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_venu_sq_2;
    }

    @Override
    public boolean supportsTrainingLoad(@NonNull final GBDevice device) {
        return false;
    }

    @Override
    public boolean supportsRacePrediction(@NonNull final GBDevice device) {
        return false;
    }

    @Override
    public boolean supportsTrainingStatus(@NonNull final GBDevice device) {
        return false;
    }
}
