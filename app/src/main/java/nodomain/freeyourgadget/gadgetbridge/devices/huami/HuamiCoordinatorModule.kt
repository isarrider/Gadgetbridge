package nodomain.freeyourgadget.gadgetbridge.devices.huami

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitband5.AmazfitBand5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbip.AmazfitBipCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbip.AmazfitBipLiteCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbip3.AmazfitBip3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbip3pro.AmazfitBip3ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbips.AmazfitBipSCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbips.AmazfitBipSLiteCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbipu.AmazfitBipUCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbipupro.AmazfitBipUProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitcor.AmazfitCorCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitcor2.AmazfitCor2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgtr.AmazfitGTRCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgtr.AmazfitGTRLiteCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgtr2.AmazfitGTR2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgtr2.AmazfitGTR2eCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgts.AmazfitGTSCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgts2.AmazfitGTS2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgts2.AmazfitGTS2MiniCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgts2.AmazfitGTS2eCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitneo.AmazfitNeoCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitpop.AmazfitPopCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitpoppro.AmazfitPopProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfittrex.AmazfitTRexCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfittrexpro.AmazfitTRexProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitvergel.AmazfitVergeLCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitx.AmazfitXCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.miband2.MiBand2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.miband2.MiBand2HRXCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.miband3.MiBand3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.miband4.MiBand4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.miband5.MiBand5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.miband6.MiBand6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppe.ZeppECoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.rings.AmazfitHelioRingCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.straps.AmazfitHelioStrapCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitActive2NfcCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitActive2RoundCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitActive2SquareCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitActive3PremiumCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitActiveEdgeCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitActiveMaxCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBalance2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBalance2XTCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBalance3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBalanceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBalanceUltraCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBand7Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBip5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBip5UnityCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBip6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitBipMaxCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitCheetah2ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitCheetah2UltraCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitCheetahProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitCheetahRoundCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitCheetahSquareCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitFalconCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitGTR3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitGTR3ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitGTR4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitGTRMiniCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitGTS3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitGTS4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitGTS4MiniCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitGTS4MiniNewCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitTRex2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitTRex3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitTRex3Pro44mmCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitTRex3Pro48mmCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitTRexUltra2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.AmazfitTRexUltraCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos.watches.MiBand7Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each huami device type, keyed by its [DeviceType]. A new huami device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface HuamiCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND2)
    fun miband2(coordinator: MiBand2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND2_HRX)
    fun miband2Hrx(coordinator: MiBand2HRXCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIP)
    fun amazfitbip(coordinator: AmazfitBipCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITCOR)
    fun amazfitcor(coordinator: AmazfitCorCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND3)
    fun miband3(coordinator: MiBand3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITCOR2)
    fun amazfitcor2(coordinator: AmazfitCor2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND4)
    fun miband4(coordinator: MiBand4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIP_LITE)
    fun amazfitbipLite(coordinator: AmazfitBipLiteCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTR)
    fun amazfitgtr(coordinator: AmazfitGTRCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTS)
    fun amazfitgts(coordinator: AmazfitGTSCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIPS)
    fun amazfitbips(coordinator: AmazfitBipSCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTR_LITE)
    fun amazfitgtrLite(coordinator: AmazfitGTRLiteCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITTREX)
    fun amazfittrex(coordinator: AmazfitTRexCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND5)
    fun miband5(coordinator: MiBand5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBAND5)
    fun amazfitband5(coordinator: AmazfitBand5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIPS_LITE)
    fun amazfitbipsLite(coordinator: AmazfitBipSLiteCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTR2)
    fun amazfitgtr2(coordinator: AmazfitGTR2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTS2)
    fun amazfitgts2(coordinator: AmazfitGTS2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIPU)
    fun amazfitbipu(coordinator: AmazfitBipUCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITVERGEL)
    fun amazfitvergel(coordinator: AmazfitVergeLCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIPUPRO)
    fun amazfitbipupro(coordinator: AmazfitBipUProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITNEO)
    fun amazfitneo(coordinator: AmazfitNeoCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTS2_MINI)
    fun amazfitgts2Mini(coordinator: AmazfitGTS2MiniCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.ZEPP_E)
    fun zeppE(coordinator: ZeppECoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTR2E)
    fun amazfitgtr2e(coordinator: AmazfitGTR2eCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTS2E)
    fun amazfitgts2e(coordinator: AmazfitGTS2eCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITX)
    fun amazfitx(coordinator: AmazfitXCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND6)
    fun miband6(coordinator: MiBand6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITTREXPRO)
    fun amazfittrexpro(coordinator: AmazfitTRexProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITPOP)
    fun amazfitpop(coordinator: AmazfitPopCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITPOPPRO)
    fun amazfitpoppro(coordinator: AmazfitPopProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND7)
    fun miband7(coordinator: MiBand7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTS3)
    fun amazfitgts3(coordinator: AmazfitGTS3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTR3)
    fun amazfitgtr3(coordinator: AmazfitGTR3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTR4)
    fun amazfitgtr4(coordinator: AmazfitGTR4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBAND7)
    fun amazfitband7(coordinator: AmazfitBand7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTS4)
    fun amazfitgts4(coordinator: AmazfitGTS4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTS4MINI)
    fun amazfitgts4mini(coordinator: AmazfitGTS4MiniCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTS4MININEW)
    fun amazfitgts4mininew(coordinator: AmazfitGTS4MiniNewCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITTREX2)
    fun amazfittrex2(coordinator: AmazfitTRex2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITTREX3)
    fun amazfittrex3(coordinator: AmazfitTRex3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITTREX3PRO44MM)
    fun amazfittrex3pro44mm(coordinator: AmazfitTRex3Pro44mmCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITTREX3PRO48MM)
    fun amazfittrex3pro48mm(coordinator: AmazfitTRex3Pro48mmCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTR3PRO)
    fun amazfitgtr3pro(coordinator: AmazfitGTR3ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIP3)
    fun amazfitbip3(coordinator: AmazfitBip3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIP3PRO)
    fun amazfitbip3pro(coordinator: AmazfitBip3ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITCHEETAHPRO)
    fun amazfitcheetahpro(coordinator: AmazfitCheetahProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITCHEETAH2PRO)
    fun amazfitcheetah2pro(coordinator: AmazfitCheetah2ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITCHEETAH2ULTRA)
    fun amazfitcheetah2ultra(coordinator: AmazfitCheetah2UltraCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITCHEETAHSQUARE)
    fun amazfitcheetahsquare(coordinator: AmazfitCheetahSquareCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITCHEETAHROUND)
    fun amazfitcheetahround(coordinator: AmazfitCheetahRoundCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIP5)
    fun amazfitbip5(coordinator: AmazfitBip5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIP5UNITY)
    fun amazfitbip5unity(coordinator: AmazfitBip5UnityCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIP6)
    fun amazfitbip6(coordinator: AmazfitBip6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBIPMAX)
    fun amazfitbipmax(coordinator: AmazfitBipMaxCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITTREXULTRA)
    fun amazfittrexultra(coordinator: AmazfitTRexUltraCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITTREXULTRA2)
    fun amazfittrexultra2(coordinator: AmazfitTRexUltra2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITGTRMINI)
    fun amazfitgtrmini(coordinator: AmazfitGTRMiniCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITFALCON)
    fun amazfitfalcon(coordinator: AmazfitFalconCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBALANCE)
    fun amazfitbalance(coordinator: AmazfitBalanceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBALANCE2)
    fun amazfitbalance2(coordinator: AmazfitBalance2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBALANCE2XT)
    fun amazfitbalance2xt(coordinator: AmazfitBalance2XTCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBALANCE3)
    fun amazfitbalance3(coordinator: AmazfitBalance3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITBALANCEULTRA)
    fun amazfitbalanceultra(coordinator: AmazfitBalanceUltraCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITACTIVE)
    fun amazfitactive(coordinator: AmazfitActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITACTIVE2)
    fun amazfitactive2(coordinator: AmazfitActive2RoundCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITACTIVE2NFC)
    fun amazfitactive2nfc(coordinator: AmazfitActive2NfcCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITACTIVE2SQUARE)
    fun amazfitactive2square(coordinator: AmazfitActive2SquareCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITACTIVE3PREMIUM)
    fun amazfitactive3premium(coordinator: AmazfitActive3PremiumCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITACTIVEEDGE)
    fun amazfitactiveedge(coordinator: AmazfitActiveEdgeCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITACTIVEMAX)
    fun amazfitactivemax(coordinator: AmazfitActiveMaxCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITHELIORING)
    fun amazfithelioring(coordinator: AmazfitHelioRingCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.AMAZFITHELIOSTRAP)
    fun amazfitheliostrap(coordinator: AmazfitHelioStrapCoordinator): DeviceCoordinator
}
