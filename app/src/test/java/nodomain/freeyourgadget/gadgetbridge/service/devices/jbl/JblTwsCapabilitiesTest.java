/*  Copyright (C) 2026

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.jbl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblTwsCapabilities.FirmwareStatus;

public class JblTwsCapabilitiesTest {
    private static final JblTwsCapabilities FLEX_2 = JblTwsCapabilities.builder()
            .accidentalTouchSupportedSince("2.8.0")
            .build();

    @Test
    public void testProductSupportIsIndependentOfFirmware() {
        assertTrue(FLEX_2.supportsAccidentalTouch());
        assertEquals("2.8.0", FLEX_2.getAccidentalTouchMinFirmware());

        final JblTwsCapabilities buds2 = JblTwsCapabilities.builder()
                .ambientLevelSupported()
                .build();
        assertFalse(buds2.supportsAccidentalTouch());
        assertNull(buds2.getAccidentalTouchMinFirmware());
    }

    @Test
    public void testFirmwareComparison() {
        assertEquals(FirmwareStatus.UNSUPPORTED, FLEX_2.getAccidentalTouchFirmwareStatus("2.7.9"));
        assertEquals(FirmwareStatus.SUPPORTED, FLEX_2.getAccidentalTouchFirmwareStatus("2.8.0"));
        assertEquals(FirmwareStatus.SUPPORTED, FLEX_2.getAccidentalTouchFirmwareStatus("2.8.1"));
        assertEquals(FirmwareStatus.SUPPORTED, FLEX_2.getAccidentalTouchFirmwareStatus("3.5.0"));
        assertEquals(FirmwareStatus.SUPPORTED, FLEX_2.getAccidentalTouchFirmwareStatus("10.0.0"));
        assertEquals(FirmwareStatus.UNSUPPORTED, FLEX_2.getAccidentalTouchFirmwareStatus("2.7"));
    }

    @Test
    public void testMissingComponentsAreZeroFilled() {
        assertEquals(FirmwareStatus.UNSUPPORTED, FLEX_2.getAccidentalTouchFirmwareStatus("2"));
        assertEquals(FirmwareStatus.SUPPORTED, FLEX_2.getAccidentalTouchFirmwareStatus("2.8"));
    }

    @Test
    public void testUnknownFirmwareFailsClosed() {
        assertEquals(FirmwareStatus.UNKNOWN, FLEX_2.getAccidentalTouchFirmwareStatus(null));
        assertEquals(FirmwareStatus.UNKNOWN, FLEX_2.getAccidentalTouchFirmwareStatus(""));
        assertEquals(FirmwareStatus.UNKNOWN, FLEX_2.getAccidentalTouchFirmwareStatus("   "));
        assertEquals(FirmwareStatus.UNKNOWN, FLEX_2.getAccidentalTouchFirmwareStatus("unknown"));
        assertEquals(FirmwareStatus.UNKNOWN, FLEX_2.getAccidentalTouchFirmwareStatus("2.x.0"));
        assertEquals(FirmwareStatus.UNKNOWN, FLEX_2.getAccidentalTouchFirmwareStatus("2.8.0.1"));
        assertEquals(FirmwareStatus.UNKNOWN, FLEX_2.getAccidentalTouchFirmwareStatus("2..0"));

        assertFalse(FLEX_2.isAccidentalTouchAvailableOnFirmware(null));
        assertFalse(FLEX_2.isAccidentalTouchAvailableOnFirmware("unknown"));
        assertTrue(FLEX_2.isAccidentalTouchAvailableOnFirmware("2.8.0"));
    }

    @Test
    public void testInvalidBuilderConfigurationDisablesCapability() {
        final JblTwsCapabilities invalid = JblTwsCapabilities.builder()
                .accidentalTouchSupportedSince("2.x.0")
                .build();
        assertFalse(invalid.supportsAccidentalTouch());
        assertNull(invalid.getAccidentalTouchMinFirmware());
        assertEquals(FirmwareStatus.UNSUPPORTED, invalid.getAccidentalTouchFirmwareStatus("3.5.0"));
        assertFalse(invalid.isAccidentalTouchAvailableOnFirmware("3.5.0"));
    }

    @Test
    public void testProductWithoutCapabilityIsUnsupported() {
        final JblTwsCapabilities unsupported = JblTwsCapabilities.builder().build();
        assertEquals(FirmwareStatus.UNSUPPORTED, unsupported.getAccidentalTouchFirmwareStatus("3.5.0"));
        assertFalse(unsupported.isAccidentalTouchAvailableOnFirmware("3.5.0"));
    }
}
