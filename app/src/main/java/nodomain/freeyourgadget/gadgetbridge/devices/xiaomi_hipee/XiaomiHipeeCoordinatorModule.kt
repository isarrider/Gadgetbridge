package nodomain.freeyourgadget.gadgetbridge.devices.xiaomi_hipee

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each xiaomi_hipee device type, keyed by its [DeviceType]. A new xiaomi_hipee device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface XiaomiHipeeCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_HIPEE_P1)
    fun xiaomiHipeeP1(coordinator: XiaomiHipeeP1Coordinator): DeviceCoordinator
}
