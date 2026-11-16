package nodomain.freeyourgadget.gadgetbridge.devices.zetime

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each zetime device type, keyed by its [DeviceType]. A new zetime device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ZetimeCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ZETIME)
    fun zetime(coordinator: ZeTimeCoordinator): DeviceCoordinator
}
