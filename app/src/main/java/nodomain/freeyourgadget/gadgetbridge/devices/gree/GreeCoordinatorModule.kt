package nodomain.freeyourgadget.gadgetbridge.devices.gree

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each gree device type, keyed by its [DeviceType]. A new gree device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GreeCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GREE_AC)
    fun greeAc(coordinator: GreeAcCoordinator): DeviceCoordinator
}
