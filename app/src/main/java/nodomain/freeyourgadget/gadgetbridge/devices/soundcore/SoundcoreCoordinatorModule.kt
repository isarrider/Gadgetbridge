package nodomain.freeyourgadget.gadgetbridge.devices.soundcore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceTypeKey
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType

/**
 * Binds the coordinator of each soundcore device type, keyed by its [DeviceType]. A new soundcore device
 * type gets its binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
interface SoundcoreCoordinatorModule {
    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_LIBERTY3_PRO)
    fun soundcoreLiberty3Pro(coordinator: SoundcoreLiberty3ProCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_LIBERTY4_NC)
    fun soundcoreLiberty4Nc(coordinator: SoundcoreLiberty4NCCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_MOTION300)
    fun soundcoreMotion300(coordinator: SoundcoreMotion300Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_MOTION_PLUS)
    fun soundcoreMotionPlus(coordinator: SoundcoreMotionPlusCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_Q30)
    fun soundcoreQ30(coordinator: SoundcoreQ30Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_SPACE_ONE)
    fun soundcoreSpaceOne(coordinator: SoundcoreSpaceOneCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_LIFE_TUNE)
    fun soundcoreLifeTune(coordinator: SoundcoreLifeTuneCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_LIFE_TUNE_XR)
    fun soundcoreLifeTuneXr(coordinator: SoundcoreLifeTuneXRCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_AEROFIT2)
    fun soundcoreAerofit2(coordinator: SoundcoreAeroFit2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_SPORT_X20)
    fun soundcoreSportX20(coordinator: SoundcoreSportX20Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_R50I_NC)
    fun soundcoreR50iNc(coordinator: SoundcoreR50iNCCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_A1)
    fun soundcoreA1(coordinator: SoundcoreA1Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_A2_NC)
    fun soundcoreA2Nc(coordinator: SoundcoreA2NCCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_A3I)
    fun soundcoreA3i(coordinator: SoundcoreA3iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_A30I)
    fun soundcoreA30i(coordinator: SoundcoreA30iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_K20I)
    fun soundcoreK20i(coordinator: SoundcoreK20iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P2)
    fun soundcoreP2(coordinator: SoundcoreP2Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P2I)
    fun soundcoreP2i(coordinator: SoundcoreP2iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P2_MINI)
    fun soundcoreP2Mini(coordinator: SoundcoreP2MiniCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P3)
    fun soundcoreP3(coordinator: SoundcoreP3Coordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P3I)
    fun soundcoreP3i(coordinator: SoundcoreP3iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P20I)
    fun soundcoreP20i(coordinator: SoundcoreP20iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P30I)
    fun soundcoreP30i(coordinator: SoundcoreP30iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P31I)
    fun soundcoreP31i(coordinator: SoundcoreP31iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P40I)
    fun soundcoreP40i(coordinator: SoundcoreP40iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P41I)
    fun soundcoreP41i(coordinator: SoundcoreP41iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_P42I)
    fun soundcoreP42i(coordinator: SoundcoreP42iCoordinator): DeviceCoordinator

    @Binds
    @IntoMap
    @DeviceTypeKey(DeviceType.SOUNDCORE_V20I)
    fun soundcoreV20i(coordinator: SoundcoreV20iCoordinator): DeviceCoordinator
}
