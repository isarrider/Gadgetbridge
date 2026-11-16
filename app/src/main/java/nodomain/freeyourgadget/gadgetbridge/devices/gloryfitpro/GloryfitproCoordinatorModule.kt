package nodomain.freeyourgadget.gadgetbridge.devices.gloryfitpro

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfitpro.watches.DM58Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each gloryfitpro device type, keyed by its [DeviceType]. A new gloryfitpro device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GloryfitproCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.DM58)
    fun dm58(coordinator: DM58Coordinator): DeviceCoordinator
}
