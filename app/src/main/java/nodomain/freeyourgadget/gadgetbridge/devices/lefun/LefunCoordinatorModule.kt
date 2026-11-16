package nodomain.freeyourgadget.gadgetbridge.devices.lefun

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each lefun device type, keyed by its [DeviceType]. A new lefun device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface LefunCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.LEFUN)
    fun lefun(coordinator: LefunDeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.VIVITAR_HR_BP_MONITOR_ACTIVITY_TRACKER)
    fun vivitarHrBpMonitorActivityTracker(coordinator: VivitarHrBpMonitorActivityTrackerCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BOHEMIC_SMART_BRACELET)
    fun bohemicSmartBracelet(coordinator: BohemicSmartBraceletDeviceCoordinator): DeviceCoordinator
}
