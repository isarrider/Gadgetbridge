package nodomain.freeyourgadget.gadgetbridge.devices.qnscale

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each qnscale device type, keyed by its [DeviceType]. A new qnscale device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface QnscaleCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.QN_SCALE)
    fun qnScale(coordinator: QnScaleCoordinator): DeviceCoordinator
}
