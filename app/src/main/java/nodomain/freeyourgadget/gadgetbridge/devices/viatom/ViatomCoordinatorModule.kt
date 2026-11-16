package nodomain.freeyourgadget.gadgetbridge.devices.viatom

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each viatom device type, keyed by its [DeviceType]. A new viatom device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ViatomCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.F8SCALE)
    fun f8scale(coordinator: F8ScaleCoordinator): DeviceCoordinator
}
