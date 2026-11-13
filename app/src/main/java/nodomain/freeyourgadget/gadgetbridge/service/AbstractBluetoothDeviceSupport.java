package nodomain.freeyourgadget.gadgetbridge.service;

import android.bluetooth.BluetoothAdapter;
import android.hardware.usb.UsbAccessory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public abstract class AbstractBluetoothDeviceSupport extends AbstractDeviceSupport {
    private BluetoothAdapter btAdapter;

    @Override
    public void setContext(@NonNull final GBDevice gbDevice,
                           @Nullable final BluetoothAdapter btAdapter,
                           @NonNull final DeviceSupportDependencies dependencies) {
        super.setContext(gbDevice, btAdapter, dependencies);
        this.btAdapter = btAdapter;
    }

    @Override
    public void setContext(@NonNull final GBDevice gbDevice,
                           @NonNull final UsbAccessory usbAccessory,
                           @NonNull final DeviceSupportDependencies dependencies) {
        throw new IllegalStateException("This is a Bluetooth support class");
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return btAdapter;
    }
}
