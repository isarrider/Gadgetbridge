package nodomain.freeyourgadget.gadgetbridge.service

import android.content.Context

/**
 * The app-wide dependencies a [DeviceSupport] gets through [DeviceSupport.setContext]. Hilt can't build supports,
 * so `setContext` is their only way in, and everything app-wide a support needs comes through here, including
 * dependencies only one vendor uses.
 *
 * What a support receives falls into three groups:
 * - per-connection values, such as the device and the USB accessory, stay `setContext` parameters;
 * - app-wide dependencies, such as the Context supports run in and the injected homes, go here;
 * - what a support creates for itself, such as its connection, queues and protocol state, isn't a dependency.
 *
 * The `BluetoothAdapter` is app-wide too, but it stays a `setContext` parameter because it selects the transport.
 *
 * Helpers owned by a support take only the dependencies they use, through their constructors, never this class.
 *
 * Each property is what a support would take as a constructor parameter if supports were ever built by Hilt.
 */
class DeviceSupportDependencies(
    /** The Context supports run in: the `DeviceCommunicationService`. */
    val context: Context,
)
