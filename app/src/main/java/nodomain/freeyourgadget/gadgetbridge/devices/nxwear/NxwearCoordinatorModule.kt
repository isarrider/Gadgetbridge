package nodomain.freeyourgadget.gadgetbridge.devices.nxwear

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each nxwear device type, keyed by its [DeviceType]. A new nxwear device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface NxwearCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NX_WEAR_B8)
    fun nxWearB8(coordinator: NxWearB8Coordinator): DeviceCoordinator
}
