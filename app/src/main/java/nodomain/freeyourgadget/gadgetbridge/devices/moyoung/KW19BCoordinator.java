package nodomain.freeyourgadget.gadgetbridge.devices.moyoung;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.regex.Pattern;
import javax.inject.Inject;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class KW19BCoordinator extends AbstractMoyoungDeviceCoordinator {
    @Inject
    public KW19BCoordinator() {
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^KW19-B$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_kw19_b;
    }

    @Override
    @DrawableRes
    public int getDefaultIconResource() {
        return R.drawable.ic_device_watchxplus;
    }

    @Override
    public String getManufacturer() {
        return "Mo Young / Da Fit";
    }

    @Override
    public boolean supportsBloodPressureMeasurement(@NonNull final GBDevice device) {
        // untested
        return true;
    }
}
