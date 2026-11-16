package nodomain.freeyourgadget.gadgetbridge.devices.cardo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each cardo device type, keyed by its [DeviceType]. A new cardo device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface CardoCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CARDO_LS24X)
    fun cardoLs24x(coordinator: Ls24xDeviceCoordinator): DeviceCoordinator
}
