/*  Copyright (C) 2023-2024 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.services;

import android.content.Intent;

import com.google.protobuf.ByteString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventAppInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.XiaomiFWHelper;
import nodomain.freeyourgadget.gadgetbridge.externalevents.IntentApiReceiver;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceApp;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.Rpk;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkAppStatus;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkInfo;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkInfoList;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkLaunch;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkList;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.RpkMessage;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.XiaomiProto;
import nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiSupport;
import nodomain.freeyourgadget.gadgetbridge.util.AndroidUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class XiaomiRpkService extends AbstractXiaomiService implements XiaomiDataUploadService.Callback {
    private static final Logger LOG = LoggerFactory.getLogger(XiaomiRpkService.class);

    public static final int COMMAND_TYPE = 20;

    public static final int CMD_RPK_LIST = 0;
    public static final int CMD_RPK_SET = 1;
    public static final int CMD_RPK_DELETE = 3;
    public static final int CMD_RPK_INSTALL = 1;
    public static final int CMD_RPK_INSTALLED = 2;
    public static final int CMD_RPK_LAUNCH = 4;
    public static final int CMD_RPK_LAUNCHED = 6;
    public static final int CMD_RPK_APP_STATUS = 7;
    public static final int CMD_RPK_MESSAGE_TO_WATCH = 8;
    public static final int CMD_RPK_MESSAGE_FROM_WATCH = 9;

    public static final int RPK_APP_STATUS_CONNECTED = 1;
    public static final int RPK_APP_STATUS_DISCONNECTED = 2;
    public static final int RPK_APP_STATUS_UNINSTALLED = 3;

    // Broadcast to the companion app with the same package as the wear app that sent the message
    public static final String ACTION_WATCH_APP_MESSAGE = "nodomain.freeyourgadget.gadgetbridge.action.WATCH_APP_MESSAGE";
    public static final String EXTRA_APP_ID = "appId";
    public static final String EXTRA_PAYLOAD = "payload";

    // Actions waiting for rpk list, by rpk id, oldest first.
    private final Map<String, List<Consumer<RpkInfoList>>> awaitingSha = new HashMap<>();
    // Actions waiting for the band to report the app launched, by rpk id, oldest first. Each runs once.
    // Guarded by itself: written by callers of startWearApp and read on the Bluetooth thread.
    private final Map<String, List<Runnable>> awaitingLaunch = new HashMap<>();
    // Whether the companion app of each rpk id said it is listening, kept across band reconnects.
    private final Map<String, Boolean> companionAppConnected = new HashMap<>();


    // Not null if we're installing a firmware
    private XiaomiFWHelper fwHelper = null;

    public XiaomiRpkService(final XiaomiSupport support) {
        super(support);
    }

    private final List<GBDeviceApp> apps = new ArrayList<>();
    private final Map<String, ByteString> shaMap = new HashMap<>();

    @Override
    public void handleCommand(final XiaomiProto.Command cmd) {
        switch (cmd.getSubtype()) {
            case CMD_RPK_LAUNCHED:
                onWearAppLaunched(cmd.getRpk().getRpkDel());
                return;
            case CMD_RPK_MESSAGE_FROM_WATCH:
                onWearAppMessage(cmd.getRpk().getRpkMessage());
                return;
            case CMD_RPK_LIST:
                handleRpkList(cmd.getRpk().getRpkList());
                return;
            case CMD_RPK_INSTALLED:
                LOG.debug("Got rpk install complete notification");
                requestRpkList();
                return;
            case CMD_RPK_DELETE:
                // Band may send a delete response in some cases
                LOG.debug("Got rpk delete response");
                requestRpkList();
                return;
            case CMD_RPK_INSTALL:
                final int installStatus = cmd.getRpk().getRpkInstallStart().getCmd();
                LOG.debug("Got rpk install response, status={}", installStatus);
                if (installStatus != 0) {
                    LOG.warn("Rpk install rejected with status {} for {}", installStatus, fwHelper != null ? fwHelper.getId() : "null");
                    return;
                }
                LOG.debug("Rpk install accepted, starting upload");
                setDeviceBusy();
                getSupport().getDataUploadService().setCallback(this);
                getSupport().getDataUploadService().requestUpload(XiaomiDataUploadService.TYPE_RPK, fwHelper.getBytes());
                return;
        }

        LOG.warn("Unknown rpk command {}: {}", cmd.getSubtype(), GB.hexdump(cmd.toByteArray()));
    }

    /**
     * Send a message to an app installed on the band. Like the official app, this offers no
     * guarantees about delivery. The wear app must be active for a message to reach it.
     *
     * @param appId rpk id as reported in the rpk list
     */
    public void sendWearAppMessage(final String appId, final byte[] payload) {
        withRpkRef(appId, ref -> getSupport().sendCommand("wear app message " + appId, buildMessageCommand(ref, payload)));
    }

    /**
     * Launch an app installed on the band.
     *
     * @param appId rpk id as reported in the rpk list
     */
    public void startWearApp(final String appId) {
        withRpkRef(appId, ref -> launchWearApp(appId, ref));
    }

    /**
     * Launch an app installed on the band and run an action once the band reports it launched
     * (CMD_RPK_LAUNCHED), for example to send a message the app only receives while open.
     * The band does not always answer: an app already open on its screen does not report another
     * launch. The action is dropped on disconnect, when the app is not installed, or by
     * {@link #cancelLaunchAction}, so a caller that waits for it should also have a timeout.
     *
     * @param appId rpk id as reported in the rpk list
     * @param onLaunched runs at most once, on the thread handling band commands
     */
    public void startWearApp(final String appId, final Runnable onLaunched) {
        synchronized (awaitingLaunch) {
            awaitingLaunch.computeIfAbsent(appId, k -> new ArrayList<>()).add(onLaunched);
        }
        startWearApp(appId);
    }

    /**
     * Drop an action passed to {@link #startWearApp(String, Runnable)} that has not run yet.
     */
    public void cancelLaunchAction(final String appId, final Runnable onLaunched) {
        synchronized (awaitingLaunch) {
            final List<Runnable> actions = awaitingLaunch.get(appId);
            if (actions != null && actions.remove(onLaunched) && actions.isEmpty()) {
                awaitingLaunch.remove(appId);
            }
        }
    }

    private void runLaunchActions(final String appId) {
        final List<Runnable> actions;
        synchronized (awaitingLaunch) {
            actions = awaitingLaunch.remove(appId);
        }
        if (actions == null) {
            return;
        }
        LOG.debug("Wear app {} launched, running {} waiting actions", appId, actions.size());
        for (final Runnable action : actions) {
            action.run();
        }
    }

    private void dropLaunchActionsOfMissingApps() {
        synchronized (awaitingLaunch) {
            awaitingLaunch.keySet().retainAll(shaMap.keySet());
        }
    }

    private void launchWearApp(final String appId, final RpkInfoList rpkRef) {
        LOG.debug("Launching {}", appId);
        // Field 2 is the uri parameter in the SDK's launchWearApp. The band seems to ignore it
        // (Band 9, fw 2.2.90). The app always opens on its entry page, independent of which uri is
        // sent, or whether it's sent at all.
        getSupport().sendCommand("launch wear app " + appId, buildLaunchCommand(rpkRef, appId));
    }

    private void runAwaitingActions() {
        for (final Map.Entry<String, List<Consumer<RpkInfoList>>> entry : awaitingSha.entrySet()) {
            final ByteString sha = shaMap.get(entry.getKey());
            if (sha == null) {
                LOG.warn("Rpk {} not installed but action was waiting for it.", entry.getKey());
                continue;
            }

            final RpkInfoList ref = rpkRef(entry.getKey(), sha);
            for (final Consumer<RpkInfoList> action : entry.getValue()) {
                action.accept(ref);
            }
        }
        awaitingSha.clear();
    }

    private void onWearAppLaunched(final RpkInfoList app) {
        final RpkInfoList ref = rpkRef(app.getId(), app.getSha());
        // as the official app: messages first, then the companion app status
        runLaunchActions(app.getId());
        sendAppStatus(ref, companionAppStatus(app.getId()));
    }

    /**
     * The wear app waits for the status of its companion phone app (a phone app with the same
     * package name as the wear app) after it launches. If not received, getApkStatus() times out
     * on the watch.
     * Here, uninstalled means the companion app is not installed, connected means that there's an
     * active connection to the companion app, and disconnected means that connection was severed
     * (when third party app messages are disabled, or the companion app said it is not listening).
     */
    private int companionAppStatus(final String appId) {
        if (!watchAppMessagesAllowed()) {
            LOG.debug("Watch app messages not allowed for {}, companion app of {} disconnected", getSupport().getDevice().getAliasOrName(), appId);
            return RPK_APP_STATUS_DISCONNECTED;
        }
        if (!AndroidUtils.isPackageInstalled(appId)) {
            LOG.debug("Companion app {} not installed", appId);
            return RPK_APP_STATUS_UNINSTALLED;
        }
        final Boolean connected = companionAppConnected.get(appId);
        if (connected != null && !connected) {
            LOG.debug("Companion app {} said it is not listening", appId);
            return RPK_APP_STATUS_DISCONNECTED;
        }
        return RPK_APP_STATUS_CONNECTED;
    }

    /**
     * The companion app of a wear app says whether it is listening, just as an app binding or unbinding
     * the official app's SDK would.
     * Tell the wear app the new status now, and answer later status requests with this new status.
     *
     * @param appId rpk id as reported in the rpk list
     */
    public void setCompanionAppStatus(final String appId, final boolean connected) {
        companionAppConnected.put(appId, connected);
        withRpkRef(appId, ref -> sendAppStatus(ref, companionAppStatus(appId)));
    }

    private void sendAppStatus(final RpkInfoList app, final int status) {
        getSupport().sendCommand("wear app status " + app.getId(), buildAppStatusCommand(app, status));
    }

    /**
     * Send a message from a wear app to its companion phone app (same package name). The band only
     * lets wear apps send JSON.
     */
    private void onWearAppMessage(final RpkMessage message) {
        final String appId = message.getApp().getId();
        final GBDevice device = getSupport().getDevice();
        if (!watchAppMessagesAllowed()) {
            LOG.debug("Watch app messages not allowed for {}, dropping message from {}", device.getAliasOrName(), appId);
            return;
        }
        LOG.debug("Message from wear app {}, {} bytes", appId, message.getPayload().size());
        final Intent intent = new Intent(ACTION_WATCH_APP_MESSAGE)
                .setPackage(appId)
                .putExtra(IntentApiReceiver.EXTRA_DEVICE, device.getAddress())
                .putExtra(EXTRA_APP_ID, appId)
                .putExtra(EXTRA_PAYLOAD, message.getPayload().toStringUtf8());
        getSupport().getContext().sendBroadcast(intent);
    }

    private boolean watchAppMessagesAllowed() {
        return GBApplication.getDeviceSpecificSharedPrefs(getSupport().getDevice().getAddress())
                .getBoolean(IntentApiReceiver.THIRD_PARTY_APPS_WATCH_APP_MESSAGES, false);
    }

    @Override
    public void onDisconnect() {
        // Clear all pending actions, otherwise commands (and thus messages) from before the
        // disconnect would run on the next rpk list, possibly much later.
        awaitingSha.clear();
        synchronized (awaitingLaunch) {
            awaitingLaunch.clear();
        }
    }

    static RpkInfoList rpkRef(final String id, final ByteString sha) {
        return RpkInfoList.newBuilder().setId(id).setSha(sha).build();
    }

    /**
     * Runs an action whenever the sha of the rpk is known. If it's available immediately, the
     * action runs immediately. Otherwise, it's queued to be run when the rpk list is next received,
     * and the rpk list is requested.
     *
     * @param appId rpk id as reported in the rpk list.
     * @param action the action to be executed once the sha is obtained.
     */
    private void withRpkRef(final String appId, final Consumer<RpkInfoList> action) {
        final ByteString sha = shaMap.get(appId);
        if (sha != null) {
            action.accept(rpkRef(appId, sha));
        } else {
            // sha is only known from the rpk list, which is fetched on demand
            LOG.debug("Rpk {} not in the cached list, requesting it", appId);
            awaitingSha.computeIfAbsent(appId, k -> new ArrayList<>()).add(action);
            requestRpkList();
        }
    }

    static XiaomiProto.Command buildLaunchCommand(final RpkInfoList app, final String uri) {
        return XiaomiProto.Command.newBuilder()
                .setType(COMMAND_TYPE)
                .setSubtype(CMD_RPK_LAUNCH)
                .setRpk(Rpk.newBuilder().setRpkLaunch(RpkLaunch.newBuilder().setApp(app).setUri(uri)))
                .build();
    }

    static XiaomiProto.Command buildMessageCommand(final RpkInfoList app, final byte[] payload) {
        return XiaomiProto.Command.newBuilder()
                .setType(COMMAND_TYPE)
                .setSubtype(CMD_RPK_MESSAGE_TO_WATCH)
                .setRpk(Rpk.newBuilder().setRpkMessage(RpkMessage.newBuilder().setApp(app).setPayload(ByteString.copyFrom(payload))))
                .build();
    }

    static XiaomiProto.Command buildAppStatusCommand(final RpkInfoList app, final int status) {
        return XiaomiProto.Command.newBuilder()
                .setType(COMMAND_TYPE)
                .setSubtype(CMD_RPK_APP_STATUS)
                .setRpk(Rpk.newBuilder().setRpkAppStatus(RpkAppStatus.newBuilder().setApp(app).setStatus(status)))
                .build();
    }

    public void requestRpkList() {
        getSupport().sendCommand("request rpk list", COMMAND_TYPE, CMD_RPK_LIST);
    }

    public void deleteRpk(UUID uuid) {
        for (GBDeviceApp app : apps) {
            if (app.getUUID().equals(uuid)) {
                String packageName = app.getCreator();
                ByteString sha = shaMap.get(packageName);
                LOG.debug("Deleting rpk: id={}", packageName);
                getSupport().sendCommand("delete rpk",
                        XiaomiProto.Command.newBuilder()
                                .setType(COMMAND_TYPE)
                                .setSubtype(CMD_RPK_DELETE)
                                .setRpk(Rpk.newBuilder()
                                        .setRpkDel(
                                                RpkInfoList.newBuilder()
                                                        .setId(packageName)
                                                        .setSha(sha)
                                        )
                                )
                                .build());
                // Band sends no response to delete — request list immediately after
                requestRpkList();
                return;
            }
        }
        LOG.warn("Unknown rpk uuid {}", uuid);
    }

    public void installRpk(final XiaomiFWHelper fwHelper) {
        assert fwHelper.isValid();
        assert fwHelper.isRpk();

        this.fwHelper = fwHelper;

        LOG.debug("Sending install request for rpk: id={}, versionCode={}, size={}", fwHelper.getId(), fwHelper.getVersionCode(), fwHelper.getBytes().length);
        getSupport().sendCommand("install rpk " + fwHelper.getId(),
                XiaomiProto.Command.newBuilder()
                        .setType(COMMAND_TYPE)
                        .setSubtype(CMD_RPK_INSTALL)
                        .setRpk(Rpk.newBuilder()
                                .setRpkInfo(
                                        RpkInfo.newBuilder()
                                                .setId(fwHelper.getId())
                                                .setUnknown2(fwHelper.getVersionCode())
                                                .setSize(fwHelper.getBytes().length)
                                ))
                        .build());
    }

    private void handleRpkList(final RpkList rpkList) {
        LOG.debug("Got {} rpks", rpkList.getRpkInfoCount());
        apps.clear();
        shaMap.clear();
        for (var info : rpkList.getRpkInfoList()) {
            shaMap.put(info.getId(), info.getSha());
            String packageName = info.getId();
            String appName = info.getName();

            GBDeviceApp gbDeviceApp = new GBDeviceApp(UUID.nameUUIDFromBytes(packageName.getBytes()), appName, packageName, "", GBDeviceApp.Type.APP_GENERIC);
            apps.add(gbDeviceApp);
        }
        final List<GBDeviceApp> appsAndFaces = new ArrayList<>(apps);
        appsAndFaces.addAll(getSupport().getWatchfaceService().getInstalledFacesCache());
        final GBDeviceEventAppInfo appInfoCmd = new GBDeviceEventAppInfo();
        appInfoCmd.apps = appsAndFaces.toArray(new GBDeviceApp[0]);
        getSupport().evaluateGBDeviceEvent(appInfoCmd);

        runAwaitingActions();
        dropLaunchActionsOfMissingApps();
    }


    @Override
    public void onUploadFinish(final boolean success) {
        final int notificationMessage = success ? R.string.uploadrpkoperation_complete : R.string.uploadrpkoperation_failed;

        onUploadProgress(notificationMessage, 100, false);

        if (getSupport().getConnectionSpecificSupport() != null) {
            getSupport().getConnectionSpecificSupport().runOnQueue("rpk upload finish", () -> {
                LOG.debug("Rpk upload finished: {}", success);
                getSupport().getDataUploadService().setCallback(null);
                unsetDeviceBusy();

                // List refresh is triggered by CMD_RPK_INSTALLED notification from the band

                fwHelper = null;
            });
        }
    }

    @Override
    public void onUploadProgress(final int progressPercent) {
        onUploadProgress(R.string.uploadwatchfaceoperation_in_progress, progressPercent, true);
    }

    private void onUploadProgress(final int stringResource, final int progressPercent, final boolean ongoing) {
        if (getSupport().getConnectionSpecificSupport() != null)
            getSupport().getConnectionSpecificSupport().onUploadProgress(stringResource, progressPercent, ongoing);
    }

    private void setDeviceBusy() {
        final GBDevice device = getSupport().getDevice();
        device.setBusyTask(R.string.uploading_rpk, getSupport().getContext());
        device.sendDeviceUpdateIntent(getSupport().getContext());
    }

    private void unsetDeviceBusy() {
        final GBDevice device = getSupport().getDevice();
        if (device != null && device.isConnected()) {
            if (device.isBusy()) {
                device.unsetBusyTask();
                device.sendDeviceUpdateIntent(getSupport().getContext());
            }
            device.sendDeviceUpdateIntent(getSupport().getContext());
        }
    }

    public Collection<? extends GBDeviceApp> getInstalledAppsCache() {
        return Collections.unmodifiableList(apps);
    }
}
