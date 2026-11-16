package nodomain.freeyourgadget.gadgetbridge.devices.braun

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each braun device type, keyed by its [DeviceType]. A new braun device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface BraunCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BRAUN_BPW4500)
    fun braunBpw4500(coordinator: BraunBPW4500DeviceCoordinator): DeviceCoordinator
}
