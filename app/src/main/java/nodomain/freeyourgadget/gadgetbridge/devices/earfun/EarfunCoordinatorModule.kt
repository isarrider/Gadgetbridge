package nodomain.freeyourgadget.gadgetbridge.devices.earfun

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each earfun device type, keyed by its [DeviceType]. A new earfun device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface EarfunCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.EARFUN_AIR_S)
    fun earfunAirS(coordinator: EarFunAirSCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.EARFUN_AIR_PRO_4)
    fun earfunAirPro4(coordinator: EarFunAirPro4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.EARFUN_FREE_PRO_3)
    fun earfunFreePro3(coordinator: EarFunFreePro3Coordinator): DeviceCoordinator
}
