package nodomain.freeyourgadget.gadgetbridge.devices.jyou

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.jyou.TeclastH30.TeclastH30Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.jyou.y5.Y5Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each jyou device type, keyed by its [DeviceType]. A new jyou device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface JyouCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.TECLASTH30)
    fun teclasth30(coordinator: TeclastH30Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.Y5)
    fun y5(coordinator: Y5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BFH16)
    fun bfh16(coordinator: BFH16DeviceCoordinator): DeviceCoordinator
}
