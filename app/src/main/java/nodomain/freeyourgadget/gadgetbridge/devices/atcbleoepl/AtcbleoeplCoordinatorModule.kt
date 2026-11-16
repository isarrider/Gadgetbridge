package nodomain.freeyourgadget.gadgetbridge.devices.atcbleoepl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each atcbleoepl device type, keyed by its [DeviceType]. A new atcbleoepl device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface AtcbleoeplCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ATC_BLE_OEPL)
    fun atcBleOepl(coordinator: ATCBLEOEPLCoordinator): DeviceCoordinator
}
