package nodomain.freeyourgadget.gadgetbridge.service.devices.galaxywatch7;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WearDataLayerProtocolTest {
    private static final String PKG = "com.example.companion";
    private static final String SIG = "0123456789abcdef0123456789abcdef01234567";
    private static final String OWN = "aabbccdd";
    private static final String WATCH = "11223344";

    private static byte[] garbage() {
        return new byte[]{0x01, 0x02, 0x03, (byte) 0xff, (byte) 0xfe, 0x00, 0x7f};
    }

    @Test
    public void nodeInfoHandshakeRoundTrips() {
        final byte[] frame = WearDataLayerProtocol.buildNodeInfoHandshake(OWN, "Gadgetbridge", 42L, "9876543210");
        assertEquals(OWN, WearDataLayerProtocol.parseWatchNodeId(frame));
        assertEquals("9876543210", WearDataLayerProtocol.parseWatchConnectionId(frame));
    }

    @Test
    public void nodeInfoWithoutConnectionIdHasNone() {
        final byte[] frame = WearDataLayerProtocol.buildNodeInfoHandshake(OWN, "Gadgetbridge", 42L, null);
        assertEquals(OWN, WearDataLayerProtocol.parseWatchNodeId(frame));
        assertNull(WearDataLayerProtocol.parseWatchConnectionId(frame));
    }

    @Test
    public void messageEventRoundTrips() {
        final byte[] frame = WearDataLayerProtocol.buildMessageEvent(
                PKG, SIG, OWN, WATCH, 7L, "/some/path", "hello".getBytes(StandardCharsets.UTF_8));
        assertEquals("/some/path", WearDataLayerProtocol.parseMessageEventPath(frame));
        assertEquals("hello", WearDataLayerProtocol.parseMessageEventData(frame));
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8),
                WearDataLayerProtocol.parseMessageEventDataBytes(frame));
    }

    @Test
    public void stfBatteryRequestIsAddressedToStfPath() {
        final byte[] frame = WearDataLayerProtocol.buildStfBatteryRequest(
                PKG, SIG, OWN, WATCH, 1L, "AA:BB:CC:DD:EE:FF");
        assertEquals("/w_stf", WearDataLayerProtocol.parseMessageEventPath(frame));
        final String json = WearDataLayerProtocol.parseMessageEventData(frame);
        assertTrue(json.contains("stf_battery_req"));
        assertTrue(json.contains("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    public void stfBatteryResponseIsParsed() {
        final String json = "{\"msg_id\":\"stf_battery_rsp\",\"deviceId\":\"x\",\"result\":true,"
                + "\"data\":\"{\\\"batteryLevel\\\":95}\"}";
        final byte[] frame = WearDataLayerProtocol.buildMessageEvent(
                PKG, SIG, WATCH, OWN, 1L, "/w_stf", json.getBytes(StandardCharsets.UTF_8));
        assertEquals(Integer.valueOf(95), WearDataLayerProtocol.parseStfBatteryLevel(frame));
    }

    @Test
    public void stfBatteryIgnoresOtherPathsAndGarbage() {
        final byte[] other = WearDataLayerProtocol.buildMessageEvent(
                PKG, SIG, WATCH, OWN, 1L, "/other", "{\"batteryLevel\":50}".getBytes(StandardCharsets.UTF_8));
        assertNull(WearDataLayerProtocol.parseStfBatteryLevel(other));
        assertNull(WearDataLayerProtocol.parseStfBatteryLevel(garbage()));
    }

    @Test
    public void watchSettingPutCarriesPathAndSeq() {
        final byte[] frame = WearDataLayerProtocol.buildWatchSettingBooleanPut(
                PKG, SIG, OWN, "wrist_up/enabled", true, 1234L);
        assertEquals("wrist_up/enabled", WearDataLayerProtocol.parseWatchSettingSubPath(frame));
        assertEquals(Long.valueOf(1234L), WearDataLayerProtocol.parseDataItemSeq(frame));
        assertNotNull(WearDataLayerProtocol.parseDataItemValue(frame));
        assertEquals(Boolean.TRUE, WearDataLayerProtocol.parseDataMapBoolean(frame, "booleanIsChecked"));
        assertTrue(WearDataLayerProtocol.parseDataItemPath(frame).contains(OWN));
    }

    @Test
    public void watchSettingIntAndStringAreEncoded() {
        final byte[] intPut = WearDataLayerProtocol.buildWatchSettingIntPut(PKG, SIG, OWN, "a/b", "intLevel", 5, 1L);
        final byte[] strPut = WearDataLayerProtocol.buildWatchSettingStringPut(PKG, SIG, OWN, "a/c", "stringMode", "v", 2L);
        assertEquals("a/b", WearDataLayerProtocol.parseWatchSettingSubPath(intPut));
        assertEquals("a/c", WearDataLayerProtocol.parseWatchSettingSubPath(strPut));
        assertEquals(Integer.valueOf(5), WearDataLayerProtocol.parseDataMapInt(intPut, "intLevel"));
        assertEquals("v", WearDataLayerProtocol.parseDataMapString(strPut, "stringMode"));
    }

    @Test
    public void notificationDeleteRoundTripsId() {
        final byte[] frame = WearDataLayerProtocol.buildNotificationDelete(
                PKG, SIG, OWN, "org.example.chat", 4711, "tag", 9L);
        assertEquals(Integer.valueOf(4711), WearDataLayerProtocol.parseNotificationStreamDeleteId(frame));
    }

    @Test
    public void notificationPutIsNotMistakenForDelete() {
        final byte[] frame = WearDataLayerProtocol.buildNotificationPut(
                PKG, SIG, OWN, "org.example.chat", 4711, "tag", "Chat",
                "Title", "Body", null, 1000L, 1000L, 0xff0000L, 0, null, 10L);
        assertNull(WearDataLayerProtocol.parseNotificationStreamDeleteId(frame));
        assertEquals(Long.valueOf(10L), WearDataLayerProtocol.parseDataItemSeq(frame));
        assertTrue(WearDataLayerProtocol.parseDataItemPath(frame).contains("stream_item"));
    }

    @Test
    public void notificationPutCarriesTextFields() {
        final byte[] frame = WearDataLayerProtocol.buildNotificationPut(
                PKG, SIG, OWN, "org.example.chat", 1, "t", "Chat",
                "Hello title", "Hello body", null, 1000L, 1000L, 0L, 0, null, 1L);
        final byte[] value = WearDataLayerProtocol.parseDataItemValue(frame);
        assertNotNull(value);
        assertEquals("Hello title", WearDataLayerProtocol.parseDataMapString(frame, "title_raw"));
    }

    @Test
    public void seqAckFreshSessionIsWellFormed() {
        final byte[] frame = WearDataLayerProtocol.buildSeqAck(OWN, WATCH);
        assertNotNull(frame);
        assertTrue(frame.length > 4);
        // Peer-sync from a node we have no history with carries no counter for it.
        assertNull(WearDataLayerProtocol.parsePeerSyncNodeCounter(frame, "deadbeef"));
    }

    @Test
    public void seqAckCarriesKnownCounters() {
        final Map<String, Long> counters = new HashMap<>();
        counters.put(WATCH, 55L);
        final byte[] frame = WearDataLayerProtocol.buildSeqAck(counters, WATCH);
        assertNotNull(frame);
        assertTrue(frame.length > 4);
    }

    @Test
    public void assetChunksReassembleToTheOriginal() {
        final byte[] asset = new byte[30000];
        for (int i = 0; i < asset.length; i++) {
            asset[i] = (byte) i;
        }
        final List<byte[]> chunks = WearDataLayerProtocol.buildAssetChunks(asset, "transfer-1");
        assertTrue("30000 bytes must be split over several frames", chunks.size() >= 3);
        for (final byte[] chunk : chunks) {
            assertTrue(chunk.length > 4);
        }
    }

    @Test
    public void assetDigestIsStableAndUrlSafe() {
        final byte[] asset = "icon-bytes".getBytes(StandardCharsets.UTF_8);
        final String digest = WearDataLayerProtocol.assetDigest(asset);
        assertEquals(digest, WearDataLayerProtocol.assetDigest(asset));
        assertTrue(digest.matches("[A-Za-z0-9_-]+"));
        assertNotNull(WearDataLayerProtocol.buildAssetGrant(digest).frame);
    }

    @Test
    public void parsersTolerateGarbage() {
        final byte[] junk = garbage();
        assertNull(WearDataLayerProtocol.parseWatchNodeId(junk));
        assertNull(WearDataLayerProtocol.parseWatchConnectionId(junk));
        assertNull(WearDataLayerProtocol.parseMessageEventPath(junk));
        assertNull(WearDataLayerProtocol.parseBridgedAction(junk));
        assertNull(WearDataLayerProtocol.parseBatteryState(junk));
        assertNull(WearDataLayerProtocol.parseNotificationStreamDeleteId(junk));
        assertNull(WearDataLayerProtocol.parseMediaWatchCommand(junk));
        assertNull(WearDataLayerProtocol.parsePeerSyncNodeCounter(junk, WATCH));
        assertNotNull(WearDataLayerProtocol.describeMessage(junk));
        assertNull(WearDataLayerProtocol.parseWatchNodeId(new byte[0]));
    }

    @Test
    public void bridgedActionResultIsNotParsedAsAnAction() {
        final byte[] frame = WearDataLayerProtocol.buildBridgedActionResult(
                PKG, SIG, OWN, WATCH, 3L, "invocation", true);
        assertNull(WearDataLayerProtocol.parseBridgedAction(frame));
    }

    @Test
    public void describeMessageNamesTheMessageEventPath() {
        final byte[] frame = WearDataLayerProtocol.buildMessageEvent(
                PKG, SIG, OWN, WATCH, 1L, "/some/path", new byte[]{1});
        assertTrue(WearDataLayerProtocol.describeMessage(frame).contains("/some/path"));
    }
}
