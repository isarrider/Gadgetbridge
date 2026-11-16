package nodomain.freeyourgadget.gadgetbridge.devices.innovo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each innovo device type, keyed by its [DeviceType]. A new innovo device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface InnovoCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.INNOVO_IP900BP_B)
    fun innovoIp900bpB(coordinator: InnovoPulseOximeterCoordinator): DeviceCoordinator
}
