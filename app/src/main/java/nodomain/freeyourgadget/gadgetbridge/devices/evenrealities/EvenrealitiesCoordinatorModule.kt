package nodomain.freeyourgadget.gadgetbridge.devices.evenrealities

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each evenrealities device type, keyed by its [DeviceType]. A new evenrealities device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface EvenrealitiesCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.EVEN_REALITIES_G_1)
    fun evenRealitiesG1(coordinator: G1DeviceCoordinator): DeviceCoordinator
}
