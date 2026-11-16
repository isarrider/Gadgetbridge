package nodomain.freeyourgadget.gadgetbridge.devices.nothing

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each nothing device type, keyed by its [DeviceType]. A new nothing device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface NothingCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_EAR1)
    fun nothingEar1(coordinator: Ear1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_EAR2)
    fun nothingEar2(coordinator: Ear2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_EAR_A)
    fun nothingEarA(coordinator: EarACoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_EAR_STICK)
    fun nothingEarStick(coordinator: EarStickCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_CMF_BUDS_2)
    fun nothingCmfBuds2(coordinator: CmfBuds2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_CMF_BUDS_2A)
    fun nothingCmfBuds2a(coordinator: CmfBuds2aCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_CMF_BUDS_PRO_2)
    fun nothingCmfBudsPro2(coordinator: CmfBudsPro2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.NOTHING_CMF_BUDS_2_PLUS)
    fun nothingCmfBuds2Plus(coordinator: CmfBuds2PlusCoordinator): DeviceCoordinator
}
