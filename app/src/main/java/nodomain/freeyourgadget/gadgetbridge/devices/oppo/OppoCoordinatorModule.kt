package nodomain.freeyourgadget.gadgetbridge.devices.oppo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each oppo device type, keyed by its [DeviceType]. A new oppo device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface OppoCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.OPPO_ENCO_AIR)
    fun oppoEncoAir(coordinator: OppoEncoAirCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.OPPO_ENCO_AIR2)
    fun oppoEncoAir2(coordinator: OppoEncoAir2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.OPPO_ENCO_BUDS2)
    fun oppoEncoBuds2(coordinator: OppoEncoBuds2Coordinator): DeviceCoordinator
}
