package nodomain.freeyourgadget.gadgetbridge.devices.soundpeats

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each soundpeats device type, keyed by its [DeviceType]. A new soundpeats device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface SoundpeatsCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDPEATS_Q3_PRO)
    fun soundpeatsQ3Pro(coordinator: SoundpeatsQ3ProCoordinator): DeviceCoordinator
}
