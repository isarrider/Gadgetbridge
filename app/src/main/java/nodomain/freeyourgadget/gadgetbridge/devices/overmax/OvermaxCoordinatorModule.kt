package nodomain.freeyourgadget.gadgetbridge.devices.overmax

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each overmax device type, keyed by its [DeviceType]. A new overmax device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface OvermaxCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.OVTOUCH26)
    fun ovtouch26(coordinator: OVTouch26Coordinator): DeviceCoordinator
}
