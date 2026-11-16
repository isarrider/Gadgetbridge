package nodomain.freeyourgadget.gadgetbridge.devices.makibeshr3

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each makibeshr3 device type, keyed by its [DeviceType]. A new makibeshr3 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Makibeshr3CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MAKIBESHR3)
    fun makibeshr3(coordinator: MakibesHR3Coordinator): DeviceCoordinator
}
