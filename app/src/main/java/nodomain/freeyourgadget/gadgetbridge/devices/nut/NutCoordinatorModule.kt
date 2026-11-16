package nodomain.freeyourgadget.gadgetbridge.devices.nut

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each nut device type, keyed by its [DeviceType]. A new nut device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface NutCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NUTMINI)
    fun nutmini(coordinator: NutCoordinator): DeviceCoordinator
}
