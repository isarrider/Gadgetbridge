package nodomain.freeyourgadget.gadgetbridge.devices.ultrahuman

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each ultrahuman device type, keyed by its [DeviceType]. A new ultrahuman device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface UltrahumanCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ULTRAHUMAN_RING_AIR)
    fun ultrahumanRingAir(coordinator: UltrahumanDeviceCoordinator): DeviceCoordinator
}
