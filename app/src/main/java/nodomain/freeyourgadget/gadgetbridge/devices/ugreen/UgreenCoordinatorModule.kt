package nodomain.freeyourgadget.gadgetbridge.devices.ugreen

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.ugreen.hitunemax5c.UgreenHituneMax5cCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each ugreen device type, keyed by its [DeviceType]. A new ugreen device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface UgreenCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.UGREEN_HITUNE_MAX_5C)
    fun ugreenHituneMax5c(coordinator: UgreenHituneMax5cCoordinator): DeviceCoordinator
}
