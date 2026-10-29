/*  Copyright (C) 2026 Viktor Karpov

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.devices.miscale;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import nodomain.freeyourgadget.gadgetbridge.R;

/**
 * The scale is useless without its bind key, so pairing asks for it, and it can be changed later
 * where every other device keeps its key.
 */
public class MiScaleS400CoordinatorTest {
    private final MiScaleS400Coordinator coordinator = new MiScaleS400Coordinator();

    @Test
    public void pairingAsksForTheBindKey() {
        assertTrue(coordinator.requiresAuthKey());
    }

    @Test
    public void theBindKeyIs32HexCharacters() {
        assertTrue(coordinator.validateAuthKey("00112233445566778899aabbccddeeff"));
        assertTrue(coordinator.validateAuthKey("00112233445566778899AABBCCDDEEFF"));
        assertFalse(coordinator.validateAuthKey("00112233445566778899aabbccddee"));
        assertFalse(coordinator.validateAuthKey("00112233445566778899aabbccddeeff00"));
        assertFalse(coordinator.validateAuthKey("0x112233445566778899aabbccddeeff"));
        assertFalse(coordinator.validateAuthKey("g0112233445566778899aabbccddeeff"));
    }

    @Test
    public void theBindKeyIsKeptWithTheOtherDevicesKeys() {
        assertArrayEquals(new int[]{R.xml.devicesettings_pairingkey}, coordinator.getSupportedDeviceSpecificAuthenticationSettings());
    }
}
