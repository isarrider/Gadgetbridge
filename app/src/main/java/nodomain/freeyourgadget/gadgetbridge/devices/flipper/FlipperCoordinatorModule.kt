package nodomain.freeyourgadget.gadgetbridge.devices.flipper

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.flipper.zero.FlipperZeroCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each flipper device type, keyed by its [DeviceType]. A new flipper device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface FlipperCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.FLIPPER_ZERO)
    fun flipperZero(coordinator: FlipperZeroCoordinator): DeviceCoordinator
}
