package nodomain.freeyourgadget.gadgetbridge.devices.scannable

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each scannable device type, keyed by its [DeviceType]. A new scannable device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ScannableCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SCANNABLE)
    fun scannable(coordinator: ScannableDeviceCoordinator): DeviceCoordinator
}
