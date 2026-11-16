package nodomain.freeyourgadget.gadgetbridge.devices.tlw64

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each tlw64 device type, keyed by its [DeviceType]. A new tlw64 device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Tlw64CoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.TLW64)
    fun tlw64(coordinator: TLW64Coordinator): DeviceCoordinator
}
