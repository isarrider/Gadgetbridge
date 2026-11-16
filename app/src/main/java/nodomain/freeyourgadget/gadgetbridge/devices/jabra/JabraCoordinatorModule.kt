package nodomain.freeyourgadget.gadgetbridge.devices.jabra

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each jabra device type, keyed by its [DeviceType]. A new jabra device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface JabraCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.JABRA_EVOLVE_65)
    fun jabraEvolve65(coordinator: JabraEvolve65Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.JABRA_EVOLVE2_55)
    fun jabraEvolve255(coordinator: JabraEvolve255Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.JABRA_EVOLVE2_65)
    fun jabraEvolve265(coordinator: JabraEvolve265Coordinator): DeviceCoordinator
}
