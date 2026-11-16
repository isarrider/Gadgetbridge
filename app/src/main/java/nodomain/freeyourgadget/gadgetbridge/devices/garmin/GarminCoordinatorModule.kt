package nodomain.freeyourgadget.gadgetbridge.devices.garmin

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge1000Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge1030Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge1030PlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge1040Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge1050Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge130Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge130PlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge25Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge510Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge520Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge520PlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge530Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge540Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge550Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge810Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge820Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge830Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge840Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdge850Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdgeExplore1000Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdgeExplore2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdgeExplore820Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdgeExploreCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.bike.GarminEdgeMtbCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.gps.GarminETrexSeCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.gps.GarminGpsmap66iCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.gps.GarminGpsmap66sCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.gps.GarminGpsmap66srCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.gps.GarminGpsmapH1Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.gps.GarminInReachMini2
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.gps.GarminMontana700Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.hrm.GarminHrm200Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.hrm.GarminHrm600Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.hrm.GarminHrmProPlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.cirqa.GarminCirqaCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.d2.GarminD2AirCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.d2.GarminD2AirX10Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.d2.GarminD2AirX15Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.d2.GarminD2Mach1Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.d2.GarminD2Mach1ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.d2.GarminD2Mach2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.d2.GarminD2Mach2ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.descent.GarminDescentG1Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.descent.GarminDescentG2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.descent.GarminDescentMk3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.descent.GarminDescentMk3iCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.enduro.GarminEnduro2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.enduro.GarminEnduro3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.enduro.GarminEnduro4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.enduro.GarminEnduroCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.epix.GarminEpixCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.epix.GarminEpixGen2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.epix.GarminEpixProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix3ChronosCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix3HrCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix5PlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix5SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix5SPlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix5XCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix5XPlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6SProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6SProSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6SSapphireCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6SSportCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6SapphireCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6SportCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6XCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6XProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6XProSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix6XSapphireCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7ProSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7SProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7SProSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7XCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7XProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix7XProSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix8Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix8ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix8SolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix9Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix9ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenix9ProSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.fenix.GarminFenixECoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner165Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner165MusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner170Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner170MusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner220Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner225Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner230Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner235Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner245Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner245MusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner255Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner255MusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner255SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner255SMusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner25Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner265Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner265SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner30Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner35Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner45Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner45PlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner55Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner570Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner620Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner630Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner645Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner645MusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner70Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner735XTCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner745Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner920Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner920XTCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner935Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner945Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner945LTECoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner955Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner965Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.forerunner.GarminForerunner970Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2SSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2SSurfCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2SolTacCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2SolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2XCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2XSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct2XTacticalCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct3SolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct3SolarTacticalCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinct3TacticalCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinctCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinctCrossoverCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinctECoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinctSolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinctSolarTacticalCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.instinct.GarminInstinctTacticalCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.lily.GarminLily2ActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.lily.GarminLily2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.lily.GarminLilyCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.quatix.GarminQuatix6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.quatix.GarminQuatix6XCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.quatix.GarminQuatix7Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.quatix.GarminQuatix7XCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.quatix.GarminQuatix8Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.swim.GarminSwim2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.tactix.GarminTactix7Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.tactix.GarminTactix8Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.tactix.GarminTactix8SolarCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenu2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenu2PlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenu2SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenu3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenu3SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenu4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenuCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenuSq2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenuSq2MusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenuSqCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenuSqMusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenuX1Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActive3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActive3MusicCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActive3TCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActive4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActive4SCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActive5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActive6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive.GarminVivoActiveHrCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivomove.GarminVivomoveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivomove.GarminVivomoveHrCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivomove.GarminVivomoveSportCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivomove.GarminVivomoveStyleCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivomove.GarminVivomoveTrendCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivosmart.GarminVivosmart3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivosmart.GarminVivosmart4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivosmart.GarminVivosmart5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivosmart.GarminVivosmartCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivosmart.GarminVivosmartHrCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivosmart.GarminVivosmartHrPlusCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivosport.GarminVivosportCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each garmin device type, keyed by its [DeviceType]. A new garmin device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GarminCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.VIVOMOVE_HR)
    fun vivomoveHr(coordinator: GarminVivomoveHrCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_HRM_PRO_PLUS)
    fun garminHrmProPlus(coordinator: GarminHrmProPlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_HRM_200)
    fun garminHrm200(coordinator: GarminHrm200Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_HRM_600)
    fun garminHrm600(coordinator: GarminHrm600Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_25)
    fun garminEdge25(coordinator: GarminEdge25Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_130)
    fun garminEdge130(coordinator: GarminEdge130Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_130_PLUS)
    fun garminEdge130Plus(coordinator: GarminEdge130PlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_510)
    fun garminEdge510(coordinator: GarminEdge510Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_520)
    fun garminEdge520(coordinator: GarminEdge520Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_520_PLUS)
    fun garminEdge520Plus(coordinator: GarminEdge520PlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_530)
    fun garminEdge530(coordinator: GarminEdge530Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_540)
    fun garminEdge540(coordinator: GarminEdge540Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_550)
    fun garminEdge550(coordinator: GarminEdge550Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_810)
    fun garminEdge810(coordinator: GarminEdge810Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_820)
    fun garminEdge820(coordinator: GarminEdge820Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_830)
    fun garminEdge830(coordinator: GarminEdge830Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_840)
    fun garminEdge840(coordinator: GarminEdge840Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_850)
    fun garminEdge850(coordinator: GarminEdge850Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_1000)
    fun garminEdge1000(coordinator: GarminEdge1000Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_1030)
    fun garminEdge1030(coordinator: GarminEdge1030Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_1030_PLUS)
    fun garminEdge1030Plus(coordinator: GarminEdge1030PlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_1040)
    fun garminEdge1040(coordinator: GarminEdge1040Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_1050)
    fun garminEdge1050(coordinator: GarminEdge1050Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_EXPLORE)
    fun garminEdgeExplore(coordinator: GarminEdgeExploreCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_EXPLORE_2)
    fun garminEdgeExplore2(coordinator: GarminEdgeExplore2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_EXPLORE_820)
    fun garminEdgeExplore820(coordinator: GarminEdgeExplore820Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_EXPLORE_1000)
    fun garminEdgeExplore1000(coordinator: GarminEdgeExplore1000Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EDGE_MTB)
    fun garminEdgeMtb(coordinator: GarminEdgeMtbCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_GPSMAP_66I)
    fun garminGpsmap66i(coordinator: GarminGpsmap66iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_GPSMAP_66S)
    fun garminGpsmap66s(coordinator: GarminGpsmap66sCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_GPSMAP_66SR)
    fun garminGpsmap66sr(coordinator: GarminGpsmap66srCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_GPSMAP_H1)
    fun garminGpsmapH1(coordinator: GarminGpsmapH1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_ETREX_SE)
    fun garminEtrexSe(coordinator: GarminETrexSeCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_MONTANA_700)
    fun garminMontana700(coordinator: GarminMontana700Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_ENDURO)
    fun garminEnduro(coordinator: GarminEnduroCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_ENDURO_2)
    fun garminEnduro2(coordinator: GarminEnduro2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_ENDURO_3)
    fun garminEnduro3(coordinator: GarminEnduro3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_ENDURO_4)
    fun garminEnduro4(coordinator: GarminEnduro4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EPIX)
    fun garminEpix(coordinator: GarminEpixCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EPIX_GEN2)
    fun garminEpixGen2(coordinator: GarminEpixGen2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_EPIX_PRO)
    fun garminEpixPro(coordinator: GarminEpixProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_DESCENT_MK3)
    fun garminDescentMk3(coordinator: GarminDescentMk3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_DESCENT_MK3I)
    fun garminDescentMk3i(coordinator: GarminDescentMk3iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_DESCENT_G1)
    fun garminDescentG1(coordinator: GarminDescentG1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_DESCENT_G2)
    fun garminDescentG2(coordinator: GarminDescentG2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_2)
    fun garminFenix2(coordinator: GarminFenix2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_3)
    fun garminFenix3(coordinator: GarminFenix3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_3_CHRONOS)
    fun garminFenix3Chronos(coordinator: GarminFenix3ChronosCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_3_HR)
    fun garminFenix3Hr(coordinator: GarminFenix3HrCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_5)
    fun garminFenix5(coordinator: GarminFenix5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_5S)
    fun garminFenix5s(coordinator: GarminFenix5SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_5S_PLUS)
    fun garminFenix5sPlus(coordinator: GarminFenix5SPlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_5_PLUS)
    fun garminFenix5Plus(coordinator: GarminFenix5PlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_5X)
    fun garminFenix5x(coordinator: GarminFenix5XCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_5X_PLUS)
    fun garminFenix5xPlus(coordinator: GarminFenix5XPlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6)
    fun garminFenix6(coordinator: GarminFenix6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6_SPORT)
    fun garminFenix6Sport(coordinator: GarminFenix6SportCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6_PRO)
    fun garminFenix6Pro(coordinator: GarminFenix6ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6_SAPPHIRE)
    fun garminFenix6Sapphire(coordinator: GarminFenix6SapphireCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6S_PRO)
    fun garminFenix6sPro(coordinator: GarminFenix6SProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6S)
    fun garminFenix6s(coordinator: GarminFenix6SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6S_SPORT)
    fun garminFenix6sSport(coordinator: GarminFenix6SSportCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6S_PRO_SOLAR)
    fun garminFenix6sProSolar(coordinator: GarminFenix6SProSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6S_SAPPHIRE)
    fun garminFenix6sSapphire(coordinator: GarminFenix6SSapphireCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6X)
    fun garminFenix6x(coordinator: GarminFenix6XCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6X_PRO)
    fun garminFenix6xPro(coordinator: GarminFenix6XProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6X_PRO_SOLAR)
    fun garminFenix6xProSolar(coordinator: GarminFenix6XProSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_6X_SAPPHIRE)
    fun garminFenix6xSapphire(coordinator: GarminFenix6XSapphireCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7)
    fun garminFenix7(coordinator: GarminFenix7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7S)
    fun garminFenix7s(coordinator: GarminFenix7SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7S_PRO)
    fun garminFenix7sPro(coordinator: GarminFenix7SProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7S_PRO_SOLAR)
    fun garminFenix7sProSolar(coordinator: GarminFenix7SProSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7X)
    fun garminFenix7x(coordinator: GarminFenix7XCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7_PRO)
    fun garminFenix7Pro(coordinator: GarminFenix7ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7_PRO_SOLAR)
    fun garminFenix7ProSolar(coordinator: GarminFenix7ProSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7X_PRO)
    fun garminFenix7xPro(coordinator: GarminFenix7XProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_7X_PRO_SOLAR)
    fun garminFenix7xProSolar(coordinator: GarminFenix7XProSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_8)
    fun garminFenix8(coordinator: GarminFenix8Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_8_SOLAR)
    fun garminFenix8Solar(coordinator: GarminFenix8SolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_8_PRO)
    fun garminFenix8Pro(coordinator: GarminFenix8ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_9)
    fun garminFenix9(coordinator: GarminFenix9Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_9_PRO)
    fun garminFenix9Pro(coordinator: GarminFenix9ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_9_PRO_SOLAR)
    fun garminFenix9ProSolar(coordinator: GarminFenix9ProSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FENIX_E)
    fun garminFenixE(coordinator: GarminFenixECoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_25)
    fun garminForerunner25(coordinator: GarminForerunner25Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_30)
    fun garminForerunner30(coordinator: GarminForerunner30Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_35)
    fun garminForerunner35(coordinator: GarminForerunner35Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_45)
    fun garminForerunner45(coordinator: GarminForerunner45Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_45_PLUS)
    fun garminForerunner45Plus(coordinator: GarminForerunner45PlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_55)
    fun garminForerunner55(coordinator: GarminForerunner55Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_165)
    fun garminForerunner165(coordinator: GarminForerunner165Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_165_MUSIC)
    fun garminForerunner165Music(coordinator: GarminForerunner165MusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_170)
    fun garminForerunner170(coordinator: GarminForerunner170Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_170_MUSIC)
    fun garminForerunner170Music(coordinator: GarminForerunner170MusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_220)
    fun garminForerunner220(coordinator: GarminForerunner220Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_225)
    fun garminForerunner225(coordinator: GarminForerunner225Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_230)
    fun garminForerunner230(coordinator: GarminForerunner230Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_235)
    fun garminForerunner235(coordinator: GarminForerunner235Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_245)
    fun garminForerunner245(coordinator: GarminForerunner245Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_245_MUSIC)
    fun garminForerunner245Music(coordinator: GarminForerunner245MusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_255)
    fun garminForerunner255(coordinator: GarminForerunner255Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_255_MUSIC)
    fun garminForerunner255Music(coordinator: GarminForerunner255MusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_255S)
    fun garminForerunner255s(coordinator: GarminForerunner255SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_255S_MUSIC)
    fun garminForerunner255sMusic(coordinator: GarminForerunner255SMusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_265)
    fun garminForerunner265(coordinator: GarminForerunner265Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_265S)
    fun garminForerunner265s(coordinator: GarminForerunner265SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_570)
    fun garminForerunner570(coordinator: GarminForerunner570Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_620)
    fun garminForerunner620(coordinator: GarminForerunner620Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_630)
    fun garminForerunner630(coordinator: GarminForerunner630Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_645)
    fun garminForerunner645(coordinator: GarminForerunner645Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_645_MUSIC)
    fun garminForerunner645Music(coordinator: GarminForerunner645MusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_70)
    fun garminForerunner70(coordinator: GarminForerunner70Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_735XT)
    fun garminForerunner735xt(coordinator: GarminForerunner735XTCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_745)
    fun garminForerunner745(coordinator: GarminForerunner745Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_920)
    fun garminForerunner920(coordinator: GarminForerunner920Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_920XT)
    fun garminForerunner920xt(coordinator: GarminForerunner920XTCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_935)
    fun garminForerunner935(coordinator: GarminForerunner935Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_945)
    fun garminForerunner945(coordinator: GarminForerunner945Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_945_LTE)
    fun garminForerunner945Lte(coordinator: GarminForerunner945LTECoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_955)
    fun garminForerunner955(coordinator: GarminForerunner955Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_965)
    fun garminForerunner965(coordinator: GarminForerunner965Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_FORERUNNER_970)
    fun garminForerunner970(coordinator: GarminForerunner970Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_QUATIX_6)
    fun garminQuatix6(coordinator: GarminQuatix6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_QUATIX_6X)
    fun garminQuatix6x(coordinator: GarminQuatix6XCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_QUATIX_7)
    fun garminQuatix7(coordinator: GarminQuatix7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_QUATIX_7X)
    fun garminQuatix7x(coordinator: GarminQuatix7XCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_QUATIX_8)
    fun garminQuatix8(coordinator: GarminQuatix8Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_TACTIX_7)
    fun garminTactix7(coordinator: GarminTactix7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_TACTIX_8)
    fun garminTactix8(coordinator: GarminTactix8Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_TACTIX_8_SOLAR)
    fun garminTactix8Solar(coordinator: GarminTactix8SolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_SWIM_2)
    fun garminSwim2(coordinator: GarminSwim2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INREACH_MINI_2)
    fun garminInreachMini2(coordinator: GarminInReachMini2): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT)
    fun garminInstinct(coordinator: GarminInstinctCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_SOLAR)
    fun garminInstinctSolar(coordinator: GarminInstinctSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_TACTICAL)
    fun garminInstinctTactical(coordinator: GarminInstinctTacticalCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_SOLAR_TACTICAL)
    fun garminInstinctSolarTactical(coordinator: GarminInstinctSolarTacticalCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2)
    fun garminInstinct2(coordinator: GarminInstinct2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2S)
    fun garminInstinct2s(coordinator: GarminInstinct2SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2S_SOLAR)
    fun garminInstinct2sSolar(coordinator: GarminInstinct2SSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2S_SURF)
    fun garminInstinct2sSurf(coordinator: GarminInstinct2SSurfCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2X)
    fun garminInstinct2x(coordinator: GarminInstinct2XCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2X_SOLAR)
    fun garminInstinct2xSolar(coordinator: GarminInstinct2XSolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2X_TACTICAL)
    fun garminInstinct2xTactical(coordinator: GarminInstinct2XTacticalCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2_SOLAR)
    fun garminInstinct2Solar(coordinator: GarminInstinct2SolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_2_SOLTAC)
    fun garminInstinct2Soltac(coordinator: GarminInstinct2SolTacCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_CROSSOVER)
    fun garminInstinctCrossover(coordinator: GarminInstinctCrossoverCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_E)
    fun garminInstinctE(coordinator: GarminInstinctECoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_3)
    fun garminInstinct3(coordinator: GarminInstinct3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_3_SOLAR)
    fun garminInstinct3Solar(coordinator: GarminInstinct3SolarCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_3_SOLAR_TACTICAL)
    fun garminInstinct3SolarTactical(coordinator: GarminInstinct3SolarTacticalCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_INSTINCT_3_TACTICAL)
    fun garminInstinct3Tactical(coordinator: GarminInstinct3TacticalCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_LILY)
    fun garminLily(coordinator: GarminLilyCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_LILY_2)
    fun garminLily2(coordinator: GarminLily2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_LILY_2_ACTIVE)
    fun garminLily2Active(coordinator: GarminLily2ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOMOVE)
    fun garminVivomove(coordinator: GarminVivomoveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOMOVE_SPORT)
    fun garminVivomoveSport(coordinator: GarminVivomoveSportCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOMOVE_STYLE)
    fun garminVivomoveStyle(coordinator: GarminVivomoveStyleCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOMOVE_TREND)
    fun garminVivomoveTrend(coordinator: GarminVivomoveTrendCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU)
    fun garminVenu(coordinator: GarminVenuCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_SQ)
    fun garminVenuSq(coordinator: GarminVenuSqCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_SQ_MUSIC)
    fun garminVenuSqMusic(coordinator: GarminVenuSqMusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_SQ_2)
    fun garminVenuSq2(coordinator: GarminVenuSq2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_SQ_2_MUSIC)
    fun garminVenuSq2Music(coordinator: GarminVenuSq2MusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_2)
    fun garminVenu2(coordinator: GarminVenu2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_2S)
    fun garminVenu2s(coordinator: GarminVenu2SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_2_PLUS)
    fun garminVenu2Plus(coordinator: GarminVenu2PlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_3)
    fun garminVenu3(coordinator: GarminVenu3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_3S)
    fun garminVenu3s(coordinator: GarminVenu3SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_4)
    fun garminVenu4(coordinator: GarminVenu4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VENU_X1)
    fun garminVenuX1(coordinator: GarminVenuX1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE)
    fun garminVivoactive(coordinator: GarminVivoActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE_3)
    fun garminVivoactive3(coordinator: GarminVivoActive3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE_3_MUSIC)
    fun garminVivoactive3Music(coordinator: GarminVivoActive3MusicCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE_3T)
    fun garminVivoactive3t(coordinator: GarminVivoActive3TCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE_4)
    fun garminVivoactive4(coordinator: GarminVivoActive4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE_4S)
    fun garminVivoactive4s(coordinator: GarminVivoActive4SCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE_5)
    fun garminVivoactive5(coordinator: GarminVivoActive5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE_6)
    fun garminVivoactive6(coordinator: GarminVivoActive6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOACTIVE_HR)
    fun garminVivoactiveHr(coordinator: GarminVivoActiveHrCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOSMART)
    fun garminVivosmart(coordinator: GarminVivosmartCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOSMART_HR)
    fun garminVivosmartHr(coordinator: GarminVivosmartHrCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOSMART_HR_PLUS)
    fun garminVivosmartHrPlus(coordinator: GarminVivosmartHrPlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOSMART_3)
    fun garminVivosmart3(coordinator: GarminVivosmart3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOSMART_4)
    fun garminVivosmart4(coordinator: GarminVivosmart4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOSMART_5)
    fun garminVivosmart5(coordinator: GarminVivosmart5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_VIVOSPORT)
    fun garminVivosport(coordinator: GarminVivosportCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_CIRQA)
    fun garminCirqa(coordinator: GarminCirqaCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_D2_AIR)
    fun garminD2Air(coordinator: GarminD2AirCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_D2_AIR_X10)
    fun garminD2AirX10(coordinator: GarminD2AirX10Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_D2_AIR_X15)
    fun garminD2AirX15(coordinator: GarminD2AirX15Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_D2_MACH_1)
    fun garminD2Mach1(coordinator: GarminD2Mach1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_D2_MACH_1_PRO)
    fun garminD2Mach1Pro(coordinator: GarminD2Mach1ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_D2_MACH_2)
    fun garminD2Mach2(coordinator: GarminD2Mach2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARMIN_D2_MACH_2_PRO)
    fun garminD2Mach2Pro(coordinator: GarminD2Mach2ProCoordinator): DeviceCoordinator
}
