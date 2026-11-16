package nodomain.freeyourgadget.gadgetbridge.devices.pebble

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each pebble device type, keyed by its [DeviceType]. A new pebble device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface PebbleCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.PEBBLE)
    fun pebble(coordinator: PebbleCoordinator): DeviceCoordinator
}
