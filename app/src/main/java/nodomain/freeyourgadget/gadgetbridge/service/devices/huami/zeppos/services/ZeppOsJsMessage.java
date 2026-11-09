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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ZeppOsJsMessage {
    public static final int HEADER_LENGTH = 16;
    public static final int BINDER_TRANSACTION_LIMIT = 1024 * 1024;
    public static final int MAX_BODY_LENGTH = (BINDER_TRANSACTION_LIMIT / 2 / 4) * 3;
    public static final int TYPE_OPEN = 1;
    public static final int TYPE_CLOSE = 2;
    public static final int TYPE_MESSAGE = 4;
    public static final int PHONE_PORT = 1004;

    public static final byte CMD_JS = 0x01;

    private ZeppOsJsMessage() {
    }

    public static final class Frame {
        public final int version;
        public final int type;
        public final int port1;
        public final int port2;
        public final int appId;
        public final int extra;
        public final byte[] body;

        Frame(final int version, final int type, final int port1, final int port2,
              final int appId, final int extra, final byte[] body) {
            this.version = version;
            this.type = type;
            this.port1 = port1;
            this.port2 = port2;
            this.appId = appId;
            this.extra = extra;
            this.body = body;
        }
    }

    public static Frame parse(final byte[] payload) {
        if (payload == null || payload.length < HEADER_LENGTH || payload[0] != CMD_JS) {
            return null;
        }
        final int bodyLength = payload.length - HEADER_LENGTH;
        if (bodyLength > MAX_BODY_LENGTH) {
            return null;
        }
        final ByteBuffer buf = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
        buf.get();
        final int version = buf.get() & 0xff;
        final int type = buf.getShort() & 0xffff;
        final int port1 = buf.getShort() & 0xffff;
        final int port2 = buf.getShort() & 0xffff;
        final int appId = buf.getInt();
        final int extra = buf.getInt();
        final byte[] body = new byte[bodyLength];
        buf.get(body);
        return new Frame(version, type, port1, port2, appId, extra, body);
    }

    public static byte[] encodeMessage(final int appId, final int watchPort, final byte[] body) {
        return encode(1, TYPE_MESSAGE, watchPort, PHONE_PORT, appId, 0, body);
    }

    public static byte[] encodeOpenAck(final Frame open) {
        if (open == null || open.type != TYPE_OPEN) {
            return null;
        }
        return encode(open.version, TYPE_OPEN, open.port1, PHONE_PORT, open.appId, open.extra, new byte[0]);
    }

    public static String canonicalAppId(final String raw) {
        if (raw == null || raw.isEmpty() || raw.length() > 10) {
            return null;
        }
        for (int i = 0; i < raw.length(); i++) {
            final char c = raw.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
        }
        final long value;
        try {
            value = Long.parseLong(raw);
        } catch (final NumberFormatException e) {
            return null;
        }
        if (value > 0xffffffffL) {
            return null;
        }
        return Long.toString(value);
    }

    public static String appIdToString(final int appId) {
        return Integer.toUnsignedString(appId);
    }

    public static int appIdBits(final String canonical) {
        return Integer.parseUnsignedInt(canonical);
    }

    public static byte[] decodePayload(final byte[] utf8) {
        if (utf8 == null) {
            return null;
        }
        return decodePayload(new String(utf8, StandardCharsets.UTF_8));
    }

    public static byte[] decodePayload(final String base64) {
        if (!isCanonicalBase64(base64)) {
            return null;
        }
        try {
            final byte[] body = Base64.getDecoder().decode(base64);
            if (body.length > MAX_BODY_LENGTH) {
                return null;
            }
            return body;
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean isCanonicalBase64(final String value) {
        if (value == null || (value.length() & 3) != 0) {
            return false;
        }
        int padding = 0;
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c == '=') {
                padding++;
                if (padding > 2) {
                    return false;
                }
            } else if (padding > 0 || !isBase64Char(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBase64Char(final char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '+' || c == '/';
    }

    public static String encodePayload(final byte[] body) {
        if (body == null || body.length > MAX_BODY_LENGTH) {
            return null;
        }
        return Base64.getEncoder().encodeToString(body);
    }

    private static byte[] encode(final int version, final int type, final int port1, final int port2,
                                 final int appId, final int extra, final byte[] body) {
        final byte[] data = body == null ? new byte[0] : body;
        if (data.length > MAX_BODY_LENGTH) {
            return null;
        }
        final ByteBuffer buf = ByteBuffer.allocate(HEADER_LENGTH + data.length).order(ByteOrder.LITTLE_ENDIAN);
        buf.put(CMD_JS);
        buf.put((byte) version);
        buf.putShort((short) type);
        buf.putShort((short) port1);
        buf.putShort((short) port2);
        buf.putInt(appId);
        buf.putInt(extra);
        buf.put(data);
        return buf.array();
    }
}
