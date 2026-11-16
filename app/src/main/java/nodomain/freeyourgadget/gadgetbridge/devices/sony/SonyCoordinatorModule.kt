package nodomain.freeyourgadget.gadgetbridge.devices.sony

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyLinkBudsCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyLinkBudsSCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWF1000XM3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWF1000XM4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWF1000XM5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWFC500Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWFC510Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWFC700NCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWFC710NCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWFSP800NCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWH1000XM2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWH1000XM3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWH1000XM4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWH1000XM5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWH1000XM6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWHCH720NCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWHULT900NCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWIC100Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.coordinators.SonyWISP600NCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.reonpocket.SonyReonPocketProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.SonyWena3Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each sony device type, keyed by its [DeviceType]. A new sony device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface SonyCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WH_1000XM3)
    fun sonyWh1000xm3(coordinator: SonyWH1000XM3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WF_SP800N)
    fun sonyWfSp800n(coordinator: SonyWFSP800NCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WI_C100)
    fun sonyWiC100(coordinator: SonyWIC100Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WI_SP600N)
    fun sonyWiSp600n(coordinator: SonyWISP600NCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WH_1000XM4)
    fun sonyWh1000xm4(coordinator: SonyWH1000XM4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WF_1000XM3)
    fun sonyWf1000xm3(coordinator: SonyWF1000XM3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WH_1000XM2)
    fun sonyWh1000xm2(coordinator: SonyWH1000XM2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WF_1000XM4)
    fun sonyWf1000xm4(coordinator: SonyWF1000XM4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_LINKBUDS)
    fun sonyLinkbuds(coordinator: SonyLinkBudsCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_LINKBUDS_S)
    fun sonyLinkbudsS(coordinator: SonyLinkBudsSCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WH_1000XM5)
    fun sonyWh1000xm5(coordinator: SonyWH1000XM5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WH_1000XM6)
    fun sonyWh1000xm6(coordinator: SonyWH1000XM6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WH_ULT900N)
    fun sonyWhUlt900n(coordinator: SonyWHULT900NCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WF_1000XM5)
    fun sonyWf1000xm5(coordinator: SonyWF1000XM5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WF_C500)
    fun sonyWfC500(coordinator: SonyWFC500Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WF_C510)
    fun sonyWfC510(coordinator: SonyWFC510Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WF_C700N)
    fun sonyWfC700n(coordinator: SonyWFC700NCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WF_C710N)
    fun sonyWfC710n(coordinator: SonyWFC710NCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WH_CH720N)
    fun sonyWhCh720n(coordinator: SonyWHCH720NCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_REON_POCKET_PRO)
    fun sonyReonPocketPro(coordinator: SonyReonPocketProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SONY_WENA_3)
    fun sonyWena3(coordinator: SonyWena3Coordinator): DeviceCoordinator
}
