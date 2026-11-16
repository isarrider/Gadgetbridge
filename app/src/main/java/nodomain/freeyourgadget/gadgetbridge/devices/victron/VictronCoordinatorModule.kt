package nodomain.freeyourgadget.gadgetbridge.devices.victron

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each victron device type, keyed by its [DeviceType]. A new victron device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface VictronCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.VICTRON_SMARTSHUNT)
    fun victronSmartshunt(coordinator: VictronSmartShuntCoordinator): DeviceCoordinator
}
