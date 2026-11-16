package nodomain.freeyourgadget.gadgetbridge.devices.igpsport

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each igpsport device type, keyed by its [DeviceType]. A new igpsport device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface IgpsportCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_BSC100MAX)
    fun igpsportBsc100max(coordinator: IGPSportBSC100MaxCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_BSC200)
    fun igpsportBsc200(coordinator: IGPSportBSC200Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_BSC200S)
    fun igpsportBsc200s(coordinator: IGPSportBSC200SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_BSC300)
    fun igpsportBsc300(coordinator: IGPSportBSC300Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_BSC300T)
    fun igpsportBsc300t(coordinator: IGPSportBSC300TCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_BSC500)
    fun igpsportBsc500(coordinator: IGPSportBSC500Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_IGS630)
    fun igpsportIgs630(coordinator: IGPSportiGS630Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_IGS630S)
    fun igpsportIgs630s(coordinator: IGPSportiGS630SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_IGS800)
    fun igpsportIgs800(coordinator: IGPSportiGS800Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_BINAVI_AIR)
    fun igpsportBinaviAir(coordinator: IGPSportBiNaviAirCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IGPSPORT_BINAVI)
    fun igpsportBinavi(coordinator: IGPSportBiNaviCoordinator): DeviceCoordinator
}
