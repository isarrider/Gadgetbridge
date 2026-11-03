/*  Copyright (C) 2026 David Giron

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.devices.soundcore

import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.service.devices.soundcore.devices.SoundcoreP31iProtocol
import java.util.regex.Pattern

class SoundcoreP31iCoordinator : SoundcoreEarbudsCoordinator() {
    override fun getModelName(): String = "D1202"

    override fun getSupportedDeviceName(): Pattern =
        Pattern.compile("^soundcore P31i$", Pattern.CASE_INSENSITIVE)

    override fun getDeviceNameResource(): Int = R.string.devicetype_soundcore_p31i

    override fun getDefaultIconResource(): Int = R.drawable.ic_device_galaxy_buds_3_pro

    override fun getBatteryProperties(): BatteryProperties = BatteryProperties(multiplier = 10, offset = 1)

    // CHECK: There are 5 levels in manual mode. Testing with A30i values.
    override fun getNoiseCancellationLevelRange(): ByteArray = byteArrayOf(0x00, 0x15, 0x25, 0x35, 0x45, 0x51)

    override fun createProtocol(device: GBDevice): SoundcoreP31iProtocol = SoundcoreP31iProtocol(device)

    // TODO: Movie Mode? Music Mode? (default?)
    // Appears in Spatial Audio settings.
    override fun getDefaultCapabilities(): Set<Capability> = setOf(
        Capability.FindDevice,
        Capability.NoiseControl,
        Capability.NoiseCancellationModes,
        Capability.Transparency,
        Capability.AdaptiveNoiseCancellation,
        Capability.AdaptiveNoiseCancellationMode,
        Capability.WindNoiseReduction,
        Capability.VocalMode,
        Capability.TouchControls,
        Capability.FitTest,
        Capability.GamingMode,
        Capability.AudioEqualizer,
        Capability.Multipoint,
        Capability.LDAC,
    )

    override val supportedButtonActions: List<ButtonAction> = listOf(
        ButtonAction.SINGLE_TAP,
        ButtonAction.DOUBLE_TAP,
        ButtonAction.TRIPLE_TAP,
        ButtonAction.LONG_PRESS,
    )

    override val supportedTapFunctions: List<TapFunction> = listOf(
        TapFunction.VOLUME_DOWN,
        TapFunction.VOLUME_UP,
        TapFunction.MEDIA_NEXT,
        TapFunction.MEDIA_PREV,
        TapFunction.PLAYPAUSE,
        TapFunction.VOICE_ASSISTANT,
        TapFunction.AMBIENT_SOUND_CONTROL,
        TapFunction.NONE,
    )
}
