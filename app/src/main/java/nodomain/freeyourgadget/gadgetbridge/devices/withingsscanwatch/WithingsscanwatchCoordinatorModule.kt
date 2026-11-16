package nodomain.freeyourgadget.gadgetbridge.devices.withingsscanwatch

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each withingsscanwatch device type, keyed by its [DeviceType]. A new withingsscanwatch device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface WithingsscanwatchCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WITHINGS_SCANWATCH)
    fun withingsScanwatch(coordinator: WithingsScanwatchDeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WITHINGS_SCANWATCH_2)
    fun withingsScanwatch2(coordinator: WithingsScanwatch2DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.WITHINGS_SCANWATCH_LIGHT)
    fun withingsScanwatchLight(coordinator: WithingsScanwatchLightDeviceCoordinator): DeviceCoordinator
}
