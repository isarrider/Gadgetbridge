package nodomain.freeyourgadget.gadgetbridge.devices.eightbitdo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each eightbitdo device type, keyed by its [DeviceType]. A new eightbitdo device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface EightbitdoCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.EIGHTBITDO_MICRO)
    fun eightbitdoMicro(coordinator: EightBitDoMicroCoordinator): DeviceCoordinator
}
