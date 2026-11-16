package nodomain.freeyourgadget.gadgetbridge.service.devices.gatt_client

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each gatt_client device type, keyed by its [DeviceType]. A new gatt_client device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GattClientCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BLE_GATT_CLIENT)
    fun bleGattClient(coordinator: BleGattClientCoordinator): DeviceCoordinator
}
