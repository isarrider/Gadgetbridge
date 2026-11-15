package nodomain.freeyourgadget.gadgetbridge.devices.nikon

import nodomain.freeyourgadget.gadgetbridge.R
import java.util.regex.Pattern

class NikonD3400Coordinator: AbstractNikonCoordinator() {
    override fun getSupportedDeviceName(): Pattern {
        return Pattern.compile("^D3400_[0-9]+$")
    }

    override fun getDeviceNameResource(): Int {
        return R.string.devicetype_nikon_d3400
    }
}
