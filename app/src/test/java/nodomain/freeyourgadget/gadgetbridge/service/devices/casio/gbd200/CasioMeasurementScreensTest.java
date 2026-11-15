package nodomain.freeyourgadget.gadgetbridge.service.devices.casio.gbd200;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CasioMeasurementScreensTest {
    // Captured from the official app talking to a GBD-200 (HCI snoop log).
    private static final String V0 = "3e 00 01 02 03 04 05 07 00 09"; // read, factory layout
    private static final String V1 = "3e 03 01 02 03 04 05 07 00 09"; // read after slot 0 -> lap time
    private static final String V2 = "3e 12 01 02 03 04 05 07 00 09"; // write, later read back
    private static final String V3 = "3e 12 01 02 03 06 05 07 00 09"; // write, slot 4 -> 0x06
    private static final String V4 = "3e 12 01 13 04 06 08 07 00 09"; // write, then read back
    private static final String[] CAPTURES = {V0, V1, V2, V3, V4};

    // An ID that is not expected to ever become a known field.
    private static final byte UNKNOWN = 0x7f;
    private static final String WITH_UNKNOWN = "3e 12 01 02 03 7f 05 07 00 09";

    private static final String NAK = "ff 81 3e";

    private static byte[] hex(String s) {
        String[] parts = s.trim().split("\\s+");
        byte[] out = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            out[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return out;
    }

    private static CasioMeasurementScreens screens(String packet) {
        CasioMeasurementScreens s = CasioMeasurementScreens.decode(hex(packet));
        assertNotNull(packet, s);
        return s;
    }

    // --- decode / encode ---

    @Test
    public void decode_capturedDefault_yieldsNineSlots() {
        CasioMeasurementScreens s = screens(V0);
        assertEquals(0x00, s.getId(0));
        assertEquals(0x00, s.getId(7));
        assertEquals(0x09, s.getId(8));
        assertSame(CasioMeasurementScreenField.SPLIT_TIME, s.getField(0));
        assertSame(CasioMeasurementScreenField.DISTANCE, s.getField(1));
        assertSame(CasioMeasurementScreenField.PACE, s.getField(2));
        assertSame(CasioMeasurementScreenField.LAP_TIME, s.getField(3));
        assertSame(CasioMeasurementScreenField.LAP_PACE, s.getField(5));
        assertSame(CasioMeasurementScreenField.SPEED, s.getField(6));
        assertSame(CasioMeasurementScreenField.SPLIT_TIME, s.getField(7));
        assertSame(CasioMeasurementScreenField.CALORIES, s.getField(8));
    }

    @Test
    public void decode_capturedCurrentTime_mapsSlot0() {
        CasioMeasurementScreens s = screens(V2);
        assertSame(CasioMeasurementScreenField.CURRENT_TIME, s.getField(0));
        assertEquals(0x12, s.getId(0));
        assertEquals(0x04, s.getId(4));
    }

    @Test
    public void encode_roundTrips_allCaptures() {
        for (String capture : CAPTURES) {
            assertArrayEquals(capture, hex(capture), screens(capture).encode());
        }
    }

    @Test
    public void decode_unknownId_preservedNotReplaced() {
        CasioMeasurementScreens s = screens(WITH_UNKNOWN);
        assertNull(s.getField(4));
        assertFalse(s.isKnown(4));
        assertTrue(s.isKnown(3));
        assertEquals(UNKNOWN, s.getId(4));
        assertArrayEquals(hex(WITH_UNKNOWN), s.encode());
    }

    @Test
    public void decode_unknownHighId_preserved() {
        String packet = "3e ff 01 02 03 04 05 07 00 09";
        CasioMeasurementScreens s = screens(packet);
        assertEquals((byte) 0xff, s.getId(0));
        assertNull(s.getField(0));
        assertArrayEquals(hex(packet), s.encode());
    }

    @Test
    public void decode_capturedLayout_matchesOfficialAppLabels() {
        // The official app showed exactly these nine labels for this packet.
        CasioMeasurementScreens s = screens(V4);
        assertSame(CasioMeasurementScreenField.CURRENT_TIME, s.getField(0));
        assertSame(CasioMeasurementScreenField.DISTANCE, s.getField(1));
        assertSame(CasioMeasurementScreenField.DATE, s.getField(2));
        assertSame(CasioMeasurementScreenField.LAP_DISTANCE, s.getField(3));
        assertSame(CasioMeasurementScreenField.AVG_PACE, s.getField(4));
        assertSame(CasioMeasurementScreenField.AVG_SPEED, s.getField(5));
        assertSame(CasioMeasurementScreenField.SPEED, s.getField(6));
        assertSame(CasioMeasurementScreenField.SPLIT_TIME, s.getField(7));
        assertSame(CasioMeasurementScreenField.CALORIES, s.getField(8));
    }

    @Test
    public void decode_rejectsShort() {
        assertNull(CasioMeasurementScreens.decode(hex("3e")));
        assertNull(CasioMeasurementScreens.decode(hex("3e 12 01 02 03 04 05 07 00")));
    }

    @Test
    public void decode_rejectsLong() {
        assertNull(CasioMeasurementScreens.decode(hex("3e 12 01 02 03 04 05 07 00 09 00")));
    }

    @Test
    public void decode_rejectsWrongFeature() {
        assertNull(CasioMeasurementScreens.decode(hex("3f 00 3c 00 00 1e 00 00 64 00")));
        assertNull(CasioMeasurementScreens.decode(hex("40 0d 0a 00 f7 00")));
    }

    @Test
    public void decode_rejectsNak() {
        assertNull(CasioMeasurementScreens.decode(hex(NAK)));
    }

    @Test
    public void decode_rejectsNullAndEmpty() {
        assertNull(CasioMeasurementScreens.decode(null));
        assertNull(CasioMeasurementScreens.decode(new byte[0]));
    }

    // --- model ---

    @Test
    public void constructor_requiresNineIds_andCopies() {
        assertThrows(IllegalArgumentException.class, () -> new CasioMeasurementScreens(null));
        assertThrows(IllegalArgumentException.class, () -> new CasioMeasurementScreens(new byte[8]));
        assertThrows(IllegalArgumentException.class, () -> new CasioMeasurementScreens(new byte[10]));

        byte[] ids = {0x12, 0x01, 0x02, 0x03, 0x04, 0x05, 0x07, 0x00, 0x09};
        CasioMeasurementScreens s = new CasioMeasurementScreens(ids);
        ids[0] = 0x55;
        assertEquals(0x12, s.getId(0));
        s.toIds()[0] = 0x55;
        assertEquals(0x12, s.getId(0));
        s.encode()[1] = 0x55;
        assertEquals(0x12, s.getId(0));
    }

    @Test
    public void withId_changesOnlyThatSlot_andLeavesOriginalAlone() {
        CasioMeasurementScreens v1 = screens(V1);
        CasioMeasurementScreens changed = v1.withId(0, (byte) 0x12);
        assertArrayEquals(hex(V2), changed.encode());
        assertArrayEquals(hex(V1), v1.encode());
    }

    @Test
    public void withField_writesTheFieldsWireId() {
        CasioMeasurementScreens changed = screens(V1).withField(0, CasioMeasurementScreenField.CURRENT_TIME);
        assertArrayEquals(hex(V2), changed.encode());
    }

    @Test
    public void equalsAndHashCode_followTheNineBytes() {
        assertEquals(screens(V2), screens(V2));
        assertEquals(screens(V2).hashCode(), screens(V2).hashCode());
        assertNotEquals(screens(V2), screens(V3));
        assertEquals(screens(V2), screens(V1).withId(0, (byte) 0x12));
    }

    // --- isWritable ---

    @Test
    public void isWritable_knownIdsOnly_true() {
        assertTrue(screens(V2).isWritable(screens(V1)));
        CasioMeasurementScreens allKnown = screens("3e 12 01 02 03 05 05 07 00 09");
        assertTrue(allKnown.isWritable(screens(V1)));
        assertTrue(allKnown.isWritable(screens(WITH_UNKNOWN)));
    }

    @Test
    public void isWritable_unknownPreservedInPlace_true() {
        CasioMeasurementScreens watch = screens(WITH_UNKNOWN);
        assertTrue(watch.isWritable(watch));
        assertTrue(watch.withId(0, (byte) 0x00).isWritable(watch));
    }

    @Test
    public void isWritable_unknownIntroduced_false() {
        CasioMeasurementScreens watch = screens(V2);
        assertFalse(screens("3e 12 01 02 03 04 05 07 00 7f").isWritable(watch));
        assertFalse(screens("3e 12 01 02 03 04 05 07 00 0a").isWritable(watch));
    }

    @Test
    public void isWritable_unknownMovedToOtherSlot_false() {
        CasioMeasurementScreens watch = screens(WITH_UNKNOWN);
        assertFalse(screens("3e 7f 01 02 03 7f 05 07 00 09").isWritable(watch));
        assertFalse(screens("3e 7f 01 02 03 05 05 07 00 09").isWritable(watch));
    }

    // --- revalidatedAgainst ---

    @Test
    public void revalidatedAgainst_unknownStillOnWatch_isKept() {
        CasioMeasurementScreens watch = screens(WITH_UNKNOWN);
        CasioMeasurementScreens draft = watch.withId(0, (byte) 0x00);
        CasioMeasurementScreens result = draft.revalidatedAgainst(watch);
        assertEquals(draft, result);
        assertTrue(result.isWritable(watch));
    }

    @Test
    public void revalidatedAgainst_unknownGoneFromWatch_takesTheWatchByte() {
        CasioMeasurementScreens draft = screens(WITH_UNKNOWN).withId(0, (byte) 0x00);
        CasioMeasurementScreens watch = screens(V3);
        assertFalse(draft.isWritable(watch));
        CasioMeasurementScreens result = draft.revalidatedAgainst(watch);
        assertArrayEquals(hex("3e 00 01 02 03 06 05 07 00 09"), result.encode());
        assertTrue(result.isWritable(watch));
    }

    @Test
    public void revalidatedAgainst_knownEdits_areUntouched() {
        CasioMeasurementScreens draft = screens("3e 09 07 05 03 02 01 00 12 12");
        CasioMeasurementScreens result = draft.revalidatedAgainst(screens(WITH_UNKNOWN));
        assertEquals(draft, result);
        assertTrue(result.isWritable(screens(WITH_UNKNOWN)));
    }

    @Test
    public void revalidatedAgainst_isAlwaysWritable() {
        String[] layouts = {V0, V1, V2, V3, WITH_UNKNOWN, "3e 7f 7f 7f 7f 7f 7f 7f 7f 7f",
                "3e ff 01 02 03 04 05 07 00 0a"};
        for (String draft : layouts) {
            for (String watch : layouts) {
                assertTrue(draft + " / " + watch,
                        screens(draft).revalidatedAgainst(screens(watch)).isWritable(screens(watch)));
            }
        }
    }

    @Test
    public void duplicatesAllowed() {
        // The watch accepts the same field on several screens (0x00 in slots 0 and 7).
        CasioMeasurementScreens v0 = screens(V0);
        assertEquals(v0.getId(0), v0.getId(7));
        assertTrue(v0.isWritable(screens(V2)));
    }

    // --- field enum ---

    @Test
    public void fromWireId_explicitIds() {
        assertSame(CasioMeasurementScreenField.SPLIT_TIME, CasioMeasurementScreenField.fromWireId((byte) 0x00));
        assertSame(CasioMeasurementScreenField.DISTANCE, CasioMeasurementScreenField.fromWireId((byte) 0x01));
        assertSame(CasioMeasurementScreenField.PACE, CasioMeasurementScreenField.fromWireId((byte) 0x02));
        assertSame(CasioMeasurementScreenField.LAP_TIME, CasioMeasurementScreenField.fromWireId((byte) 0x03));
        assertSame(CasioMeasurementScreenField.LAP_DISTANCE, CasioMeasurementScreenField.fromWireId((byte) 0x04));
        assertSame(CasioMeasurementScreenField.LAP_PACE, CasioMeasurementScreenField.fromWireId((byte) 0x05));
        assertSame(CasioMeasurementScreenField.AVG_PACE, CasioMeasurementScreenField.fromWireId((byte) 0x06));
        assertSame(CasioMeasurementScreenField.SPEED, CasioMeasurementScreenField.fromWireId((byte) 0x07));
        assertSame(CasioMeasurementScreenField.AVG_SPEED, CasioMeasurementScreenField.fromWireId((byte) 0x08));
        assertSame(CasioMeasurementScreenField.CALORIES, CasioMeasurementScreenField.fromWireId((byte) 0x09));
        assertSame(CasioMeasurementScreenField.CURRENT_TIME, CasioMeasurementScreenField.fromWireId((byte) 0x12));
        assertSame(CasioMeasurementScreenField.DATE, CasioMeasurementScreenField.fromWireId((byte) 0x13));
    }

    @Test
    public void fromWireId_unknownIds_returnNull() {
        for (int id : new int[]{0x0a, 0x11, 0x14, 0x7f, 0x80, 0xff}) {
            assertNull(Integer.toHexString(id), CasioMeasurementScreenField.fromWireId((byte) id));
            assertFalse(Integer.toHexString(id), CasioMeasurementScreenField.isKnown((byte) id));
        }
    }

    @Test
    public void wireIds_areUnique_andRoundTrip() {
        Set<Byte> seen = new HashSet<>();
        for (CasioMeasurementScreenField field : CasioMeasurementScreenField.values()) {
            assertTrue(field + " reuses a wire ID", seen.add(field.wireId));
            assertSame(field, CasioMeasurementScreenField.fromWireId(field.wireId));
            assertTrue(CasioMeasurementScreenField.isKnown(field.wireId));
        }
        assertEquals(CasioMeasurementScreenField.values().length, seen.size());
    }

    // --- hex ---

    @Test
    public void hex_roundTrip() {
        CasioMeasurementScreens s = CasioMeasurementScreens.fromHex("120102030405070009");
        assertNotNull(s);
        assertEquals(screens(V2), s);
        assertEquals("120102030405070009", s.toHex());
        assertEquals("ff010203047f070009", screens("3e ff 01 02 03 04 7f 07 00 09").toHex());
        assertEquals(screens(WITH_UNKNOWN), CasioMeasurementScreens.fromHex(screens(WITH_UNKNOWN).toHex()));
        assertEquals(screens(V2), CasioMeasurementScreens.fromHex("120102030405070009".toUpperCase()));
    }

    @Test
    public void hex_rejectsBadInput() {
        assertNull(CasioMeasurementScreens.fromHex(null));
        assertNull(CasioMeasurementScreens.fromHex(""));
        assertNull(CasioMeasurementScreens.fromHex("1201020304050700"));     // 16 chars
        assertNull(CasioMeasurementScreens.fromHex("12010203040507000900")); // 20 chars
        assertNull(CasioMeasurementScreens.fromHex("12010203040507000g"));
        assertNull(CasioMeasurementScreens.fromHex("+20102030405070009"));
        assertNull(CasioMeasurementScreens.fromHex("12 01 02 03 04 05 "));
    }

    // --- response classification ---

    @Test
    public void classify_readWithNoPendingWrite_isReadOk() {
        CasioMeasurementScreens.Response r = CasioMeasurementScreens.classify(null, hex(V2));
        assertSame(CasioMeasurementScreens.Result.READ_OK, r.result);
        assertEquals(screens(V2), r.screens);
    }

    @Test
    public void classify_readBackMatchesExpected_isWriteOk() {
        CasioMeasurementScreens.Response r = CasioMeasurementScreens.classify(screens(V2), hex(V2));
        assertSame(CasioMeasurementScreens.Result.WRITE_OK, r.result);
        assertEquals(screens(V2), r.screens);
    }

    @Test
    public void classify_readBackDiffers_isWriteMismatch_withWatchState() {
        CasioMeasurementScreens.Response r = CasioMeasurementScreens.classify(screens(V2), hex(V1));
        assertSame(CasioMeasurementScreens.Result.WRITE_MISMATCH, r.result);
        assertEquals(screens(V1), r.screens);
    }

    @Test
    public void classify_invalidResponse_isError_withoutScreens() {
        String[] invalid = {NAK, "3e", "3e 12 01 02 03 04 05 07 00", "3e 12 01 02 03 04 05 07 00 09 00"};
        for (String packet : invalid) {
            for (CasioMeasurementScreens expected : new CasioMeasurementScreens[]{null, screens(V2)}) {
                CasioMeasurementScreens.Response r = CasioMeasurementScreens.classify(expected, hex(packet));
                assertSame(packet, CasioMeasurementScreens.Result.ERROR, r.result);
                assertNull(packet, r.screens);
            }
        }
        assertSame(CasioMeasurementScreens.Result.ERROR, CasioMeasurementScreens.classify(null, null).result);
    }
}
