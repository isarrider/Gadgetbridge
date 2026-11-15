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
package nodomain.freeyourgadget.gadgetbridge.activities.casio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.casio.gbd200.CasioMeasurementScreenField;
import nodomain.freeyourgadget.gadgetbridge.service.devices.casio.gbd200.CasioMeasurementScreens;

/** Editor for the three measurement screens of the GBD-200. The watch is the only source of
 *  truth: the layout is read when the screen opens and written as a whole on Save. */
public class CasioMeasurementScreensActivity extends AbstractGBActivity {
    private static final Logger LOG = LoggerFactory.getLogger(CasioMeasurementScreensActivity.class);

    private static final String STATE_DRAFT = "draft";
    private static final String STATE_WATCH = "watch";

    // other requests may be queued ahead of ours, and the watch takes about a second for each
    private static final long READ_TIMEOUT_MS = 20000;
    private static final long WRITE_TIMEOUT_MS = 20000;
    // how long to wait for a busy device (e.g. fetching activity data) before giving up
    private static final long BUSY_TIMEOUT_MS = 30000;
    // the watch refuses reads for a few seconds after connecting (up to POST_INIT_TIMEOUT_MS in
    // CasioGBD200DeviceSupport)
    private static final long READ_RETRY_DELAY_MS = 2000;
    private static final int READ_RETRIES = 4;

    private static final int MENU_REFRESH = Menu.FIRST;

    private static final int[] AREA_LABELS = {
            R.string.casio_measurement_screens_area_top,
            R.string.casio_measurement_screens_area_middle,
            R.string.casio_measurement_screens_area_bottom,
    };

    private enum State {DISCONNECTED, LOADING, READY, SAVING, ERROR}

    private GBDevice gbDevice;
    // what the watch last reported, null until the first successful read
    @Nullable
    private CasioMeasurementScreens watchState;
    // what the user is editing, null until there is something to edit
    @Nullable
    private CasioMeasurementScreens draft;
    private State state = State.DISCONNECTED;
    @StringRes
    private int message = 0;
    private int readRetriesLeft = 0;
    private long busyDeadline = 0;

    private TextView statusView;
    private View saveButton;
    private final View[] areaRows = new View[CasioMeasurementScreens.SLOT_COUNT];
    private final TextView[] areaFields = new TextView[CasioMeasurementScreens.SLOT_COUNT];

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable = this::onTimeout;
    private final Runnable readRunnable = this::requestRead;

    private final OnBackPressedCallback discardCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            confirmDiscard(CasioMeasurementScreensActivity.this::finish);
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final GBDevice device = intent.getParcelableExtra(GBDevice.EXTRA_DEVICE);
            if (device == null || !gbDevice.getAddress().equals(device.getAddress())) {
                return;
            }
            if (GBDevice.ACTION_DEVICE_CHANGED.equals(intent.getAction())) {
                gbDevice = device;
                onDeviceChanged();
            } else if (CasioMeasurementScreens.ACTION_MEASUREMENT_SCREENS.equals(intent.getAction())) {
                onResult(intent);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_casio_measurement_screens);

        gbDevice = getIntent().getParcelableExtra(GBDevice.EXTRA_DEVICE);
        if (gbDevice == null) {
            finish();
            return;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState != null) {
            draft = CasioMeasurementScreens.fromHex(savedInstanceState.getString(STATE_DRAFT));
            watchState = CasioMeasurementScreens.fromHex(savedInstanceState.getString(STATE_WATCH));
        }

        statusView = findViewById(R.id.casio_measurement_screens_status);
        saveButton = findViewById(R.id.casio_measurement_screens_save);
        saveButton.setOnClickListener(v -> save());

        final LayoutInflater inflater = LayoutInflater.from(this);
        final ViewGroup list = findViewById(R.id.casio_measurement_screens_list);
        for (int screen = 0; screen < CasioMeasurementScreens.SCREEN_COUNT; screen++) {
            final View card = inflater.inflate(R.layout.item_casio_measurement_screen, list, false);
            final TextView title = card.findViewById(R.id.casio_measurement_screen_title);
            title.setText(getString(R.string.casio_measurement_screens_screen, screen + 1));
            ViewCompat.setAccessibilityHeading(title, true);
            final ViewGroup container = card.findViewById(R.id.casio_measurement_screen_areas);
            for (int area = 0; area < CasioMeasurementScreens.AREAS_PER_SCREEN; area++) {
                final int slot = screen * CasioMeasurementScreens.AREAS_PER_SCREEN + area;
                final View row = inflater.inflate(R.layout.item_casio_measurement_screen_area, container, false);
                ((TextView) row.findViewById(R.id.casio_measurement_area_label)).setText(AREA_LABELS[area]);
                row.setOnClickListener(v -> pickField(slot));
                areaRows[slot] = row;
                areaFields[slot] = row.findViewById(R.id.casio_measurement_area_field);
                container.addView(row);
            }
            list.addView(card);
        }

        getOnBackPressedDispatcher().addCallback(this, discardCallback);
        updateUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(GBDevice.ACTION_DEVICE_CHANGED);
        filter.addAction(CasioMeasurementScreens.ACTION_MEASUREMENT_SCREENS);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        // the device from the intent is a snapshot and may be outdated by now
        final GBDevice current = GBApplication.app().getDeviceManager().getDeviceByAddress(gbDevice.getAddress());
        if (current != null) {
            gbDevice = current;
        }
        if (isLinked()) {
            startRead();
        } else {
            setState(State.DISCONNECTED, 0);
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        cancelPending();
        if (state == State.SAVING) {
            removeDraftPref();
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (draft != null) {
            outState.putString(STATE_DRAFT, draft.toHex());
        }
        if (watchState != null) {
            outState.putString(STATE_WATCH, watchState.toHex());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, R.string.refresh)
                .setIcon(R.drawable.ic_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isDirty()) {
                confirmDiscard(this::finish);
            } else {
                finish();
            }
            return true;
        }
        if (item.getItemId() == MENU_REFRESH) {
            refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Talking to the watch ──────────────────────────────────────────────────

    /** GBDevice.isInitialized() also holds for a scanned device, which has no link yet. */
    private boolean isLinked() {
        return gbDevice.getState().equalsOrHigherThan(GBDevice.State.INITIALIZED);
    }

    private void refresh() {
        if (!isLinked()) {
            Toast.makeText(this, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (state == State.LOADING || state == State.SAVING) {
            return;
        }
        if (state == State.READY && isDirty()) {
            confirmDiscard(() -> {
                // things may have changed while the dialog was open
                if (!isLinked() || state != State.READY) {
                    return;
                }
                draft = null;
                startRead();
            });
            return;
        }
        startRead();
    }

    private void startRead() {
        readRetriesLeft = READ_RETRIES;
        busyDeadline = SystemClock.elapsedRealtime() + BUSY_TIMEOUT_MS;
        setState(State.LOADING, 0);
        requestRead();
    }

    private void requestRead() {
        cancelPending();
        if (gbDevice.isBusy()) {
            // the service drops requests without an answer while the device is busy
            if (SystemClock.elapsedRealtime() < busyDeadline) {
                handler.postDelayed(readRunnable, READ_RETRY_DELAY_MS);
            } else {
                LOG.warn("Device still busy with {}, giving up", gbDevice.getBusyTask());
                setState(State.ERROR, R.string.casio_measurement_screens_timeout);
            }
            return;
        }
        GBApplication.deviceService(gbDevice)
                .onReadConfiguration(CasioMeasurementScreens.CONFIG_MEASUREMENT_SCREENS);
        handler.postDelayed(timeoutRunnable, READ_TIMEOUT_MS);
    }

    private void save() {
        if (state != State.READY || draft == null) {
            return;
        }
        if (!isLinked()) {
            Toast.makeText(this, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (gbDevice.isBusy()) {
            // the service would drop the request without an answer
            Toast.makeText(this, gbDevice.getBusyTask(), Toast.LENGTH_SHORT).show();
            return;
        }
        cancelPending();
        // the layout is handed over in a device pref, which the service removes when it reads it
        GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()).edit()
                .putString(CasioMeasurementScreens.PREF_MEASUREMENT_SCREENS_DRAFT, draft.toHex())
                .apply();
        GBApplication.deviceService(gbDevice)
                .onSendConfiguration(CasioMeasurementScreens.CONFIG_MEASUREMENT_SCREENS);
        handler.postDelayed(timeoutRunnable, WRITE_TIMEOUT_MS);
        setState(State.SAVING, 0);
    }

    /** The service removes it when it picks it up, this is for a request it never got. */
    private void removeDraftPref() {
        GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()).edit()
                .remove(CasioMeasurementScreens.PREF_MEASUREMENT_SCREENS_DRAFT)
                .apply();
    }

    private void cancelPending() {
        handler.removeCallbacks(timeoutRunnable);
        handler.removeCallbacks(readRunnable);
    }

    private void onTimeout() {
        if (state == State.LOADING || state == State.SAVING) {
            LOG.warn("No measurement screens result while {}", state);
            if (state == State.SAVING) {
                removeDraftPref();
            }
            setState(State.ERROR, R.string.casio_measurement_screens_timeout);
        }
    }

    private void onDeviceChanged() {
        if (!isLinked()) {
            cancelPending();
            setState(State.DISCONNECTED, 0);
        } else if (state == State.DISCONNECTED) {
            startRead();
        }
    }

    private void onResult(final Intent intent) {
        CasioMeasurementScreens.Result result;
        try {
            result = CasioMeasurementScreens.Result.valueOf(
                    String.valueOf(intent.getStringExtra(CasioMeasurementScreens.EXTRA_MEASUREMENT_SCREENS_RESULT)));
        } catch (final IllegalArgumentException e) {
            result = CasioMeasurementScreens.Result.ERROR;
        }
        final byte[] ids = intent.getByteArrayExtra(CasioMeasurementScreens.EXTRA_MEASUREMENT_SCREENS_IDS);
        final CasioMeasurementScreens screens =
                ids != null && ids.length == CasioMeasurementScreens.SLOT_COUNT
                        ? new CasioMeasurementScreens(ids) : null;
        if (screens == null) {
            result = CasioMeasurementScreens.Result.ERROR;
        }

        switch (result) {
            case READ_OK:
                if (state == State.SAVING) {
                    // a late answer to an earlier read, the result of the write is still to come
                    return;
                }
                cancelPending();
                // only a draft with edits survives a read, anything else follows the watch
                final boolean dirty = isDirty();
                watchState = screens;
                draft = dirty ? draft.revalidatedAgainst(screens) : screens;
                setState(State.READY, 0);
                break;
            case WRITE_OK:
                cancelPending();
                watchState = screens;
                draft = screens;
                setState(State.READY, R.string.casio_measurement_screens_saved);
                break;
            case WRITE_MISMATCH:
                cancelPending();
                watchState = screens;
                draft = screens;
                setState(State.READY, R.string.casio_measurement_screens_not_accepted);
                break;
            case ERROR:
            default:
                if (state == State.LOADING && readRetriesLeft > 0) {
                    readRetriesLeft--;
                    cancelPending();
                    handler.postDelayed(readRunnable, READ_RETRY_DELAY_MS);
                } else if (state == State.LOADING || state == State.SAVING) {
                    cancelPending();
                    setState(State.ERROR, R.string.casio_measurement_screens_error);
                }
                break;
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private boolean isDirty() {
        return draft != null && watchState != null && !draft.equals(watchState);
    }

    private void setState(final State newState, @StringRes final int newMessage) {
        state = newState;
        message = newMessage;
        updateUi();
    }

    private void updateUi() {
        final CasioMeasurementScreens shown = draft != null ? draft : watchState;
        final boolean editable = state == State.READY && draft != null;
        for (int slot = 0; slot < CasioMeasurementScreens.SLOT_COUNT; slot++) {
            areaFields[slot].setText(shown == null ? "" : describe(shown.getId(slot)));
            areaRows[slot].setEnabled(editable);
            areaRows[slot].setAlpha(editable ? 1f : 0.5f);
        }
        saveButton.setEnabled(editable && isDirty() && draft.isWritable(watchState));
        discardCallback.setEnabled(isDirty());

        @StringRes final int status;
        switch (state) {
            case DISCONNECTED:
                status = R.string.device_not_connected;
                break;
            case LOADING:
                status = R.string.casio_measurement_screens_loading;
                break;
            case SAVING:
                status = R.string.casio_measurement_screens_saving;
                break;
            default:
                status = message;
                break;
        }
        if (status == 0) {
            statusView.setVisibility(View.GONE);
        } else {
            statusView.setText(status);
            statusView.setVisibility(View.VISIBLE);
        }
    }

    private void pickField(final int slot) {
        if (state != State.READY || draft == null) {
            return;
        }
        final CasioMeasurementScreenField[] fields = CasioMeasurementScreenField.values();
        final CharSequence[] labels = new CharSequence[fields.length];
        int checked = -1;
        for (int i = 0; i < fields.length; i++) {
            labels[i] = describe(fields[i].wireId);
            if (fields[i] == draft.getField(slot)) {
                checked = i;
            }
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.casio_measurement_screens_area_title,
                        slot / CasioMeasurementScreens.AREAS_PER_SCREEN + 1,
                        getString(AREA_LABELS[slot % CasioMeasurementScreens.AREAS_PER_SCREEN])))
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (state != State.READY || draft == null) {
                        return;
                    }
                    draft = draft.withField(slot, fields[which]);
                    setState(State.READY, 0);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDiscard(final Runnable onDiscard) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.casio_measurement_screens_discard)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> onDiscard.run())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private String describe(final byte id) {
        final CasioMeasurementScreenField field = CasioMeasurementScreenField.fromWireId(id);
        return field == null
                ? getString(R.string.casio_measurement_field_unknown, id & 0xFF)
                : getString(fieldLabel(field));
    }

    @StringRes
    private static int fieldLabel(final CasioMeasurementScreenField field) {
        switch (field) {
            case SPLIT_TIME: return R.string.casio_measurement_field_split_time;
            case DISTANCE: return R.string.casio_measurement_field_distance;
            case PACE: return R.string.casio_measurement_field_pace;
            case LAP_TIME: return R.string.casio_measurement_field_lap_time;
            case LAP_DISTANCE: return R.string.casio_measurement_field_lap_distance;
            case LAP_PACE: return R.string.casio_measurement_field_lap_pace;
            case AVG_PACE: return R.string.casio_measurement_field_avg_pace;
            case SPEED: return R.string.casio_measurement_field_speed;
            case AVG_SPEED: return R.string.casio_measurement_field_avg_speed;
            case CALORIES: return R.string.casio_measurement_field_calories;
            case CURRENT_TIME: return R.string.casio_measurement_field_current_time;
            case DATE: return R.string.casio_measurement_field_date;
            default: throw new IllegalArgumentException("No label for " + field);
        }
    }
}
