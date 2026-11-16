package nodomain.freeyourgadget.gadgetbridge.devices.itag

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each itag device type, keyed by its [DeviceType]. A new itag device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ItagCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ITAG)
    fun itag(coordinator: ITagCoordinator): DeviceCoordinator
}
