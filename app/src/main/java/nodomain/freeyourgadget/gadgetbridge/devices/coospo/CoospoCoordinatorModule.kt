package nodomain.freeyourgadget.gadgetbridge.devices.coospo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each coospo device type, keyed by its [DeviceType]. A new coospo device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface CoospoCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COOSPO_H6)
    fun coospoH6(coordinator: CoospoH6Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COOSPO_HW9)
    fun coospoHw9(coordinator: CoospoHW9Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COOSPO_HW807)
    fun coospoHw807(coordinator: CoospoHW807Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COOSPO_H9Z)
    fun coospoH9z(coordinator: CoospoH9ZCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.COOSPO_H808S)
    fun coospoH808s(coordinator: CoospoH808SCoordinator): DeviceCoordinator
}
