package nodomain.freeyourgadget.gadgetbridge.devices

import dagger.MapKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Keys a coordinator binding by the [DeviceType] it serves. Each vendor's coordinator module binds its device types'
 * coordinators with it, and [DeviceCoordinatorFactory] looks them up by type.
 */
@MapKey
@Retention(AnnotationRetention.BINARY)
annotation class DeviceTypeKey(val value: DeviceType)
