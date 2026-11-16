package nodomain.freeyourgadget.gadgetbridge.devices.generic_hr

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each generic_hr device type, keyed by its [DeviceType]. A new generic_hr device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GenericHrCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GENERIC_HEART_RATE)
    fun genericHeartRate(coordinator: GenericHeartRateCoordinator): DeviceCoordinator
}
