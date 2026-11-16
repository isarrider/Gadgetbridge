package nodomain.freeyourgadget.gadgetbridge.devices.pinetime

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each pinetime device type, keyed by its [DeviceType]. A new pinetime device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface PinetimeCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.PINETIME_JF)
    fun pinetimeJf(coordinator: PineTimeJFCoordinator): DeviceCoordinator
}
