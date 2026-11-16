package nodomain.freeyourgadget.gadgetbridge.devices.galaxy_buds

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each galaxy_buds device type, keyed by its [DeviceType]. A new galaxy_buds device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GalaxyBudsCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GALAXY_BUDS_PRO)
    fun galaxyBudsPro(coordinator: GalaxyBudsProDeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GALAXY_BUDS_LIVE)
    fun galaxyBudsLive(coordinator: GalaxyBudsLiveDeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GALAXY_BUDS)
    fun galaxyBuds(coordinator: GalaxyBudsDeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GALAXY_BUDS2)
    fun galaxyBuds2(coordinator: GalaxyBuds2DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GALAXY_BUDS2_PRO)
    fun galaxyBuds2Pro(coordinator: GalaxyBuds2ProDeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GALAXY_BUDS3_PRO)
    fun galaxyBuds3Pro(coordinator: GalaxyBuds3ProDeviceCoordinator): DeviceCoordinator
}
