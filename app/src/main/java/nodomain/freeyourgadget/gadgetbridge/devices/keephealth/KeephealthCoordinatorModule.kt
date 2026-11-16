package nodomain.freeyourgadget.gadgetbridge.devices.keephealth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each keephealth device type, keyed by its [DeviceType]. A new keephealth device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface KeephealthCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.KEEPHEALTH_C60)
    fun keephealthC60(coordinator: C60DeviceCoordinator): DeviceCoordinator
}
