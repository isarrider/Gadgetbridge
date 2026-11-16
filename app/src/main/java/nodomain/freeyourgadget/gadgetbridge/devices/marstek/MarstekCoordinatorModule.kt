package nodomain.freeyourgadget.gadgetbridge.devices.marstek

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each marstek device type, keyed by its [DeviceType]. A new marstek device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface MarstekCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MARSTEK_B2500)
    fun marstekB2500(coordinator: MarstekB2500DeviceCoordinator): DeviceCoordinator
}
