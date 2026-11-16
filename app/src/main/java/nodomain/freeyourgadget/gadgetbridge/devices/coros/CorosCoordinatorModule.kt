package nodomain.freeyourgadget.gadgetbridge.devices.coros

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each coros device type, keyed by its [DeviceType]. A new coros device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface CorosCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COROS_PACE_3)
    fun corosPace3(coordinator: CorosPace3Coordinator): DeviceCoordinator
}
