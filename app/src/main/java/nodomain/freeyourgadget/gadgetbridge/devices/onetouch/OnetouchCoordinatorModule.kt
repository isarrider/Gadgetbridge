package nodomain.freeyourgadget.gadgetbridge.devices.onetouch

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each onetouch device type, keyed by its [DeviceType]. A new onetouch device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface OnetouchCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ONETOUCH)
    fun onetouch(coordinator: OneTouchCoordinator): DeviceCoordinator
}
