package nodomain.freeyourgadget.gadgetbridge.devices.roidmi

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each roidmi device type, keyed by its [DeviceType]. A new roidmi device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface RoidmiCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ROIDMI)
    fun roidmi(coordinator: Roidmi1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ROIDMI3)
    fun roidmi3(coordinator: Roidmi3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ROIDMI_F8)
    fun roidmiF8(coordinator: RoidmiF8Coordinator): DeviceCoordinator
}
