package nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi;

import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.GlucoseSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.GlucoseSample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.services.XiaomiRpkService;
import nodomain.freeyourgadget.gadgetbridge.test.TestBase;

public class XiaomiCgmSenderTest extends TestBase {
    private static final String APP_ID = "org.example.cgmapp";

    private GBDevice device;
    private XiaomiSupport support;
    private XiaomiRpkService rpk;
    private XiaomiCgmSender sender;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        device = createDummyGDevice("00:11:22:33:44:66");
        DBHelper.getDevice(device, daoSession);
        support = Mockito.mock(XiaomiSupport.class);
        Mockito.when(support.getDevice()).thenReturn(device);
        Mockito.when(support.getContext()).thenReturn(getContext());
        rpk = Mockito.mock(XiaomiRpkService.class);
        Mockito.when(support.getRpkService()).thenReturn(rpk);
        sender = new XiaomiCgmSender(support, Runnable::run);
        prefs().putString(DeviceSettingsPreferenceConst.PREF_CGM_WATCH_APP_ID, APP_ID).commit();
    }

    private android.content.SharedPreferences.Editor prefs() {
        return GBApplication.getDeviceSpecificSharedPrefs(device.getAddress()).edit();
    }

    private static Bundle broadcast(final long time, final double mgdl, final String units) {
        final Bundle b = new Bundle();
        b.putDouble("com.eveningoutpost.dexdrip.Extras.BgEstimate", mgdl);
        b.putDouble("com.eveningoutpost.dexdrip.Extras.BgSlope", -3.0 / 300_000);
        b.putString("com.eveningoutpost.dexdrip.Extras.BgSlopeName", "FortyFiveDown");
        b.putLong("com.eveningoutpost.dexdrip.Extras.Time", time);
        b.putString("com.eveningoutpost.dexdrip.Extras.Display.Units", units);
        return b;
    }

    private static void idle() {
        shadowOf(Looper.getMainLooper()).idle();
    }

    private static void waitFor(final long millis) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis));
    }

    /** @return the action the sender asked to run once the watch app reports it launched, the last one if several */
    private Runnable launchAction() {
        idle();
        final ArgumentCaptor<Runnable> action = ArgumentCaptor.forClass(Runnable.class);
        Mockito.verify(rpk, Mockito.atLeastOnce()).startWearApp(Mockito.eq(APP_ID), action.capture());
        return action.getValue();
    }

    /** The watch app reports it launched: runs the launch action on the Bluetooth thread, as the band would. */
    private void appLaunched(final Runnable action) throws Exception {
        final Thread bluetooth = new Thread(action);
        bluetooth.start();
        bluetooth.join();
        idle();
    }

    private List<byte[]> sentMessages() {
        final ArgumentCaptor<byte[]> payload = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(rpk, Mockito.atLeast(0)).sendWearAppMessage(Mockito.eq(APP_ID), payload.capture());
        return payload.getAllValues();
    }

    private JSONObject lastMessage() throws Exception {
        appLaunched(launchAction());
        final List<byte[]> sent = sentMessages();
        Assert.assertFalse("no message sent", sent.isEmpty());
        return new JSONObject(new String(sent.get(sent.size() - 1), StandardCharsets.UTF_8));
    }

    private List<GlucoseSample> stored() {
        return new GlucoseSampleProvider(device, daoSession).getAllSamples(0, Long.MAX_VALUE);
    }

    @Test
    public void storesEachReadingAndSendsTheGraph() throws Exception {
        final long now = System.currentTimeMillis();
        sender.onReading(broadcast(now - 5 * 60_000, 160, "mg/dl"));
        sender.onReading(broadcast(now - 1000, 157, "mg/dl"));

        Assert.assertEquals(2, stored().size());
        final JSONObject message = lastMessage();
        final JSONObject bg = message.getJSONObject("bg");
        Assert.assertEquals("157", bg.getString("val"));
        Assert.assertEquals("-3", bg.getString("delta"));
        Assert.assertEquals("FortyFiveDown", bg.getString("trend"));
        final JSONArray inRange = message.getJSONObject("graph").getJSONArray("lines").getJSONObject(0).getJSONArray("points");
        Assert.assertEquals(2, inRange.length());
    }

    @Test
    public void usesTheUnitsOfTheCgmApp() throws Exception {
        sender.onReading(broadcast(System.currentTimeMillis(), 153, "mmol"));
        final JSONObject message = lastMessage();
        Assert.assertEquals("8.5", message.getJSONObject("bg").getString("val"));
        Assert.assertFalse(message.getJSONObject("status").getBoolean("isMgdl"));
    }

    @Test
    public void usesTheGlucoseLimits() throws Exception {
        prefs().putString(DeviceSettingsPreferenceConst.PREF_GLUCOSE_THRESHOLD_HIGH, "150").commit();
        sender.onReading(broadcast(System.currentTimeMillis(), 153, "mg/dl"));
        Assert.assertTrue(lastMessage().getJSONObject("bg").getBoolean("isHigh"));
    }

    @Test
    public void withoutWatchAppOnlyStores() {
        prefs().remove(DeviceSettingsPreferenceConst.PREF_CGM_WATCH_APP_ID).commit();
        sender.onReading(broadcast(System.currentTimeMillis(), 153, "mg/dl"));
        shadowOf(Looper.getMainLooper()).idle();
        Assert.assertEquals(1, stored().size());
        Mockito.verifyNoInteractions(rpk);
    }

    @Test
    public void ignoresBroadcastWithoutReading() {
        sender.onReading(new Bundle());
        shadowOf(Looper.getMainLooper()).idle();
        Assert.assertTrue(stored().isEmpty());
        Mockito.verifyNoInteractions(rpk);
    }

    @Test
    public void launchesTheWatchAppAndSendsOnceItReportsLaunched() throws Exception {
        sender.onReading(broadcast(System.currentTimeMillis(), 153, "mg/dl"));
        final Runnable launched = launchAction();
        Assert.assertTrue("sent before the app launched", sentMessages().isEmpty());

        appLaunched(launched);
        Assert.assertEquals(1, sentMessages().size());

        // no second copy when the launch wait runs out
        waitFor(XiaomiCgmSender.LAUNCH_TIMEOUT_MS * 2);
        Assert.assertEquals(1, sentMessages().size());
    }

    @Test
    public void sendsAnywayWhenTheLaunchIsNotAnswered() throws Exception {
        // the band does not answer a launch while the app is already open on its screen
        sender.onReading(broadcast(System.currentTimeMillis(), 153, "mg/dl"));
        final Runnable launched = launchAction();
        waitFor(XiaomiCgmSender.LAUNCH_TIMEOUT_MS - 1);
        Assert.assertTrue(sentMessages().isEmpty());

        waitFor(1);
        Assert.assertEquals(1, sentMessages().size());
        Mockito.verify(rpk).cancelLaunchAction(APP_ID, launched);

        // a late answer does not send it again
        appLaunched(launched);
        Assert.assertEquals(1, sentMessages().size());
    }

    @Test
    public void unansweredLaunchesDoNotHoldLaterReadings() throws Exception {
        final long now = System.currentTimeMillis();
        sender.onReading(broadcast(now - 5 * 60_000, 160, "mg/dl"));
        idle();
        waitFor(XiaomiCgmSender.LAUNCH_TIMEOUT_MS);
        sender.onReading(broadcast(now, 157, "mg/dl"));
        idle();
        waitFor(XiaomiCgmSender.LAUNCH_TIMEOUT_MS);

        final List<byte[]> sent = sentMessages();
        Assert.assertEquals(2, sent.size());
        Assert.assertEquals("157", new JSONObject(new String(sent.get(1), StandardCharsets.UTF_8))
                .getJSONObject("bg").getString("val"));
    }

    @Test
    public void eachReadingIsSentOnceWhenOneLaunchAnswersBoth() throws Exception {
        final long now = System.currentTimeMillis();
        sender.onReading(broadcast(now - 1000, 160, "mg/dl"));
        sender.onReading(broadcast(now, 157, "mg/dl"));
        idle();
        final ArgumentCaptor<Runnable> actions = ArgumentCaptor.forClass(Runnable.class);
        Mockito.verify(rpk, Mockito.times(2)).startWearApp(Mockito.eq(APP_ID), actions.capture());

        for (final Runnable launched : actions.getAllValues()) {
            appLaunched(launched);
        }
        waitFor(XiaomiCgmSender.LAUNCH_TIMEOUT_MS * 2);
        Assert.assertEquals(2, sentMessages().size());
    }

    @Test
    public void stopDropsAReadingWaitingForTheLaunch() throws Exception {
        sender.onReading(broadcast(System.currentTimeMillis(), 153, "mg/dl"));
        final Runnable launched = launchAction();
        sender.stop();
        Mockito.verify(rpk).cancelLaunchAction(APP_ID, launched);

        waitFor(XiaomiCgmSender.LAUNCH_TIMEOUT_MS * 2);
        appLaunched(launched);
        Assert.assertTrue(sentMessages().isEmpty());
    }
}
