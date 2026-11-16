package nodomain.freeyourgadget.gadgetbridge.devices.moyoung

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each moyoung device type, keyed by its [DeviceType]. A new moyoung device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface MoyoungCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.P22B1)
    fun p22b1(coordinator: P22B1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_I28_ULTRA)
    fun colmiI28Ultra(coordinator: ColmiI28UltraCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_P76)
    fun colmiP76(coordinator: ColmiP76Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_P80)
    fun colmiP80(coordinator: ColmiP80Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_P81)
    fun colmiP81(coordinator: ColmiP81Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_V72)
    fun colmiV72(coordinator: ColmiV72Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_V73)
    fun colmiV73(coordinator: ColmiV73Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_V76)
    fun colmiV76(coordinator: ColmiV76Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_V89)
    fun colmiV89(coordinator: ColmiV89Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ALT_VIBE)
    fun altVibe(coordinator: AltVibeCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BLACKVIEW_R60)
    fun blackviewR60(coordinator: BlackviewR60Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MISIRUN_C17)
    fun misirunC17(coordinator: MisirunC17Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.C20)
    fun c20(coordinator: C20Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.C20PRO)
    fun c20pro(coordinator: C20ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.L70)
    fun l70(coordinator: L70Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HC96)
    fun hc96(coordinator: HC96Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.KW19_B)
    fun kw19B(coordinator: KW19BCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.KT80)
    fun kt80(coordinator: KT80Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ITECH_ACTIVE_3)
    fun itechActive3(coordinator: ITechActive3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.KSIX_VENTURE)
    fun ksixVenture(coordinator: KsixVentureCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.FIREBOLTT_069)
    fun fireboltt069(coordinator: FireBoltt069Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.FIREBOLTT_TALK)
    fun firebolttTalk(coordinator: FireBolttTalkCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.FIREBOLTT_NINJA_PHANTOM)
    fun firebolttNinjaPhantom(coordinator: FireBolttNinjaPhantomCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.VIRAN_C29)
    fun viranC29(coordinator: ViranC29Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ZL02D)
    fun zl02d(coordinator: ZL02DCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.R50PRO)
    fun r50pro(coordinator: R50ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.RAINBUVVY_T97)
    fun rainbuvvyT97(coordinator: RainbuvvyT97Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MT55)
    fun mt55(coordinator: MT55Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GARETT_EVA)
    fun garettEva(coordinator: GarettEvaCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ADVAN_WATCH_SE1_AI)
    fun advanWatchSe1Ai(coordinator: AdvanWatchSE1AICoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.BETTDOW_FB051)
    fun bettdowFb051(coordinator: BettdowFB051Coordinator): DeviceCoordinator
}
