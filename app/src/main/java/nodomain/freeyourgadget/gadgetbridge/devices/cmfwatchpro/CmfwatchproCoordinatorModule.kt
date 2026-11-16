package nodomain.freeyourgadget.gadgetbridge.devices.cmfwatchpro

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each cmfwatchpro device type, keyed by its [DeviceType]. A new cmfwatchpro device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface CmfwatchproCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_CMF_WATCH_PRO)
    fun nothingCmfWatchPro(coordinator: CmfWatchProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_CMF_WATCH_PRO_2)
    fun nothingCmfWatchPro2(coordinator: CmfWatchPro2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_CMF_WATCH_PRO_3)
    fun nothingCmfWatchPro3(coordinator: CmfWatchPro3Coordinator): DeviceCoordinator
}
