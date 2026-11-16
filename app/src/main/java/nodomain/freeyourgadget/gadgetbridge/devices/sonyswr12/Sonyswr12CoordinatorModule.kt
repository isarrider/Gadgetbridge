package nodomain.freeyourgadget.gadgetbridge.devices.sonyswr12

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each sonyswr12 device type, keyed by its [DeviceType]. A new sonyswr12 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Sonyswr12CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_SWR12)
    fun sonySwr12(coordinator: SonySWR12DeviceCoordinator): DeviceCoordinator
}
