package nodomain.freeyourgadget.gadgetbridge.devices.shokz

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each shokz device type, keyed by its [DeviceType]. A new shokz device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ShokzCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SHOKZ_OPENSWIM_PRO)
    fun shokzOpenswimPro(coordinator: ShokzOpenSwimProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SHOKZ_OPENRUN_PRO_2)
    fun shokzOpenrunPro2(coordinator: ShokzOpenRunPro2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SHOKZ_OPENFIT_PRO)
    fun shokzOpenfitPro(coordinator: ShokzOpenFitProCoordinator): DeviceCoordinator
}
