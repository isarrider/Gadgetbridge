package nodomain.freeyourgadget.gadgetbridge.devices.concept2

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each concept2 device type, keyed by its [DeviceType]. A new concept2 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Concept2CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CONCEPT2_PM5)
    fun concept2Pm5(coordinator: Concept2PM5Coordinator): DeviceCoordinator
}
