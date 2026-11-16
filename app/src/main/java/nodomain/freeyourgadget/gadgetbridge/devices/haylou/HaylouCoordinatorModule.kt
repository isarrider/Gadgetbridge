package nodomain.freeyourgadget.gadgetbridge.devices.haylou

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each haylou device type, keyed by its [DeviceType]. A new haylou device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface HaylouCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HAYLOU_S35_ANC)
    fun haylouS35Anc(coordinator: HaylouS35AncCoordinator): DeviceCoordinator
}
