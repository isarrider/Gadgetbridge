package nodomain.freeyourgadget.gadgetbridge.devices.vesc

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each vesc device type, keyed by its [DeviceType]. A new vesc device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface VescCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.VESC)
    fun vesc(coordinator: VescCoordinator): DeviceCoordinator
}
