package nodomain.freeyourgadget.gadgetbridge.devices.sinilink

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each sinilink device type, keyed by its [DeviceType]. A new sinilink device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface SinilinkCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SINILINK)
    fun sinilink(coordinator: SinilinkCoordinator): DeviceCoordinator
}
