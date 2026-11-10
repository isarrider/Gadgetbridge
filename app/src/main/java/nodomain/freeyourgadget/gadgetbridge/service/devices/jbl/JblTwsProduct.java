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

import androidx.annotation.NonNull;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblProtocol.TUNE_BUDS_2_PRODUCT_ID;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.jbl.JblProtocol.TUNE_FLEX_2_PRODUCT_ID;

/**
 * JBL TWS models supported by the shared Harman Protocol 3 transport.
 */
public enum JblTwsProduct {
    TUNE_BUDS_2(TUNE_BUDS_2_PRODUCT_ID, JblTwsCapabilities.builder()
            .ambientLevelSupported()
            .build()),
    TUNE_FLEX_2(TUNE_FLEX_2_PRODUCT_ID, JblTwsCapabilities.builder()
            .accidentalTouchSupportedSince("2.8.0")
            .build());

    private final int productId;
    private final JblTwsCapabilities capabilities;

    JblTwsProduct(final int productId, final JblTwsCapabilities capabilities) {
        this.productId = productId;
        this.capabilities = capabilities;
    }

    public int getProductId() {
        return productId;
    }

    @NonNull
    public JblTwsCapabilities getCapabilities() {
        return capabilities;
    }
}
