package nodomain.freeyourgadget.gadgetbridge.devices.moondrop

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each moondrop device type, keyed by its [DeviceType]. A new moondrop device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface MoondropCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MOONDROP_SPACE_TRAVEL)
    fun moondropSpaceTravel(coordinator: MoondropSpaceTravelCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MOONDROP_SPACE_TRAVEL_2)
    fun moondropSpaceTravel2(coordinator: MoondropSpaceTravel2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.MOONDROP_SPACE_TRAVEL_2_ULTRA)
    fun moondropSpaceTravel2Ultra(coordinator: MoondropSpaceTravel2UltraCoordinator): DeviceCoordinator
}
