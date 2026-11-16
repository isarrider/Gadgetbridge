package nodomain.freeyourgadget.gadgetbridge.devices.gloryfit

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.BlackviewR3MaxCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.ColmiP10Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.D3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.DotnP66DCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.GrvFc1Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.HaylouWatch2ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.HiFutureZone2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.ImikiFrame2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.KieslectCallingSmartwatchKrProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.OukitelBT103Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.QecnatoS10Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.R1Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.S52Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.SwissPeakCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.T70Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.Y66Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.gloryfit.watches.Y6Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each gloryfit device type, keyed by its [DeviceType]. A new gloryfit device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface GloryfitCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.OUKITEL_BT103)
    fun oukitelBt103(coordinator: OukitelBT103Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GLORYFIT_SWISS_PEAK)
    fun gloryfitSwissPeak(coordinator: SwissPeakCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BLACKVIEW_R3MAX)
    fun blackviewR3max(coordinator: BlackviewR3MaxCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.DOTN_P66D)
    fun dotnP66d(coordinator: DotnP66DCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.QECNATO_S10)
    fun qecnatoS10(coordinator: QecnatoS10Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.R1)
    fun r1(coordinator: R1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GRV_FC1)
    fun grvFc1(coordinator: GrvFc1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.IMIKI_FRAME_2)
    fun imikiFrame2(coordinator: ImikiFrame2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.KIESLECT_CALLING_SMARTWATCH_KR_PRO)
    fun kieslectCallingSmartwatchKrPro(coordinator: KieslectCallingSmartwatchKrProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HAYLOU_WATCH_2_PRO)
    fun haylouWatch2Pro(coordinator: HaylouWatch2ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.S52)
    fun s52(coordinator: S52Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HIFUTURE_ZONE2)
    fun hifutureZone2(coordinator: HiFutureZone2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.D3)
    fun d3(coordinator: D3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.Y6)
    fun y6(coordinator: Y6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.Y66)
    fun y66(coordinator: Y66Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.T70)
    fun t70(coordinator: T70Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_P10)
    fun colmiP10(coordinator: ColmiP10Coordinator): DeviceCoordinator
}
