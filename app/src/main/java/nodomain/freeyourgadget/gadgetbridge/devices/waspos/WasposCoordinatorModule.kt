package nodomain.freeyourgadget.gadgetbridge.devices.waspos

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each waspos device type, keyed by its [DeviceType]. A new waspos device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface WasposCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WASPOS)
    fun waspos(coordinator: WaspOSCoordinator): DeviceCoordinator
}
