/*  Copyright (C) 2026 Gadgetbridge contributors

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.casio.gbd200;

/** Fields a GBD-200 measurement screen area can show, with the ID used on the wire (feature 0x3e).
 *  These are the twelve items the watch and the official app offer; each ID was observed on the
 *  wire together with its label. Any other ID is kept as a raw byte by
 *  {@link CasioMeasurementScreens}, so there is deliberately no "unknown" constant. */
public enum CasioMeasurementScreenField {
    SPLIT_TIME(0x00),
    DISTANCE(0x01),
    PACE(0x02),
    LAP_TIME(0x03),
    LAP_DISTANCE(0x04),
    LAP_PACE(0x05),
    AVG_PACE(0x06),
    SPEED(0x07),
    AVG_SPEED(0x08),
    CALORIES(0x09),
    CURRENT_TIME(0x12),
    DATE(0x13);

    public final byte wireId;

    CasioMeasurementScreenField(int wireId) {
        this.wireId = (byte) wireId;
    }

    /** @return the field for this wire ID, or null if the ID is not known. */
    public static CasioMeasurementScreenField fromWireId(byte wireId) {
        for (CasioMeasurementScreenField field : values()) {
            if (field.wireId == wireId) {
                return field;
            }
        }
        return null;
    }

    public static boolean isKnown(byte wireId) {
        return fromWireId(wireId) != null;
    }
}
