package nodomain.freeyourgadget.gadgetbridge.devices.ninebot

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each ninebot device type, keyed by its [DeviceType]. A new ninebot device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface NinebotCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NINEBOT_KICKSCOOTER)
    fun ninebotKickscooter(coordinator: NinebotScooterCoordinator): DeviceCoordinator
}
