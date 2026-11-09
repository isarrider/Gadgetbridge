package nodomain.freeyourgadget.gadgetbridge.service.devices.ultrahuman;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.HexFormat;
import java.util.List;

public class UltrahumanBiosignalFrameTest {
    private static final HexFormat HEX = HexFormat.of();
    // All packets contain synthetic values. CRCs were generated independently of the parser.
    private static final String PPG = "9028000808e80300000000000009180a000000140000001e000000"
            + "01000080320000003c0000003c02070846ec";
    private static final String MOTION = "901800080810040000000000000a0cffff00000080ff7f7b0038fe6c4b";

    @Test
    public void decodesUnsignedOpticalTripletsAndSkipsUnknownField() {
        final UltrahumanBiosignalFrame frame = parse(PPG);
        assertNotNull(frame);
        assertEquals(1000, frame.batchTimestampMs());
        assertEquals(List.of(new UltrahumanBiosignalFrame.Ppg(10, 20, 30),
                new UltrahumanBiosignalFrame.Ppg(0x80000001L, 50, 60)), frame.ppg());
        assertEquals(List.of(), frame.motion());
    }

    @Test
    public void decodesSignedMotionTriplets() {
        final UltrahumanBiosignalFrame frame = parse(MOTION);
        assertNotNull(frame);
        assertEquals(1040, frame.batchTimestampMs());
        assertEquals(List.of(new UltrahumanBiosignalFrame.Motion((short) -1, (short) 0, Short.MIN_VALUE),
                new UltrahumanBiosignalFrame.Motion(Short.MAX_VALUE, (short) 123, (short) -456)), frame.motion());
        assertEquals(List.of(), frame.ppg());
    }

    @Test
    public void rejectsIncorrectLengthsAndChecksum() {
        assertNull(parse("90"));
        assertNull(parse(PPG.substring(0, PPG.length() - 2)));
        assertNull(parse(PPG.substring(0, PPG.length() - 2) + "00"));
        assertNull(parse("9012000808e8030000000000000a070102030405060d52"));
        assertNull(parse("900e000808e8030000000000000a020102f026"));
    }

    @Test
    public void ignoresMetadataOnlyFramesAndOtherOpcodes() {
        assertNull(parse("9004003c020708e491"));
        assertNull(parse("91" + PPG.substring(2)));
    }

    private static UltrahumanBiosignalFrame parse(final String hex) {
        return UltrahumanBiosignalFrame.parse(HEX.parseHex(hex));
    }
}
