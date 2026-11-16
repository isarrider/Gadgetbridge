package nodomain.freeyourgadget.gadgetbridge.devices.cycling_sensor

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.cycling_sensor.coordinator.CyclingSensorCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each cycling_sensor device type, keyed by its [DeviceType]. A new cycling_sensor device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface CyclingSensorCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CYCLING_SENSOR)
    fun cyclingSensor(coordinator: CyclingSensorCoordinator): DeviceCoordinator
}
