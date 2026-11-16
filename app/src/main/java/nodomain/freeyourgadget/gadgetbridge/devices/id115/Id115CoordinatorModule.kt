package nodomain.freeyourgadget.gadgetbridge.devices.id115

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each id115 device type, keyed by its [DeviceType]. A new id115 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Id115CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ID115)
    fun id115(coordinator: ID115Coordinator): DeviceCoordinator
}
