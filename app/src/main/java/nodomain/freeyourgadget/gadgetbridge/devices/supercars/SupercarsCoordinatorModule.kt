package nodomain.freeyourgadget.gadgetbridge.devices.supercars

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each supercars device type, keyed by its [DeviceType]. A new supercars device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface SupercarsCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SUPER_CARS)
    fun superCars(coordinator: SuperCarsCoordinator): DeviceCoordinator
}
