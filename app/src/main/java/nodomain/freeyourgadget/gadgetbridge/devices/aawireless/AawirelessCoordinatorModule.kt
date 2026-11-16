package nodomain.freeyourgadget.gadgetbridge.devices.aawireless

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each aawireless device type, keyed by its [DeviceType]. A new aawireless device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface AawirelessCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AAWIRELESS)
    fun aawireless(coordinator: AAWirelessCoordinator): DeviceCoordinator
}
