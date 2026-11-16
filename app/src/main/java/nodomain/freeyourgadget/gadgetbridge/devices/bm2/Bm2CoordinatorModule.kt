package nodomain.freeyourgadget.gadgetbridge.devices.bm2

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each bm2 device type, keyed by its [DeviceType]. A new bm2 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Bm2CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BM2_BATTERY_MONITOR)
    fun bm2BatteryMonitor(coordinator: Bm2Coordinator): DeviceCoordinator
}
