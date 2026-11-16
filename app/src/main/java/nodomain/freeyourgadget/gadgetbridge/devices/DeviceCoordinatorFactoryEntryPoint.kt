package nodomain.freeyourgadget.gadgetbridge.devices

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Gives construction roots the [DeviceCoordinatorFactory], through
 * [nodomain.freeyourgadget.gadgetbridge.di.RootEntryPoints].
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface DeviceCoordinatorFactoryEntryPoint {
    fun deviceCoordinatorFactory(): DeviceCoordinatorFactory
}
