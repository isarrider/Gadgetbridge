package nodomain.freeyourgadget.gadgetbridge.devices.wearfit

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.wearfit.devices.WearfitX9UltraCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each wearfit device type, keyed by its [DeviceType]. A new wearfit device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface WearfitCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WEARFIT_HK8_PRO)
    fun wearfitHk8Pro(coordinator: WearFitCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WEARFIT_X9_ULTRA)
    fun wearfitX9Ultra(coordinator: WearfitX9UltraCoordinator): DeviceCoordinator
}
