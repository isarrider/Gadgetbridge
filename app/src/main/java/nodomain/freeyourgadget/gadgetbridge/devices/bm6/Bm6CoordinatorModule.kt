package nodomain.freeyourgadget.gadgetbridge.devices.bm6

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each bm6 device type, keyed by its [DeviceType]. A new bm6 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Bm6CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BM6_BATTERY_MONITOR)
    fun bm6BatteryMonitor(coordinator: Bm6Coordinator): DeviceCoordinator
}
