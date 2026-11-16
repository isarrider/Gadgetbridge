package nodomain.freeyourgadget.gadgetbridge.devices.lenovo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.lenovo.watch9.Watch9DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.lenovo.watchxplus.WatchXPlusDeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each lenovo device type, keyed by its [DeviceType]. A new lenovo device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface LenovoCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WATCH9)
    fun watch9(coordinator: Watch9DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WATCHXPLUS)
    fun watchxplus(coordinator: WatchXPlusDeviceCoordinator): DeviceCoordinator
}
