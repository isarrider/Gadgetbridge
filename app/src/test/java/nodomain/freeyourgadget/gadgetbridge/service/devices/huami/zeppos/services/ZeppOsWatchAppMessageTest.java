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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.externalevents.IntentApiReceiver;
import nodomain.freeyourgadget.gadgetbridge.externalevents.WatchAppCompanionRegistry;
import nodomain.freeyourgadget.gadgetbridge.externalevents.WatchAppMessageIntents;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.zeppos.ZeppOsSupport;
import nodomain.freeyourgadget.gadgetbridge.test.TestBase;

public class ZeppOsWatchAppMessageTest extends TestBase {
    private static final String ADDRESS = "AA:BB:CC:DD:EE:01";
    private static final String OTHER = "AA:BB:CC:DD:EE:02";
    private static final String APP_ID = "391257";
    private static final byte[] BODY = new byte[] {0x00, (byte) 0xff, 0x01};

    private ZeppOsSupport support;
    private GBDevice device;
    private ZeppOsAppsService service;

    @Before
    public void setUpService() {
        WatchAppCompanionRegistry.getInstance().clear();
        support = mock(ZeppOsSupport.class);
        device = mock(GBDevice.class);
        when(device.getAddress()).thenReturn(ADDRESS);
        when(device.getState()).thenReturn(GBDevice.State.INITIALIZED);
        when(device.getAliasOrName()).thenReturn("Watch");
        when(support.getDevice()).thenReturn(device);
        when(support.getContext()).thenReturn(app);
        service = new ZeppOsAppsService(support);
        GBApplication.getDeviceSpecificSharedPrefs(ADDRESS).edit().clear().commit();
        GBApplication.getDeviceSpecificSharedPrefs(OTHER).edit().clear().commit();
    }

    @Test
    public void phoneToWatchRequiresOptInDecimalIdBase64AndInitializedWatch() {
        service.sendWatchAppMessage(APP_ID, base64(BODY));
        verify(support, never()).writeToChunked2021(anyString(), anyShort(), any(byte[].class), anyBoolean());

        allow();
        service.sendWatchAppMessage("org.example.watchapp", base64(BODY));
        service.sendWatchAppMessage(APP_ID, "{\"value\":1}".getBytes(StandardCharsets.UTF_8));
        service.sendWatchAppMessage(APP_ID, "%%%".getBytes(StandardCharsets.UTF_8));
        when(device.getState()).thenReturn(GBDevice.State.NOT_CONNECTED);
        service.sendWatchAppMessage(APP_ID, base64(BODY));
        verify(support, never()).writeToChunked2021(anyString(), anyShort(), any(byte[].class), anyBoolean());

        when(device.getState()).thenReturn(GBDevice.State.INITIALIZED);
        service.sendWatchAppMessage(APP_ID, base64(BODY));
        final byte[] frame = captureWrite("js app message");
        final ZeppOsJsMessage.Frame parsed = ZeppOsJsMessage.parse(frame);
        assertNotNull(parsed);
        assertEquals(0, parsed.port1);
        assertArrayEquals(BODY, parsed.body);
    }

    @Test
    public void openCloseAndReconnectDoNotQueueOrLeakTheSession() {
        allow();
        service.handlePayload(openFrame());
        final byte[] ack = captureWrite("js app open");
        assertEquals(ZeppOsJsMessage.PHONE_PORT, ZeppOsJsMessage.parse(ack).port2);

        clearInvocations(support);
        service.sendWatchAppMessage(APP_ID, base64(BODY));
        assertEquals(20, ZeppOsJsMessage.parse(captureWrite("js app message")).port1);

        clearInvocations(support);
        final byte[] close = openFrame();
        close[2] = ZeppOsJsMessage.TYPE_CLOSE;
        service.handlePayload(close);
        verify(support, never()).writeToChunked2021(anyString(), anyShort(), any(byte[].class), anyBoolean());

        service.sendWatchAppMessage(APP_ID, base64(BODY));
        assertEquals(0, ZeppOsJsMessage.parse(captureWrite("js app message")).port1);

        clearInvocations(support);
        when(device.getState()).thenReturn(GBDevice.State.WAITING_FOR_RECONNECT);
        service.dispose();
        service.sendWatchAppMessage(APP_ID, base64(BODY));
        verify(support, never()).writeToChunked2021(anyString(), anyShort(), any(byte[].class), anyBoolean());

        when(device.getState()).thenReturn(GBDevice.State.INITIALIZED);
        service.handlePayload(openFrame());
        assertEquals(20, ZeppOsJsMessage.parse(captureWrite("js app open")).port1);
    }

    @Test
    public void watchToPhoneReachesOnlyTheInstalledCompanion() {
        allow();
        final int before = broadcasts().size();
        service.handlePayload(messageFrame());
        assertEquals(before, broadcasts().size());

        assertTrue(WatchAppCompanionRegistry.getInstance().bind(ADDRESS, APP_ID, "com.example.missing"));
        service.handlePayload(messageFrame());
        assertEquals(before, broadcasts().size());

        final String installed = app.getPackageName();
        assertTrue(WatchAppCompanionRegistry.getInstance().bind(ADDRESS, APP_ID, installed));
        assertTrue(WatchAppCompanionRegistry.getInstance().bind(OTHER, APP_ID, installed));
        service.handlePayload(messageFrame());
        final List<Intent> delivered = broadcasts().subList(before, broadcasts().size());
        assertEquals(1, delivered.size());
        final Intent intent = delivered.get(0);
        assertEquals(WatchAppMessageIntents.ACTION_WATCH_APP_MESSAGE, intent.getAction());
        assertEquals(installed, intent.getPackage());
        assertEquals(ADDRESS, intent.getStringExtra(IntentApiReceiver.EXTRA_DEVICE));
        assertEquals(APP_ID, intent.getStringExtra(WatchAppMessageIntents.EXTRA_APP_ID));
        assertArrayEquals(BODY, ZeppOsJsMessage.decodePayload(intent.getStringExtra(WatchAppMessageIntents.EXTRA_PAYLOAD)));

        GBApplication.getDeviceSpecificSharedPrefs(ADDRESS).edit()
                .putBoolean(IntentApiReceiver.THIRD_PARTY_APPS_WATCH_APP_MESSAGES, false)
                .commit();
        final int after = broadcasts().size();
        service.handlePayload(messageFrame());
        service.handlePayload(openFrame());
        assertEquals(after, broadcasts().size());
        verify(support, never()).writeToChunked2021(anyString(), anyShort(), any(byte[].class), anyBoolean());
    }

    @Test
    public void malformedAndUnknownJsPayloadsAreDropped() {
        allow();
        assertTrue(WatchAppCompanionRegistry.getInstance().bind(ADDRESS, APP_ID, app.getPackageName()));
        final int before = broadcasts().size();
        service.handlePayload(new byte[] {ZeppOsJsMessage.CMD_JS, 0x01});
        final byte[] unknown = openFrame();
        unknown[2] = 9;
        service.handlePayload(unknown);
        service.handlePayload(new byte[0]);
        assertEquals(before, broadcasts().size());
        verify(support, never()).writeToChunked2021(anyString(), anyShort(), any(byte[].class), anyBoolean());
    }

    @Test
    public void explicitIntentRefusesANumericPackage() {
        assertNull(WatchAppMessageIntents.incoming(ADDRESS, APP_ID, "YQ==", APP_ID));
        assertNull(WatchAppMessageIntents.incoming(ADDRESS, APP_ID, "YQ==", "not-a-package"));
        final Intent intent = WatchAppMessageIntents.incoming(ADDRESS, APP_ID, "", "com.example.one");
        assertNotNull(intent);
        assertEquals("com.example.one", intent.getPackage());
        assertEquals("", intent.getStringExtra(WatchAppMessageIntents.EXTRA_PAYLOAD));
    }

    private void allow() {
        GBApplication.getDeviceSpecificSharedPrefs(ADDRESS).edit()
                .putBoolean(IntentApiReceiver.THIRD_PARTY_APPS_WATCH_APP_MESSAGES, true)
                .commit();
    }

    private byte[] captureWrite(final String task) {
        final ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(support).writeToChunked2021(eq(task), eq((short) 0x00a0), captor.capture(), eq(false));
        return captor.getValue();
    }

    private List<Intent> broadcasts() {
        return new ArrayList<>(shadowOf((Application) app).getBroadcastIntents());
    }

    private static byte[] base64(final byte[] body) {
        return ZeppOsJsMessage.encodePayload(body).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] openFrame() {
        return new byte[] {
                0x01, 0x01,
                0x01, 0x00,
                0x14, 0x00,
                0x00, 0x00,
                0x59, (byte) 0xf8, 0x05, 0x00,
                0x00, 0x00, 0x00, 0x00
        };
    }

    private static byte[] messageFrame() {
        final byte[] frame = ZeppOsJsMessage.encodeMessage(391257, 20, BODY);
        assertNotNull(frame);
        return frame;
    }
}
