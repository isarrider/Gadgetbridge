package nodomain.freeyourgadget.gadgetbridge.devices.generic_scale

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each generic_scale device type, keyed by its [DeviceType]. A new generic_scale device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GenericScaleCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GENERIC_WEIGHT_SCALE)
    fun genericWeightScale(coordinator: GenericWeightScaleCoordinator): DeviceCoordinator
}
