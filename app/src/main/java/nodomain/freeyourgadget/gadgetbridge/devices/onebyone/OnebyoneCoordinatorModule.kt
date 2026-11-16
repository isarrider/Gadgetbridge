package nodomain.freeyourgadget.gadgetbridge.devices.onebyone

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each onebyone device type, keyed by its [DeviceType]. A new onebyone device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface OnebyoneCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ONEBYONE_SCALE)
    fun onebyoneScale(coordinator: OneByoneScaleCoordinator): DeviceCoordinator
}
