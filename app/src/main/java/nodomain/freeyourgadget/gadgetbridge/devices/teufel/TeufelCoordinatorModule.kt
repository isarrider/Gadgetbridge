package nodomain.freeyourgadget.gadgetbridge.devices.teufel

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each teufel device type, keyed by its [DeviceType]. A new teufel device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface TeufelCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.TEUFEL_MYND)
    fun teufelMynd(coordinator: TeufelMyndCoordinator): DeviceCoordinator
}
