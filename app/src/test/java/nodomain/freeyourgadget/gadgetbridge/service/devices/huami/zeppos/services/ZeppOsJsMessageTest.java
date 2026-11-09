/*  Copyright (C) 2026 The Gadgetbridge contributors

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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ZeppOsJsMessageTest {
    private static final int APP_ID = 391257;

    @Test
    public void binaryRoundTripPreservesEveryByte() {
        final byte[] body = new byte[] {0x00, (byte) 0xff, 0x10, (byte) 0x80, 0x7f};
        final String encoded = ZeppOsJsMessage.encodePayload(body);
        assertArrayEquals(body, ZeppOsJsMessage.decodePayload(encoded));
        assertArrayEquals(body, ZeppOsJsMessage.decodePayload(encoded.getBytes(StandardCharsets.UTF_8)));

        final byte[] frame = ZeppOsJsMessage.encodeMessage(APP_ID, 20, body);
        final ZeppOsJsMessage.Frame parsed = ZeppOsJsMessage.parse(frame);
        assertNotNull(parsed);
        assertEquals(ZeppOsJsMessage.TYPE_MESSAGE, parsed.type);
        assertEquals(20, parsed.port1);
        assertEquals(ZeppOsJsMessage.PHONE_PORT, parsed.port2);
        assertEquals(APP_ID, parsed.appId);
        assertArrayEquals(body, parsed.body);
        assertArrayEquals(expectedMessage(body), frame);
    }

    @Test
    public void unsignedAppIdSurvivesTheHeader() {
        final long unsigned = 3000000000L;
        final int bits = (int) unsigned;
        final String canonical = ZeppOsJsMessage.canonicalAppId(Long.toString(unsigned));
        assertEquals(Long.toString(unsigned), canonical);
        assertEquals(bits, ZeppOsJsMessage.appIdBits(canonical));
        final byte[] frame = ZeppOsJsMessage.encodeMessage(bits, 1, new byte[] {1});
        final ZeppOsJsMessage.Frame parsed = ZeppOsJsMessage.parse(frame);
        assertNotNull(parsed);
        assertEquals(Long.toString(unsigned), ZeppOsJsMessage.appIdToString(parsed.appId));
    }

    @Test
    public void openAckUsesTheWatchPortAndThePhonePort() {
        final byte[] open = new byte[] {
                0x01, 0x01,
                0x01, 0x00,
                0x14, 0x00,
                0x00, 0x00,
                0x59, (byte) 0xf8, 0x05, 0x00,
                0x02, 0x00, 0x00, 0x00,
                0x00
        };
        final ZeppOsJsMessage.Frame frame = ZeppOsJsMessage.parse(open);
        assertNotNull(frame);
        assertEquals(ZeppOsJsMessage.TYPE_OPEN, frame.type);
        assertEquals(20, frame.port1);
        assertEquals(2, frame.extra);
        assertArrayEquals(new byte[] {0x00}, frame.body);

        final byte[] ack = ZeppOsJsMessage.encodeOpenAck(frame);
        assertArrayEquals(new byte[] {
                0x01, 0x01,
                0x01, 0x00,
                0x14, 0x00,
                (byte) 0xec, 0x03,
                0x59, (byte) 0xf8, 0x05, 0x00,
                0x02, 0x00, 0x00, 0x00
        }, ack);
    }

    @Test
    public void rejectsMalformedFramesAndPayloads() {
        assertNull(ZeppOsJsMessage.parse(null));
        assertNull(ZeppOsJsMessage.parse(new byte[15]));
        final byte[] wrongCommand = expectedMessage(new byte[] {1});
        wrongCommand[0] = 0x02;
        assertNull(ZeppOsJsMessage.parse(wrongCommand));
        assertNull(ZeppOsJsMessage.decodePayload("%%%"));
        assertNull(ZeppOsJsMessage.decodePayload("YQ"));
        assertNull(ZeppOsJsMessage.decodePayload("YQ==\n"));
        assertNull(ZeppOsJsMessage.decodePayload((byte[]) null));
        assertNull(ZeppOsJsMessage.canonicalAppId(null));
        assertNull(ZeppOsJsMessage.canonicalAppId(""));
        assertNull(ZeppOsJsMessage.canonicalAppId("org.example.watchapp"));
        assertNull(ZeppOsJsMessage.canonicalAppId("-1"));
        assertNull(ZeppOsJsMessage.canonicalAppId("4294967296"));
        assertEquals("391257", ZeppOsJsMessage.canonicalAppId("0391257"));
        assertEquals("0", ZeppOsJsMessage.canonicalAppId("000"));
    }

    @Test
    public void rejectsBodiesOverTheBinderDerivedLimit() {
        final byte[] atLimit = new byte[ZeppOsJsMessage.MAX_BODY_LENGTH];
        atLimit[0] = 1;
        atLimit[atLimit.length - 1] = 2;
        final String encoded = ZeppOsJsMessage.encodePayload(atLimit);
        assertNotNull(encoded);
        assertArrayEquals(atLimit, ZeppOsJsMessage.decodePayload(encoded));
        assertNotNull(ZeppOsJsMessage.encodeMessage(1, 0, atLimit));

        final byte[] over = new byte[ZeppOsJsMessage.MAX_BODY_LENGTH + 1];
        assertNull(ZeppOsJsMessage.encodePayload(over));
        assertNull(ZeppOsJsMessage.encodeMessage(1, 0, over));
        final byte[] header = expectedMessage(new byte[0]);
        final byte[] huge = Arrays.copyOf(header, header.length + over.length);
        assertNull(ZeppOsJsMessage.parse(huge));
    }

    private static byte[] expectedMessage(final byte[] body) {
        final byte[] frame = new byte[ZeppOsJsMessage.HEADER_LENGTH + body.length];
        frame[0] = 0x01;
        frame[1] = 0x01;
        frame[2] = 0x04;
        frame[3] = 0x00;
        frame[4] = 0x14;
        frame[5] = 0x00;
        frame[6] = (byte) 0xec;
        frame[7] = 0x03;
        frame[8] = 0x59;
        frame[9] = (byte) 0xf8;
        frame[10] = 0x05;
        frame[11] = 0x00;
        System.arraycopy(body, 0, frame, ZeppOsJsMessage.HEADER_LENGTH, body.length);
        return frame;
    }
}
