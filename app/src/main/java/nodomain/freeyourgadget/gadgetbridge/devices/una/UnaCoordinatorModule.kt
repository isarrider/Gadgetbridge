package nodomain.freeyourgadget.gadgetbridge.devices.una

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each una device type, keyed by its [DeviceType]. A new una device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface UnaCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.UNA_WATCH)
    fun unaWatch(coordinator: UnaDeviceCoordinator): DeviceCoordinator
}
