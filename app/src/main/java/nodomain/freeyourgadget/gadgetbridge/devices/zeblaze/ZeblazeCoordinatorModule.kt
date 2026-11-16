package nodomain.freeyourgadget.gadgetbridge.devices.zeblaze

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.zeblaze.stratos4pro.Stratos4ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each zeblaze device type, keyed by its [DeviceType]. A new zeblaze device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ZeblazeCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ZEBLAZE_BEYOND_3_PRO)
    fun zeblazeBeyond3Pro(coordinator: Beyond3ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ZEBLAZE_STRATOS_4_PRO)
    fun zeblazeStratos4Pro(coordinator: Stratos4ProCoordinator): DeviceCoordinator
}
