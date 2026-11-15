package nodomain.freeyourgadget.gadgetbridge.devices.nikon

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanFilter
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.DeviceSettingsSpec
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.deviceSettings
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractBLEDeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport
import nodomain.freeyourgadget.gadgetbridge.service.btle.BluetoothCompanyIdentifiers
import nodomain.freeyourgadget.gadgetbridge.service.devices.nikon.NikonBleSupport

abstract class AbstractNikonCoordinator : AbstractBLEDeviceCoordinator() {
    override fun getManufacturer(): String {
        return "Nikon"
    }

    override fun createDeviceSupport(device: GBDevice): DeviceSupport {
        return NikonBleSupport()
    }

    @SuppressLint("MissingPermission")
    override fun supports(candidate: GBDeviceCandidate): Boolean {
        // Reject the classic address, which has the same name as the LE advertisement.
        return candidate.device?.type != BluetoothDevice.DEVICE_TYPE_CLASSIC && super.supports(candidate)
    }

    override fun getBondingStyle(): Int {
        return BONDING_STYLE_NONE
    }

    override fun usesRotatingAddress(): Boolean {
        return true
    }

    override fun createReconnectScanFilters(device: GBDevice): List<ScanFilter> {
        val identifier = GBApplication.getDeviceSpecificSharedPrefs(device.address).getString(PREF_IDENTIFIER, null)
            ?: return emptyList()
        // The name is padded with NUL
        val advertisedName = device.name
            .trimEnd { it == '\u0000' || it.isWhitespace() }
            .padEnd(ADVERTISED_NAME_LENGTH, '\u0000')
        return listOf(
            ScanFilter.Builder()
                .setManufacturerData(
                    BluetoothCompanyIdentifiers.NIKON_CORPORATION,
                    identifier.hexToByteArray().copyOf(4)
                )
                .build(),
            ScanFilter.Builder()
                .setDeviceName(advertisedName)
                .build()
        )
    }

    override fun getDefaultIconResource(): Int {
        return R.drawable.ic_device_camera
    }

    override fun getDeviceKind(device: GBDevice): DeviceCoordinator.DeviceKind {
        return DeviceCoordinator.DeviceKind.CAMERA
    }

    override fun supportsGallery(device: GBDevice): Boolean {
        // TODO NikonGallerySource not yet implemented
        return false
    }

    override fun getDeviceSettings(device: GBDevice): DeviceSettingsSpec = deviceSettings {

    }

    companion object {
        const val PREF_IDENTIFIER = "nikon_identifier"
        const val PREF_CLASSIC_ADDRESS = "nikon_classic_address"

        private const val ADVERTISED_NAME_LENGTH = 26
    }
}
