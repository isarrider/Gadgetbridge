/*  Copyright (C) 2026 dusberendguy

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huami.zeppos.services;

import static org.junit.Assert.*;

import org.junit.Test;

import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class ZeppOsCalendarServiceTest {
    @Test
    public void testCalendarV4() {
        final byte[] expected = GB.hexStringToByteArray("23c5bfe56a54657374313000003230323631303331543133303030305a003230323631303331543134303030305a008403000000000082c7bfe56a44656c667400");
        final byte[] actual = ZeppOsCalendarService.encodeEventV4(0x6ae5bfc5, "Test10", null, 1793451600, 1793455200, 900, false, 0x6ae5bfc7, "Delft");
        assertArrayEquals(expected, actual);
    }
}
