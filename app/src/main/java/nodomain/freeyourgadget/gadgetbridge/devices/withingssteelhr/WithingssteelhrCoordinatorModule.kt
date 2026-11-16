package nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each withingssteelhr device type, keyed by its [DeviceType]. A new withingssteelhr device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface WithingssteelhrCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WITHINGS_STEEL_HR)
    fun withingsSteelHr(coordinator: WithingsSteelHRDeviceCoordinator): DeviceCoordinator
}
