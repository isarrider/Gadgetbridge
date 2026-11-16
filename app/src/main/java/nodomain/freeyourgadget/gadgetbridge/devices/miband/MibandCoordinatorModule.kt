package nodomain.freeyourgadget.gadgetbridge.devices.miband

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each miband device type, keyed by its [DeviceType]. A new miband device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface MibandCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND)
    fun miband(coordinator: MiBandCoordinator): DeviceCoordinator
}
