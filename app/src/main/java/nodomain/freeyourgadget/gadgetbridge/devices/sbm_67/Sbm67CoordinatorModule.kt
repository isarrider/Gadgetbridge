package nodomain.freeyourgadget.gadgetbridge.devices.sbm_67

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each sbm_67 device type, keyed by its [DeviceType]. A new sbm_67 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Sbm67CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SANITAS_SBM_67)
    fun sanitasSbm67(coordinator: SanitasSBM67Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SILVERCREST_SBM_67)
    fun silvercrestSbm67(coordinator: SilverCrestSBM67Coordinator): DeviceCoordinator
}
