package nodomain.freeyourgadget.gadgetbridge.devices.um25

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.um25.Coordinator.UM25Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each um25 device type, keyed by its [DeviceType]. A new um25 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Um25CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.UM25)
    fun um25(coordinator: UM25Coordinator): DeviceCoordinator
}
