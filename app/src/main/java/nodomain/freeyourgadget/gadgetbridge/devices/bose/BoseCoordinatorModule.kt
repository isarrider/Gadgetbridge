package nodomain.freeyourgadget.gadgetbridge.devices.bose

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each bose device type, keyed by its [DeviceType]. A new bose device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface BoseCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BOSE_QC35)
    fun boseQc35(coordinator: QC35Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BOSE_NC700)
    fun boseNc700(coordinator: NC700Coordinator): DeviceCoordinator
}
