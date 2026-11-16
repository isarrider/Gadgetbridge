package nodomain.freeyourgadget.gadgetbridge.devices.realme

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each realme device type, keyed by its [DeviceType]. A new realme device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface RealmeCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REALME_BUDS_T110)
    fun realmeBudsT110(coordinator: RealmeBudsT110Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REALME_BUDS_T100)
    fun realmeBudsT100(coordinator: RealmeBudsT100Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REALME_BUDS_T200)
    fun realmeBudsT200(coordinator: RealmeBudsT200Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REALME_BUDS_T300)
    fun realmeBudsT300(coordinator: RealmeBudsT300Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REALME_BUDS_AIR_5_PRO)
    fun realmeBudsAir5Pro(coordinator: RealmeBudsAir5ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.REALME_BUDS_AIR_6_PRO)
    fun realmeBudsAir6Pro(coordinator: RealmeBudsAir6ProCoordinator): DeviceCoordinator
}
