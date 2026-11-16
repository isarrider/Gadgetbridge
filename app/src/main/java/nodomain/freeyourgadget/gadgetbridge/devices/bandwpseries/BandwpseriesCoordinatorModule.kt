package nodomain.freeyourgadget.gadgetbridge.devices.bandwpseries

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each bandwpseries device type, keyed by its [DeviceType]. A new bandwpseries device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface BandwpseriesCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.B_AND_W_P_SERIES)
    fun bAndWPSeries(coordinator: BandWPSeriesDeviceCoordinator): DeviceCoordinator
}
