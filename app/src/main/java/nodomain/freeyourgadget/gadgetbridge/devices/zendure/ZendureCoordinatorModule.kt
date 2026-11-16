package nodomain.freeyourgadget.gadgetbridge.devices.zendure

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each zendure device type, keyed by its [DeviceType]. A new zendure device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ZendureCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOLARFLOW)
    fun solarflow(coordinator: SolarFlowDeviceCoordinator): DeviceCoordinator
}
