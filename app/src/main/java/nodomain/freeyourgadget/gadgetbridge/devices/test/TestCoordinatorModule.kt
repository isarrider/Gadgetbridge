package nodomain.freeyourgadget.gadgetbridge.devices.test

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each test device type, keyed by its [DeviceType]. A new test device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface TestCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.TEST)
    fun test(coordinator: TestDeviceCoordinator): DeviceCoordinator
}
