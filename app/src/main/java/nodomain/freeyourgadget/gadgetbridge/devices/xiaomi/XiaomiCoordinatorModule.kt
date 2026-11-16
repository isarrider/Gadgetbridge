package nodomain.freeyourgadget.gadgetbridge.devices.xiaomi

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.magic_touch.MagicTouchJj02Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand10Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand10ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand11ActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand11Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand4CCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand7ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand8ActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand8Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand8ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand9ActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand9Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiBand9ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiWatchColorSportCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.MiWatchLiteCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiSmartBand2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiSmartBand3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiSmartBandProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch2Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch2LiteCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch3ActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch5ActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch5LiteCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatch6Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.RedmiWatchMoveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.XiaomiWatch5Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.XiaomiWatchS1ActiveCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.XiaomiWatchS1Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.XiaomiWatchS1ProCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.XiaomiWatchS3Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.XiaomiWatchS4Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.watches.XiaomiWatchS5Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each xiaomi device type, keyed by its [DeviceType]. A new xiaomi device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface XiaomiCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND4C)
    fun miband4c(coordinator: MiBand4CCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND7PRO)
    fun miband7pro(coordinator: MiBand7ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND8)
    fun miband8(coordinator: MiBand8Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND8ACTIVE)
    fun miband8active(coordinator: MiBand8ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND8PRO)
    fun miband8pro(coordinator: MiBand8ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND9)
    fun miband9(coordinator: MiBand9Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND9ACTIVE)
    fun miband9active(coordinator: MiBand9ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND9PRO)
    fun miband9pro(coordinator: MiBand9ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND10)
    fun miband10(coordinator: MiBand10Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND10PRO)
    fun miband10pro(coordinator: MiBand10ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND11)
    fun miband11(coordinator: MiBand11Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIBAND11ACTIVE)
    fun miband11active(coordinator: MiBand11ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIWATCHLITE)
    fun miwatchlite(coordinator: MiWatchLiteCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MIWATCHCOLORSPORT)
    fun miwatchcolorsport(coordinator: MiWatchColorSportCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH3ACTIVE)
    fun redmiwatch3active(coordinator: RedmiWatch3ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH3)
    fun redmiwatch3(coordinator: RedmiWatch3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMISMARTBAND2)
    fun redmismartband2(coordinator: RedmiSmartBand2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMISMARTBAND3)
    fun redmismartband3(coordinator: RedmiSmartBand3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH2)
    fun redmiwatch2(coordinator: RedmiWatch2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH2LITE)
    fun redmiwatch2lite(coordinator: RedmiWatch2LiteCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMISMARTBANDPRO)
    fun redmismartbandpro(coordinator: RedmiSmartBandProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH4)
    fun redmiwatch4(coordinator: RedmiWatch4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH5)
    fun redmiwatch5(coordinator: RedmiWatch5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH5ACTIVE)
    fun redmiwatch5active(coordinator: RedmiWatch5ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH5LITE)
    fun redmiwatch5lite(coordinator: RedmiWatch5LiteCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCH6)
    fun redmiwatch6(coordinator: RedmiWatch6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REDMIWATCHMOVE)
    fun redmiwatchmove(coordinator: RedmiWatchMoveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_WATCH_S1_ACTIVE)
    fun xiaomiWatchS1Active(coordinator: XiaomiWatchS1ActiveCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_WATCH_S1_PRO)
    fun xiaomiWatchS1Pro(coordinator: XiaomiWatchS1ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_WATCH_S1)
    fun xiaomiWatchS1(coordinator: XiaomiWatchS1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_WATCH_S3)
    fun xiaomiWatchS3(coordinator: XiaomiWatchS3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_WATCH_S4)
    fun xiaomiWatchS4(coordinator: XiaomiWatchS4Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_WATCH_S5)
    fun xiaomiWatchS5(coordinator: XiaomiWatchS5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.XIAOMI_WATCH_5)
    fun xiaomiWatch5(coordinator: XiaomiWatch5Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MAGIC_TOUCH_JJ02)
    fun magicTouchJj02(coordinator: MagicTouchJj02Coordinator): DeviceCoordinator
}
