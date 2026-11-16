package nodomain.freeyourgadget.gadgetbridge.devices.asteroidos

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each asteroidos device type, keyed by its [DeviceType]. A new asteroidos device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface AsteroidosCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ASTEROIDOS)
    fun asteroidos(coordinator: AsteroidOSDeviceCoordinator): DeviceCoordinator
}
