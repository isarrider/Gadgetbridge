package nodomain.freeyourgadget.gadgetbridge.devices.hplus

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each hplus device type, keyed by its [DeviceType]. A new hplus device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface HplusCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HPLUS)
    fun hplus(coordinator: HPlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MAKIBESF68)
    fun makibesf68(coordinator: MakibesF68Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.EXRIZUK8)
    fun exrizuk8(coordinator: EXRIZUK8Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.Q8)
    fun q8(coordinator: Q8Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SG2)
    fun sg2(coordinator: SG2Coordinator): DeviceCoordinator
}
