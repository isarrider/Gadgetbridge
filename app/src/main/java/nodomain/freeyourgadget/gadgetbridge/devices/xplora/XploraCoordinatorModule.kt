package nodomain.freeyourgadget.gadgetbridge.devices.xplora

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each xplora device type, keyed by its [DeviceType]. A new xplora device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface XploraCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XPLORA_XMOVE)
    fun xploraXmove(coordinator: XploraXmoveDeviceCoordinator): DeviceCoordinator
}
