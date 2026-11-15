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
package nodomain.freeyourgadget.gadgetbridge.devices.jbl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblProtocol;
import nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblTwsProduct;

public class JblTwsProductTest {
    @Test
    public void testProductIds() {
        assertEquals(JblProtocol.TUNE_BUDS_2_PRODUCT_ID,
                JblTwsProduct.TUNE_BUDS_2.getProductId());
        assertEquals(JblProtocol.TUNE_FLEX_2_PRODUCT_ID,
                JblTwsProduct.TUNE_FLEX_2.getProductId());
    }

    @Test
    public void testProductCapabilities() {
        assertTrue(JblTwsProduct.TUNE_BUDS_2.getCapabilities().isAmbientLevelSupported());
        assertFalse(JblTwsProduct.TUNE_BUDS_2.getCapabilities().supportsAccidentalTouch());
        assertFalse(JblTwsProduct.TUNE_BUDS_2.getCapabilities().isEarTipsSupported());

        assertFalse(JblTwsProduct.TUNE_FLEX_2.getCapabilities().isAmbientLevelSupported());
        assertTrue(JblTwsProduct.TUNE_FLEX_2.getCapabilities().supportsAccidentalTouch());
        assertTrue(JblTwsProduct.TUNE_FLEX_2.getCapabilities().isEarTipsSupported());
    }

    @Test
    public void testFlex2NamePatternOnlyMatchesNormalName() {
        final Pattern pattern = new JblTuneFlex2Coordinator().getSupportedDeviceName();
        assertTrue(pattern.matcher("JBL Tune Flex 2").matches());
        assertFalse(pattern.matcher("JBL Tune Flex 2 -LE").matches());
        assertFalse(pattern.matcher("JBL Tune Flex 2 LE").matches());
        assertFalse(pattern.matcher("JBL Tune Flex 2 (LE)").matches());
    }

    @Test
    public void testBuds2NamePatternOnlyMatchesNormalName() {
        final Pattern pattern = new JblTuneBuds2Coordinator().getSupportedDeviceName();
        assertTrue(pattern.matcher("JBL Tune Buds 2").matches());
        assertFalse(pattern.matcher("JBL Tune Buds 2 -LE").matches());
        assertFalse(pattern.matcher("JBL Tune Buds 2 LE").matches());
    }
}
