package nodomain.freeyourgadget.gadgetbridge.devices.fitcloud

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.fitcloud.ak102.Ak102Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.fitcloud.ak75.Ak75ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.fitcloud.ak86.Ak86Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.fitcloud.colmi.ColmiI31Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each fitcloud device type, keyed by its [DeviceType]. A new fitcloud device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface FitcloudCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AK102)
    fun ak102(coordinator: Ak102Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AK75_PRO)
    fun ak75Pro(coordinator: Ak75ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AK86)
    fun ak86(coordinator: Ak86Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_I31)
    fun colmiI31(coordinator: ColmiI31Coordinator): DeviceCoordinator
}
