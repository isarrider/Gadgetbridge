package nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.services;

import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;

import com.google.protobuf.ByteString;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.externalevents.IntentApiReceiver;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.Rpk;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkInfoList;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkList;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkMessage;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.XiaomiProto;
import nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiSupport;
import nodomain.freeyourgadget.gadgetbridge.test.GBTestApplication;

/**
 * Order of commands when messaging a watch app: rpk list (only while the app's sha is unknown),
 * launch, then message and app status (CONNECTED) once the band reports the app launched. Without a
 * pending message, the status is DISCONNECTED while app messages are disabled, UNINSTALLED without
 * an Android app of the same package name, and CONNECTED otherwise.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, application = GBTestApplication.class)
public class XiaomiRpkMessageFlowTest {
    private static final String APP_ID = "org.example.watchapp";
    private static final ByteString SHA = ByteString.copyFrom(new byte[20]);
    private static final byte[] PAYLOAD = "{\"value\":1}".getBytes(StandardCharsets.UTF_8);
    private static final String ADDRESS = "AA:BB:CC:DD:EE:FF";

    private XiaomiSupport support;
    private XiaomiRpkService service;

    @Before
    public void setUp() {
        support = Mockito.mock(XiaomiSupport.class);
        Mockito.when(support.getWatchfaceService()).thenReturn(Mockito.mock(XiaomiWatchfaceService.class));
        final GBDevice device = Mockito.mock(GBDevice.class);
        Mockito.when(device.getAddress()).thenReturn(ADDRESS);
        Mockito.when(support.getDevice()).thenReturn(device);
        Mockito.when(support.getContext()).thenReturn(RuntimeEnvironment.getApplication());
        service = new XiaomiRpkService(support);
    }

    @Test
    public void unknownAppFetchesListThenLaunchesThenSends() {
        service.sendWearAppMessage(APP_ID, PAYLOAD);
        Mockito.verify(support).sendCommand(Mockito.anyString(), Mockito.eq(XiaomiRpkService.COMMAND_TYPE),
                Mockito.eq(XiaomiRpkService.CMD_RPK_LIST));
        Assert.assertTrue(sentCommands().isEmpty());

        service.handleCommand(rpkList(APP_ID));
        final XiaomiProto.Command launch = single(sentCommands());
        Assert.assertEquals(XiaomiRpkService.CMD_RPK_LAUNCH, launch.getSubtype());
        Assert.assertEquals(APP_ID, launch.getRpk().getRpkLaunch().getApp().getId());
        Assert.assertEquals(SHA, launch.getRpk().getRpkLaunch().getApp().getSha());
        Assert.assertEquals(APP_ID, launch.getRpk().getRpkLaunch().getWearPackage());

        service.handleCommand(launched(APP_ID));
        final List<XiaomiProto.Command> sent = sentCommands();
        Assert.assertEquals(3, sent.size());
        Assert.assertEquals(XiaomiRpkService.CMD_RPK_MESSAGE_TO_WATCH, sent.get(1).getSubtype());
        Assert.assertArrayEquals(PAYLOAD, sent.get(1).getRpk().getRpkMessage().getPayload().toByteArray());
        Assert.assertEquals(XiaomiRpkService.CMD_RPK_APP_STATUS, sent.get(2).getSubtype());
    }

    @Test
    public void knownAppIsLaunchedWithoutFetchingTheList() {
        service.handleCommand(rpkList(APP_ID));
        service.sendWearAppMessage(APP_ID, PAYLOAD);
        Assert.assertEquals(XiaomiRpkService.CMD_RPK_LAUNCH, single(sentCommands()).getSubtype());
        Mockito.verify(support, Mockito.never()).sendCommand(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void appMissingOnTheBandIsDropped() {
        service.sendWearAppMessage(APP_ID, PAYLOAD);
        service.handleCommand(rpkList("org.example.other"));
        Assert.assertTrue(sentCommands().isEmpty());

        // a later launch notification for it carries no message
        service.handleCommand(launched(APP_ID));
        Assert.assertTrue(sentCommands().stream().noneMatch(cmd -> cmd.getSubtype() == XiaomiRpkService.CMD_RPK_MESSAGE_TO_WATCH));
    }

    @Test
    public void launchWithoutPendingMessageOnlySendsStatus() {
        service.handleCommand(rpkList(APP_ID));
        service.handleCommand(launched(APP_ID));
        Assert.assertEquals(XiaomiRpkService.CMD_RPK_APP_STATUS, single(sentCommands()).getSubtype());
    }

    @Test
    public void launchAfterTheQueueWasSentDoesNotResendIt() {
        service.handleCommand(rpkList(APP_ID));
        service.sendWearAppMessage(APP_ID, PAYLOAD);
        service.handleCommand(launched(APP_ID));
        service.handleCommand(launched(APP_ID));
        Assert.assertEquals(1, sentCommands().stream().filter(cmd -> cmd.getSubtype() == XiaomiRpkService.CMD_RPK_MESSAGE_TO_WATCH).count());
    }

    @Test
    public void disconnectDropsQueuedMessages() {
        service.handleCommand(rpkList(APP_ID));
        service.sendWearAppMessage(APP_ID, PAYLOAD);
        service.onDisconnect();
        service.handleCommand(launched(APP_ID));
        Assert.assertTrue(sentCommands().stream().noneMatch(cmd -> cmd.getSubtype() == XiaomiRpkService.CMD_RPK_MESSAGE_TO_WATCH));
    }

    @Test
    public void launchWhileNotAllowedSendsDisconnected() {
        service.handleCommand(launched(APP_ID));
        Assert.assertEquals(XiaomiRpkService.RPK_APP_STATUS_DISCONNECTED, sentStatus());
    }

    @Test
    public void launchWithoutCompanionAppSendsUninstalled() {
        allowWatchAppMessages();
        service.handleCommand(launched(APP_ID));
        Assert.assertEquals(XiaomiRpkService.RPK_APP_STATUS_UNINSTALLED, sentStatus());
    }

    @Test
    public void launchWithCompanionAppSendsConnected() {
        allowWatchAppMessages();
        // the test app itself stands in for an installed companion app
        service.handleCommand(launched(RuntimeEnvironment.getApplication().getPackageName()));
        Assert.assertEquals(XiaomiRpkService.RPK_APP_STATUS_CONNECTED, sentStatus());
    }

    @Test
    public void messageFromWatchWhileNotAllowedIsDropped() {
        service.handleCommand(messageFromWatch(APP_ID, "{\"t\":\"ping\"}"));
        Assert.assertTrue(watchAppMessageBroadcasts().isEmpty());
    }

    @Test
    public void messageFromWatchGoesToCompanionApp() {
        allowWatchAppMessages();
        service.handleCommand(messageFromWatch(APP_ID, "{\"t\":\"ping\"}"));
        final List<Intent> broadcasts = watchAppMessageBroadcasts();
        Assert.assertEquals(1, broadcasts.size());
        final Intent intent = broadcasts.get(0);
        Assert.assertEquals(APP_ID, intent.getPackage());
        Assert.assertEquals(ADDRESS, intent.getStringExtra(IntentApiReceiver.EXTRA_DEVICE));
        Assert.assertEquals(APP_ID, intent.getStringExtra(XiaomiRpkService.EXTRA_APP_ID));
        Assert.assertEquals("{\"t\":\"ping\"}", intent.getStringExtra(XiaomiRpkService.EXTRA_PAYLOAD));
    }

    private static List<Intent> watchAppMessageBroadcasts() {
        return shadowOf(RuntimeEnvironment.getApplication()).getBroadcastIntents().stream()
                .filter(intent -> XiaomiRpkService.ACTION_WATCH_APP_MESSAGE.equals(intent.getAction()))
                .collect(Collectors.toList());
    }

    private static void allowWatchAppMessages() {
        GBApplication.getDeviceSpecificSharedPrefs(ADDRESS).edit()
                .putBoolean(IntentApiReceiver.THIRD_PARTY_APPS_WATCH_APP_MESSAGES, true)
                .commit();
    }

    private int sentStatus() {
        final XiaomiProto.Command cmd = single(sentCommands());
        Assert.assertEquals(XiaomiRpkService.CMD_RPK_APP_STATUS, cmd.getSubtype());
        return cmd.getRpk().getRpkAppStatus().getStatus();
    }

    private List<XiaomiProto.Command> sentCommands() {
        final ArgumentCaptor<XiaomiProto.Command> captor = ArgumentCaptor.forClass(XiaomiProto.Command.class);
        Mockito.verify(support, Mockito.atLeast(0)).sendCommand(Mockito.anyString(), captor.capture());
        return captor.getAllValues();
    }

    private static XiaomiProto.Command single(final List<XiaomiProto.Command> commands) {
        Assert.assertEquals(1, commands.size());
        return commands.get(0);
    }

    private static XiaomiProto.Command rpkList(final String id) {
        return XiaomiProto.Command.newBuilder()
                .setType(XiaomiRpkService.COMMAND_TYPE)
                .setSubtype(XiaomiRpkService.CMD_RPK_LIST)
                .setRpk(Rpk.newBuilder().setRpkList(RpkList.newBuilder().addRpkInfo(
                        RpkInfoList.newBuilder().setId(id).setSha(SHA).setName("Watch app"))))
                .build();
    }

    private static XiaomiProto.Command messageFromWatch(final String id, final String payload) {
        return XiaomiProto.Command.newBuilder()
                .setType(XiaomiRpkService.COMMAND_TYPE)
                .setSubtype(XiaomiRpkService.CMD_RPK_MESSAGE_FROM_WATCH)
                .setRpk(Rpk.newBuilder().setRpkMessage(RpkMessage.newBuilder()
                        .setApp(XiaomiRpkService.rpkRef(id, SHA))
                        .setPayload(ByteString.copyFromUtf8(payload))))
                .build();
    }

    private static XiaomiProto.Command launched(final String id) {
        return XiaomiProto.Command.newBuilder()
                .setType(XiaomiRpkService.COMMAND_TYPE)
                .setSubtype(XiaomiRpkService.CMD_RPK_LAUNCHED)
                .setRpk(Rpk.newBuilder().setRpkDel(XiaomiRpkService.rpkRef(id, SHA)))
                .build();
    }
}
