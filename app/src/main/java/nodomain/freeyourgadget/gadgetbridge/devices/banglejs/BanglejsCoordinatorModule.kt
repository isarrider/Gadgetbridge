package nodomain.freeyourgadget.gadgetbridge.devices.banglejs

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each banglejs device type, keyed by its [DeviceType]. A new banglejs device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface BanglejsCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BANGLEJS)
    fun banglejs(coordinator: BangleJSCoordinator): DeviceCoordinator
}
