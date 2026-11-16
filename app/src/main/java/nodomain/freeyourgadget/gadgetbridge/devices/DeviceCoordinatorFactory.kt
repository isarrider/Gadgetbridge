package nodomain.freeyourgadget.gadgetbridge.devices

import dagger.Lazy
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gives the coordinator for each [DeviceType]. A type's coordinator is built the first time it's asked for, and the
 * same instance is returned from then on.
 */
@Singleton
class DeviceCoordinatorFactory @Inject constructor(
    private val coordinators: Map<DeviceType, @JvmSuppressWildcards Lazy<DeviceCoordinator>>,
) {
    /**
     * @throws IllegalStateException if no coordinator is bound for [type].
     */
    fun getDeviceCoordinator(type: DeviceType): DeviceCoordinator =
        checkNotNull(coordinators[type]) { "No coordinator is bound for DeviceType.$type" }.get()
}
