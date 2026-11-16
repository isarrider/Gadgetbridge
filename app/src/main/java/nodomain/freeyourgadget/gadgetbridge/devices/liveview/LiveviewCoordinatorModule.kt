package nodomain.freeyourgadget.gadgetbridge.devices.liveview

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each liveview device type, keyed by its [DeviceType]. A new liveview device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface LiveviewCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.LIVEVIEW)
    fun liveview(coordinator: LiveviewCoordinator): DeviceCoordinator
}
