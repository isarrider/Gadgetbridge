package nodomain.freeyourgadget.gadgetbridge.devices.onemoresonoflow

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each onemoresonoflow device type, keyed by its [DeviceType]. A new onemoresonoflow device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface OnemoresonoflowCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ONEMORE_SONOFLOW)
    fun onemoreSonoflow(coordinator: OneMoreSonoFlowCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ONEMORE_SONOFLOW_SE)
    fun onemoreSonoflowSe(coordinator: OneMoreSonoFlowSECoordinator): DeviceCoordinator
}
