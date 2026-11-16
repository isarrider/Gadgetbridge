package nodomain.freeyourgadget.gadgetbridge.devices.domyos

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each domyos device type, keyed by its [DeviceType]. A new domyos device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface DomyosCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.DOMYOS_T540)
    fun domyosT540(coordinator: DomyosT540Coordinator): DeviceCoordinator
}
