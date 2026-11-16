package nodomain.freeyourgadget.gadgetbridge.devices.vibratissimo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each vibratissimo device type, keyed by its [DeviceType]. A new vibratissimo device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface VibratissimoCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.VIBRATISSIMO)
    fun vibratissimo(coordinator: VibratissimoCoordinator): DeviceCoordinator
}
