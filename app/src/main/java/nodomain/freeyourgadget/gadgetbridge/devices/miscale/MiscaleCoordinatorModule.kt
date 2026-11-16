package nodomain.freeyourgadget.gadgetbridge.devices.miscale

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each miscale device type, keyed by its [DeviceType]. A new miscale device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface MiscaleCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MISMARTSCALE)
    fun mismartscale(coordinator: MiSmartScaleCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MICOMPOSITIONSCALE)
    fun micompositionscale(coordinator: MiCompositionScaleCoordinator): DeviceCoordinator
}
