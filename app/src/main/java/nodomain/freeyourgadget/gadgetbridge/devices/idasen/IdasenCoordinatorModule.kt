package nodomain.freeyourgadget.gadgetbridge.devices.idasen

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each idasen device type, keyed by its [DeviceType]. A new idasen device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface IdasenCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IKEA_IDASEN)
    fun ikeaIdasen(coordinator: IdasenCoordinator): DeviceCoordinator
}
