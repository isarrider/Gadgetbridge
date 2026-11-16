package nodomain.freeyourgadget.gadgetbridge.devices.fitpro

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.fitpro.colacao.ColaCao21Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.fitpro.colacao.ColaCao23Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each fitpro device type, keyed by its [DeviceType]. A new fitpro device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface FitproCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.FITPRO)
    fun fitpro(coordinator: FitProDeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLACAO21)
    fun colacao21(coordinator: ColaCao21Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLACAO23)
    fun colacao23(coordinator: ColaCao23Coordinator): DeviceCoordinator
}
