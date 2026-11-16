package nodomain.freeyourgadget.gadgetbridge.devices.binary_sensor

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.binary_sensor.coordinator.BinarySensorCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each binary_sensor device type, keyed by its [DeviceType]. A new binary_sensor device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface BinarySensorCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BINARY_SENSOR)
    fun binarySensor(coordinator: BinarySensorCoordinator): DeviceCoordinator
}
