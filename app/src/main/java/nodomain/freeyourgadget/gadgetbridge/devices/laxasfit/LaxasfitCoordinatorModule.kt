package nodomain.freeyourgadget.gadgetbridge.devices.laxasfit

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each laxasfit device type, keyed by its [DeviceType]. A new laxasfit device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface LaxasfitCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.LAXASFIT_Q11)
    fun laxasfitQ11(coordinator: LaxasFitCoordinator): DeviceCoordinator
}
