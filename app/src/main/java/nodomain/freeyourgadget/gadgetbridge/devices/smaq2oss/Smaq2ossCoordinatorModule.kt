package nodomain.freeyourgadget.gadgetbridge.devices.smaq2oss

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each smaq2oss device type, keyed by its [DeviceType]. A new smaq2oss device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface Smaq2ossCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SMAQ2OSS)
    fun smaq2oss(coordinator: SMAQ2OSSCoordinator): DeviceCoordinator
}
