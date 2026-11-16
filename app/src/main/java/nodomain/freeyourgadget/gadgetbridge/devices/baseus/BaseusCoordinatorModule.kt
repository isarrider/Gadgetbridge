package nodomain.freeyourgadget.gadgetbridge.devices.baseus

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each baseus device type, keyed by its [DeviceType]. A new baseus device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface BaseusCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BASEUS_BOWIE_MC1)
    fun baseusBowieMc1(coordinator: BaseusBowieMc1Coordinator): DeviceCoordinator
}
