package nodomain.freeyourgadget.gadgetbridge.devices.polar

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each polar device type, keyed by its [DeviceType]. A new polar device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface PolarCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.POLARH9)
    fun polarh9(coordinator: PolarH9DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.POLARH10)
    fun polarh10(coordinator: PolarH10DeviceCoordinator): DeviceCoordinator
}
