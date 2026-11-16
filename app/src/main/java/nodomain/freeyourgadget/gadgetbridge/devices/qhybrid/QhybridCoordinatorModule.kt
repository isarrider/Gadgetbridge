package nodomain.freeyourgadget.gadgetbridge.devices.qhybrid

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each qhybrid device type, keyed by its [DeviceType]. A new qhybrid device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface QhybridCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.FOSSILQHYBRID)
    fun fossilqhybrid(coordinator: QHybridCoordinator): DeviceCoordinator
}
