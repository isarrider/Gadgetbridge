package nodomain.freeyourgadget.gadgetbridge.devices.mijia_lywsd

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each mijia_lywsd device type, keyed by its [DeviceType]. A new mijia_lywsd device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface MijiaLywsdCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIJIA_LYWSD02)
    fun mijiaLywsd02(coordinator: MijiaLywsd02Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIJIA_LYWSD03)
    fun mijiaLywsd03(coordinator: MijiaLywsd03Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIJIA_XMWSDJ04)
    fun mijiaXmwsdj04(coordinator: MijiaXmwsdj04Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIJIA_MHO_C303)
    fun mijiaMhoC303(coordinator: MijiaMhoC303Coordinator): DeviceCoordinator
}
