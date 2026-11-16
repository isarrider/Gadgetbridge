package nodomain.freeyourgadget.gadgetbridge.devices.dji

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.dji.cameras.DjiOsmoAction4Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each dji device type, keyed by its [DeviceType]. A new dji device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface DjiCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.DJI_OSMO_ACTION_4)
    fun djiOsmoAction4(coordinator: DjiOsmoAction4Coordinator): DeviceCoordinator
}
