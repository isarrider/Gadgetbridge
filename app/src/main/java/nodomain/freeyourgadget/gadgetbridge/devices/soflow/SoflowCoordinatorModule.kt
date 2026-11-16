package nodomain.freeyourgadget.gadgetbridge.devices.soflow

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each soflow device type, keyed by its [DeviceType]. A new soflow device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface SoflowCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOFLOW_SO6)
    fun soflowSo6(coordinator: SoFlowCoordinator): DeviceCoordinator
}
