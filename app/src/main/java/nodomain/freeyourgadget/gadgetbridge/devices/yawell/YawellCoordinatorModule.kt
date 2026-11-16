package nodomain.freeyourgadget.gadgetbridge.devices.yawell

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.ColmiR02Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.ColmiR03Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.ColmiR06Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.ColmiR07Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.ColmiR09Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.ColmiR10Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.ColmiR12Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.H59Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.YawellR05Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.YawellR10Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.YawellR11Coordinator
import nodomain.freeyourgadget.gadgetbridge.devices.yawell.ring.YawellY27Coordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each yawell device type, keyed by its [DeviceType]. A new yawell device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface YawellCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.YAWELL_R05)
    fun yawellR05(coordinator: YawellR05Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.YAWELL_R10)
    fun yawellR10(coordinator: YawellR10Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.YAWELL_R11)
    fun yawellR11(coordinator: YawellR11Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.YAWELL_Y27)
    fun yawellY27(coordinator: YawellY27Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_R02)
    fun colmiR02(coordinator: ColmiR02Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_R03)
    fun colmiR03(coordinator: ColmiR03Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_R06)
    fun colmiR06(coordinator: ColmiR06Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_R07)
    fun colmiR07(coordinator: ColmiR07Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_R09)
    fun colmiR09(coordinator: ColmiR09Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_R10)
    fun colmiR10(coordinator: ColmiR10Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COLMI_R12)
    fun colmiR12(coordinator: ColmiR12Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.H59)
    fun h59(coordinator: H59Coordinator): DeviceCoordinator
}
