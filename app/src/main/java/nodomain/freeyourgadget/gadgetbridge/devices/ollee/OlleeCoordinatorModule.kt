package nodomain.freeyourgadget.gadgetbridge.devices.ollee

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each ollee device type, keyed by its [DeviceType]. A new ollee device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface OlleeCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.OLLEE_WATCH_ONE)
    fun olleeWatchOne(coordinator: OlleeDeviceCoordinator): DeviceCoordinator
}
