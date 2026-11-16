package nodomain.freeyourgadget.gadgetbridge.devices.xiaomi_scooters

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each xiaomi_scooters device type, keyed by its [DeviceType]. A new xiaomi_scooters device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface XiaomiScootersCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_SCOOTER_5_MAX)
    fun xiaomiScooter5Max(coordinator: XiaomiScooter5MaxCoordinator): DeviceCoordinator
}
