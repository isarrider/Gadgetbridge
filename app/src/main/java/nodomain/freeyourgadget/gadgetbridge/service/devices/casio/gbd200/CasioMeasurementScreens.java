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

import java.util.Arrays;

/** The GBD-200 measurement screen layout (feature 0x3e): three screens with three areas each.
 *  On the wire this is 0x3e followed by nine field IDs, screen by screen, top area first. The
 *  same ten bytes are used for the read response and for the write.
 *  Immutable. IDs without a {@link CasioMeasurementScreenField} are kept as they are, so that a
 *  layout read from the watch can always be written back unchanged. */
public final class CasioMeasurementScreens {
    public static final byte FEATURE = 0x3e;
    public static final int SCREEN_COUNT = 3;
    public static final int AREAS_PER_SCREEN = 3;
    public static final int SLOT_COUNT = SCREEN_COUNT * AREAS_PER_SCREEN;
    private static final int PACKET_LENGTH = 1 + SLOT_COUNT;

    /** Config key for reading and writing the layout. */
    public static final String CONFIG_MEASUREMENT_SCREENS = "casio_gbd200_measurement_screens";
    /** Device pref holding the layout to write, see {@link #toHex()}. It is removed as soon as
     *  it has been picked up. */
    public static final String PREF_MEASUREMENT_SCREENS_DRAFT = "casio_gbd200_measurement_screens_draft";
    /** Local broadcast reporting the outcome of a read or write. */
    public static final String ACTION_MEASUREMENT_SCREENS =
            "nodomain.freeyourgadget.gadgetbridge.casio.gbd200.measurement_screens";
    /** Name of a {@link Result}. */
    public static final String EXTRA_MEASUREMENT_SCREENS_RESULT = "result";
    /** The nine field IDs currently on the watch, if known. */
    public static final String EXTRA_MEASUREMENT_SCREENS_IDS = "screens";

    /** Outcome of a 0x3e response, see {@link #classify}. */
    public enum Result {
        READ_OK,
        WRITE_OK,
        WRITE_MISMATCH,
        ERROR
    }

    public static final class Response {
        public final Result result;
        /** What the watch reported; null if and only if result is ERROR. */
        public final CasioMeasurementScreens screens;

        private Response(Result result, CasioMeasurementScreens screens) {
            this.result = result;
            this.screens = screens;
        }
    }

    private final byte[] ids;

    public CasioMeasurementScreens(byte[] ids) {
        if (ids == null || ids.length != SLOT_COUNT) {
            throw new IllegalArgumentException("Expected " + SLOT_COUNT + " field IDs");
        }
        this.ids = ids.clone();
    }

    /** @return the layout, or null unless the packet is exactly 0x3e plus nine IDs. This also
     *  rejects the "ff 81 3e" reply that the feature request matcher lets through. */
    public static CasioMeasurementScreens decode(byte[] packet) {
        if (packet == null || packet.length != PACKET_LENGTH || packet[0] != FEATURE) {
            return null;
        }
        return new CasioMeasurementScreens(Arrays.copyOfRange(packet, 1, PACKET_LENGTH));
    }

    public byte[] encode() {
        byte[] packet = new byte[PACKET_LENGTH];
        packet[0] = FEATURE;
        System.arraycopy(ids, 0, packet, 1, SLOT_COUNT);
        return packet;
    }

    /** Interprets a 0x3e response.
     *  @param expected the layout that was just written and is awaiting its read-back, or null
     *                  if no write is pending
     *  @param response the raw bytes from the watch */
    public static Response classify(CasioMeasurementScreens expected, byte[] response) {
        CasioMeasurementScreens screens = decode(response);
        if (screens == null) {
            return new Response(Result.ERROR, null);
        }
        if (expected == null) {
            return new Response(Result.READ_OK, screens);
        }
        return new Response(expected.equals(screens) ? Result.WRITE_OK : Result.WRITE_MISMATCH, screens);
    }

    /** @param slot 0..8 */
    public byte getId(int slot) {
        return ids[slot];
    }

    /** @return the field in this slot, or null if its ID is not known. */
    public CasioMeasurementScreenField getField(int slot) {
        return CasioMeasurementScreenField.fromWireId(ids[slot]);
    }

    public boolean isKnown(int slot) {
        return CasioMeasurementScreenField.isKnown(ids[slot]);
    }

    public CasioMeasurementScreens withId(int slot, byte id) {
        byte[] changed = ids.clone();
        changed[slot] = id;
        return new CasioMeasurementScreens(changed);
    }

    public CasioMeasurementScreens withField(int slot, CasioMeasurementScreenField field) {
        return withId(slot, field.wireId);
    }

    public byte[] toIds() {
        return ids.clone();
    }

    /** Whether this layout may be sent to a watch that currently has watchState: every slot must
     *  hold a known field, or the very byte the watch already has in that slot. An unknown ID can
     *  therefore be kept in place, but not introduced or moved.
     *  @param watchState the layout last read from the watch */
    public boolean isWritable(CasioMeasurementScreens watchState) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!isKnown(slot) && watchState.ids[slot] != ids[slot]) {
                return false;
            }
        }
        return true;
    }

    /** @return this layout made writable for a watch that now has watchState: every slot that
     *  holds an unknown ID the watch no longer has there takes over the byte from the watch. */
    public CasioMeasurementScreens revalidatedAgainst(CasioMeasurementScreens watchState) {
        byte[] result = ids.clone();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!isKnown(slot)) {
                result[slot] = watchState.ids[slot];
            }
        }
        return new CasioMeasurementScreens(result);
    }

    /** @return the nine IDs as 18 lowercase hex characters. */
    public String toHex() {
        StringBuilder sb = new StringBuilder(SLOT_COUNT * 2);
        for (byte id : ids) {
            sb.append(Character.forDigit((id >> 4) & 0x0f, 16));
            sb.append(Character.forDigit(id & 0x0f, 16));
        }
        return sb.toString();
    }

    /** @return the layout, or null unless the string is exactly 18 hex characters. */
    public static CasioMeasurementScreens fromHex(String hex) {
        if (hex == null || hex.length() != SLOT_COUNT * 2) {
            return null;
        }
        byte[] ids = new byte[SLOT_COUNT];
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            int high = hexDigit(hex.charAt(slot * 2));
            int low = hexDigit(hex.charAt(slot * 2 + 1));
            if (high < 0 || low < 0) {
                return null;
            }
            ids[slot] = (byte) ((high << 4) | low);
        }
        return new CasioMeasurementScreens(ids);
    }

    private static int hexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CasioMeasurementScreens)) {
            return false;
        }
        return Arrays.equals(ids, ((CasioMeasurementScreens) o).ids);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ids);
    }

    @Override
    public String toString() {
        return "CasioMeasurementScreens{" + toHex() + "}";
    }
}
