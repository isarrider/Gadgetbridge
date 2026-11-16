package nodomain.freeyourgadget.gadgetbridge.devices.divoom

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each divoom device type, keyed by its [DeviceType]. A new divoom device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface DivoomCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.PIXOO)
    fun pixoo(coordinator: PixooCoordinator): DeviceCoordinator
}
