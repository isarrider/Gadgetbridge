package nodomain.freeyourgadget.gadgetbridge.devices.sennheiser

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each sennheiser device type, keyed by its [DeviceType]. A new sennheiser device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface SennheiserCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SENNHEISER_MOMENTUM_IN_EAR_WIRELESS)
    fun sennheiserMomentumInEarWireless(coordinator: SennheiserMomentumInEarWirelessCoordinator): DeviceCoordinator
}
