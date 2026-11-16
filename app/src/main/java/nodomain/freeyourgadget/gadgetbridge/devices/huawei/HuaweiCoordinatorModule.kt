package nodomain.freeyourgadget.gadgetbridge.devices.huawei

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.freearc.HuaweiFreearcCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.freebuds5i.HuaweiFreebuds5iCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.freebuds6.HuaweiFreebuds6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.freebudspro.HuaweiFreebudsProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.freeclip.HuaweiFreeClip2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband10.HonorBand10Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband3.HonorBand3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband4.HonorBand4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband5.HonorBand5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband6.HonorBand6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband7.HonorBand7Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband9.HonorBand9Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honormagicwatch.HonorMagicWatchCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honormagicwatch2.HonorMagicWatch2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorwatch4.HonorWatch4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorwatch5.HonorWatch5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorwatch5pro.HonorWatch5ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorwatch6.HonorWatch6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorwatchgs3.HonorWatchGS3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorwatchgspro.HonorWatchGSProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband10.HuaweiBand10Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband11.HuaweiBand11Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband11pro.HuaweiBand11ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband2pro.HuaweiBand2ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband3pro.HuaweiBand3ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband4pro.HuaweiBand4ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband6.HuaweiBand6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband7.HuaweiBand7Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband8.HuaweiBand8Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiband9.HuaweiBand9Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweibanda2.HuaweiBandA2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweibandaw70.HuaweiBandAw70Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweitalkbandb6.HuaweiTalkBandB6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweitalkbandb7.HuaweiTalkBandB7Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatch3.HuaweiWatch3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatch4pro.HuaweiWatch4ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatch5.HuaweiWatch5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatch6.HuaweiWatch6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatch6.HuaweiWatch6ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchd2.HuaweiWatchD2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchd3.HuaweiWatchD3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchfit.HuaweiWatchFitCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchfit2.HuaweiWatchFit2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchfit3.HuaweiWatchFit3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchfit4pro.HuaweiWatchFit4ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchfit5.HuaweiWatchFit5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchfit5pro.HuaweiWatchFit5ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt.HuaweiWatchGTCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt2.HuaweiWatchGT2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt2e.HuaweiWatchGT2eCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt3.HuaweiWatchGT3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt4.HuaweiWatchGT4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt5.HuaweiWatchGT5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt6.HuaweiWatchGT6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt7.HuaweiWatchGT7Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgt7.HuaweiWatchGT7ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgtcyber.HuaweiWatchGTCyberCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgtrunner.HuaweiWatchGTRunnerCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchgtrunner2.HuaweiWatchGTRunner2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchultimate.HuaweiWatchUltimateCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweiwatchultimate2.HuaweiWatchUltimate2Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each huawei device type, keyed by its [DeviceType]. A new huawei device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface HuaweiCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORBAND3)
    fun honorband3(coordinator: HonorBand3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORBAND4)
    fun honorband4(coordinator: HonorBand4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORBAND5)
    fun honorband5(coordinator: HonorBand5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBANDAW70)
    fun huaweibandaw70(coordinator: HuaweiBandAw70Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBANDA2)
    fun huaweibanda2(coordinator: HuaweiBandA2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND6)
    fun huaweiband6(coordinator: HuaweiBand6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT)
    fun huaweiwatchgt(coordinator: HuaweiWatchGTCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND2PRO)
    fun huaweiband2pro(coordinator: HuaweiBand2ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND3PRO)
    fun huaweiband3pro(coordinator: HuaweiBand3ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND4PRO)
    fun huaweiband4pro(coordinator: HuaweiBand4ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT2)
    fun huaweiwatchgt2(coordinator: HuaweiWatchGT2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT2E)
    fun huaweiwatchgt2e(coordinator: HuaweiWatchGT2eCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEITALKBANDB6)
    fun huaweitalkbandb6(coordinator: HuaweiTalkBandB6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEITALKBANDB7)
    fun huaweitalkbandb7(coordinator: HuaweiTalkBandB7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND7)
    fun huaweiband7(coordinator: HuaweiBand7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORBAND6)
    fun honorband6(coordinator: HonorBand6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORBAND7)
    fun honorband7(coordinator: HonorBand7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORBAND9)
    fun honorband9(coordinator: HonorBand9Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORBAND10)
    fun honorband10(coordinator: HonorBand10Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORWATCH4)
    fun honorwatch4(coordinator: HonorWatch4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORWATCH5)
    fun honorwatch5(coordinator: HonorWatch5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORWATCH5PRO)
    fun honorwatch5pro(coordinator: HonorWatch5ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORWATCH6)
    fun honorwatch6(coordinator: HonorWatch6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORMAGICWATCH)
    fun honormagicwatch(coordinator: HonorMagicWatchCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORMAGICWATCH2)
    fun honormagicwatch2(coordinator: HonorMagicWatch2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORWATCHGS3)
    fun honorwatchgs3(coordinator: HonorWatchGS3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HONORWATCHGSPRO)
    fun honorwatchgspro(coordinator: HonorWatchGSProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHD2)
    fun huaweiwatchd2(coordinator: HuaweiWatchD2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHD3)
    fun huaweiwatchd3(coordinator: HuaweiWatchD3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT3)
    fun huaweiwatchgt3(coordinator: HuaweiWatchGT3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT4)
    fun huaweiwatchgt4(coordinator: HuaweiWatchGT4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT5)
    fun huaweiwatchgt5(coordinator: HuaweiWatchGT5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT6)
    fun huaweiwatchgt6(coordinator: HuaweiWatchGT6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT7)
    fun huaweiwatchgt7(coordinator: HuaweiWatchGT7Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGT7PRO)
    fun huaweiwatchgt7pro(coordinator: HuaweiWatchGT7ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGTRUNNER)
    fun huaweiwatchgtrunner(coordinator: HuaweiWatchGTRunnerCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGTRUNNER2)
    fun huaweiwatchgtrunner2(coordinator: HuaweiWatchGTRunner2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHGTCYBER)
    fun huaweiwatchgtcyber(coordinator: HuaweiWatchGTCyberCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND8)
    fun huaweiband8(coordinator: HuaweiBand8Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND9)
    fun huaweiband9(coordinator: HuaweiBand9Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND10)
    fun huaweiband10(coordinator: HuaweiBand10Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND11)
    fun huaweiband11(coordinator: HuaweiBand11Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIBAND11PRO)
    fun huaweiband11pro(coordinator: HuaweiBand11ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHFIT)
    fun huaweiwatchfit(coordinator: HuaweiWatchFitCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHFIT2)
    fun huaweiwatchfit2(coordinator: HuaweiWatchFit2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHFIT3)
    fun huaweiwatchfit3(coordinator: HuaweiWatchFit3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHFIT4PRO)
    fun huaweiwatchfit4pro(coordinator: HuaweiWatchFit4ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHFIT5)
    fun huaweiwatchfit5(coordinator: HuaweiWatchFit5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHFIT5PRO)
    fun huaweiwatchfit5pro(coordinator: HuaweiWatchFit5ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHULTIMATE)
    fun huaweiwatchultimate(coordinator: HuaweiWatchUltimateCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCHULTIMATE2)
    fun huaweiwatchultimate2(coordinator: HuaweiWatchUltimate2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCH3)
    fun huaweiwatch3(coordinator: HuaweiWatch3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCH4PRO)
    fun huaweiwatch4pro(coordinator: HuaweiWatch4ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCH5)
    fun huaweiwatch5(coordinator: HuaweiWatch5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCH6)
    fun huaweiwatch6(coordinator: HuaweiWatch6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEIWATCH6PRO)
    fun huaweiwatch6pro(coordinator: HuaweiWatch6ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEI_FREEARC)
    fun huaweiFreearc(coordinator: HuaweiFreearcCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEI_FREECLIP2)
    fun huaweiFreeclip2(coordinator: HuaweiFreeClip2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEI_FREEBUDS5I)
    fun huaweiFreebuds5i(coordinator: HuaweiFreebuds5iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEI_FREEBUDS_PRO)
    fun huaweiFreebudsPro(coordinator: HuaweiFreebudsProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.HUAWEI_FREEBUDS6)
    fun huaweiFreebuds6(coordinator: HuaweiFreebuds6Coordinator): DeviceCoordinator
}
