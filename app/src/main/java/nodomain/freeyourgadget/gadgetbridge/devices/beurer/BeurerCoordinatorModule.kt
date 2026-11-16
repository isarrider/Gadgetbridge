package nodomain.freeyourgadget.gadgetbridge.devices.beurer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each beurer device type, keyed by its [DeviceType]. A new beurer device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface BeurerCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BEURER_BM_59)
    fun beurerBm59(coordinator: BeurerBm59Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BEURER_BM_69)
    fun beurerBm69(coordinator: BeurerBm69Coordinator): DeviceCoordinator
}
