package nodomain.freeyourgadget.gadgetbridge.devices.hama

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.hama.fit6900.HamaFit6900DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each hama device type, keyed by its [DeviceType]. A new hama device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface HamaCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HAMA_FIT6900)
    fun hamaFit6900(coordinator: HamaFit6900DeviceCoordinator): DeviceCoordinator
}
