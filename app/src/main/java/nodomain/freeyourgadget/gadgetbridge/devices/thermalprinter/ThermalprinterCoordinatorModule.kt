package nodomain.freeyourgadget.gadgetbridge.devices.thermalprinter

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each thermalprinter device type, keyed by its [DeviceType]. A new thermalprinter device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ThermalprinterCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.GENERIC_THERMAL_PRINTER)
    fun genericThermalPrinter(coordinator: ThermalPrinterCoordinator): DeviceCoordinator
}
