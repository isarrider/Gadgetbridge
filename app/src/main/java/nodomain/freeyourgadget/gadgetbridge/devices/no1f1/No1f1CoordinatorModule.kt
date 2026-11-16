package nodomain.freeyourgadget.gadgetbridge.devices.no1f1

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each no1f1 device type, keyed by its [DeviceType]. A new no1f1 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface No1f1CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NO1F1)
    fun no1f1(coordinator: No1F1Coordinator): DeviceCoordinator
}
