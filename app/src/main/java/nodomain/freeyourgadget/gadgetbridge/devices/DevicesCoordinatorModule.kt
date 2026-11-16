package nodomain.freeyourgadget.gadgetbridge.devices

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinators of the device types that belong to no vendor package, keyed by their
 * [DeviceType].
 */
@Module
@InstallIn(SingletonComponent::class)
interface DevicesCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.UNKNOWN)
    fun unknown(coordinator: UnknownDeviceCoordinator): DeviceCoordinator
}
