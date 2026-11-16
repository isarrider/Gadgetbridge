package nodomain.freeyourgadget.gadgetbridge.devices.generic_headphones

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each generic_headphones device type, keyed by its [DeviceType]. A new generic_headphones device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GenericHeadphonesCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GENERIC_HEADPHONES)
    fun genericHeadphones(coordinator: GenericHeadphonesCoordinator): DeviceCoordinator
}
