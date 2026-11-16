package nodomain.freeyourgadget.gadgetbridge.devices.casio

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.devices.casio.ecbs100.CasioECBS100DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.casio.gb6900.CasioGB6900DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.casio.gbd200.CasioGBD200DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.casio.gbx100.CasioGBX100DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.casio.ggb100.CasioGGB100DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.casio.gwb5600.CasioGMWB5000DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.casio.gwb5600.CasioGWB5600DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each casio device type, keyed by its [DeviceType]. A new casio device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface CasioCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CASIOECBS100)
    fun casioecbs100(coordinator: CasioECBS100DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CASIOGB6900)
    fun casiogb6900(coordinator: CasioGB6900DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CASIOGBX100)
    fun casiogbx100(coordinator: CasioGBX100DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CASIOGBD200)
    fun casiogbd200(coordinator: CasioGBD200DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CASIOGWB5600)
    fun casiogwb5600(coordinator: CasioGWB5600DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CASIOGMWB5000)
    fun casiogmwb5000(coordinator: CasioGMWB5000DeviceCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.CASIOGGB100)
    fun casioggb100(coordinator: CasioGGB100DeviceCoordinator): DeviceCoordinator
}
