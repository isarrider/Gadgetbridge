package nodomain.freeyourgadget.gadgetbridge.devices.redmibuds

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each redmibuds device type, keyed by its [DeviceType]. A new redmibuds device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface RedmibudsCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIBUDS3PRO)
    fun redmibuds3pro(coordinator: RedmiBuds3ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIBUDS4ACTIVE)
    fun redmibuds4active(coordinator: RedmiBuds4ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIBUDS5PRO)
    fun redmibuds5pro(coordinator: RedmiBuds5ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIBUDS6)
    fun redmibuds6(coordinator: RedmiBuds6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIBUDS6ACTIVE)
    fun redmibuds6active(coordinator: RedmiBuds6ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIBUDS6LITE)
    fun redmibuds6lite(coordinator: RedmiBuds6LiteCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIBUDS6PRO)
    fun redmibuds6pro(coordinator: RedmiBuds6ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIBUDS8ACTIVE)
    fun redmibuds8active(coordinator: RedmiBuds8ActiveCoordinator): DeviceCoordinator
}
