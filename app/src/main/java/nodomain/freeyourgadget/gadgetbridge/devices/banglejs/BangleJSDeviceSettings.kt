package nodomain.freeyourgadget.gadgetbridge.devices.banglejs

import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.widget.FrameLayout
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsScreen
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.DeviceSettingsScope
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.DeviceSettingsSpec
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.components.highMtu
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.components.sendAppNotifications
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.components.transliteration
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.deviceSettings
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.screens.syncCalendar
import nodomain.freeyourgadget.gadgetbridge.activities.loyaltycards.LoyaltyCardsSettingsActivity
import nodomain.freeyourgadget.gadgetbridge.activities.loyaltycards.LoyaltyCardsSettingsConst
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger("BangleJSDeviceSettings")

fun bangleJSDeviceSettings(device: GBDevice): DeviceSettingsSpec = deviceSettings {
    DeviceSpecificSettingsScreen.CALLS_AND_NOTIFICATIONS.screen(
        xmlSubScreens = listOf(R.xml.devicesettings_canned_reply_16),
    ) {
        sendAppNotifications()
        switchSetting(
            key = DeviceSettingsPreferenceConst.PREF_NOTIFICATION_WAKE_ON_OPEN,
            title = R.string.pref_title_notification_wake_on_open,
            summary = R.string.pref_summary_notification_wake_on_open,
            icon = R.drawable.ic_notifications,
            connectedOnly = false,
        )
        switchSetting(
            key = DeviceSettingsPreferenceConst.PREF_BANGLEJS_TEXT_BITMAP,
            title = R.string.pref_title_banglejs_text_bitmap,
            summary = R.string.pref_summary_banglejs_text_bitmap,
            icon = R.drawable.ic_translate,
            connectedOnly = false,
        )
        text(
            key = DeviceSettingsPreferenceConst.PREF_BANGLEJS_TEXT_BITMAP_SIZE,
            title = R.string.pref_title_banglejs_txt_bitmap_size,
            summary = R.string.pref_summary_banglejs_txt_bitmap_size,
            icon = R.drawable.ic_font_size,
            defaultValue = "18",
            inputType = InputType.TYPE_CLASS_NUMBER,
            dependency = DeviceSettingsPreferenceConst.PREF_BANGLEJS_TEXT_BITMAP,
            connectedOnly = false,
        )
        transliteration()
        switchSetting(
            key = BangleJSConstants.PREF_BANGLEJS_NOTIFICATION_MISSED_CALL_ENABLE,
            title = R.string.pref_notifications_and_calls_enable_misscall,
            icon = R.drawable.ic_phone,
            connectedOnly = false,
        )
    }

    DeviceSpecificSettingsScreen.CALENDAR.screen {
        syncCalendar()
    }

    DeviceSpecificSettingsScreen.LOCATION.screen {
        switchSetting(
            key = DeviceSettingsPreferenceConst.PREF_DEVICE_GPS_UPDATE,
            title = R.string.pref_title_banglejs_phone_gps_enbale,
            summary = R.string.pref_summary_banglejs_phone_gps_enbale,
            icon = R.drawable.ic_gps_location,
            connectedOnly = false,
        )
        text(
            key = DeviceSettingsPreferenceConst.PREF_DEVICE_GPS_UPDATE_INTERVAL,
            title = R.string.pref_title_banglejs_phone_gps_update_interval,
            summary = R.string.pref_summary_banglejs_phone_gps_update_interval,
            icon = R.drawable.ic_access_time,
            defaultValue = "1000",
            inputType = InputType.TYPE_CLASS_NUMBER,
            dependency = DeviceSettingsPreferenceConst.PREF_DEVICE_GPS_UPDATE,
            connectedOnly = false,
        )
        switchSetting(
            key = DeviceSettingsPreferenceConst.PREF_DEVICE_GPS_USE_NETWORK_ONLY,
            title = R.string.pref_title_banglejs_phone_gps_network_only,
            summary = R.string.pref_summary_banglejs_phone_gps_network_only,
            icon = R.drawable.ic_gps_location,
            dependency = DeviceSettingsPreferenceConst.PREF_DEVICE_GPS_UPDATE,
            connectedOnly = false,
        )
    }

    DeviceSpecificSettingsScreen.WATCH_APPS.screen(
        xmlSubScreens = listOf(R.xml.devicesettings_banglejs_apploader),
    ) {
        externalSettings(
            key = LoyaltyCardsSettingsConst.PREF_KEY_LOYALTY_CARDS,
            title = R.string.loyalty_cards,
            icon = R.drawable.ic_loyalty,
            activityClass = LoyaltyCardsSettingsActivity::class.java,
        )
        if (GBApplication.hasInternetAccess()) {
            switchSetting(
                key = DeviceSettingsPreferenceConst.PREF_DEVICE_INTERNET_ACCESS,
                title = R.string.pref_title_device_internet_access,
                summary = R.string.pref_summary_device_internet_access,
                icon = R.drawable.ic_language,
                connectedOnly = false,
            )
        }
        switchSetting(
            key = DeviceSettingsPreferenceConst.PREF_DEVICE_INTENTS,
            title = R.string.pref_title_device_intents,
            summary = R.string.pref_summary_device_intents,
            icon = R.drawable.ic_language,
            connectedOnly = false,
        )
    }

    DeviceSpecificSettingsScreen.CONNECTION.screen {
        highMtu()
    }

    DeviceSpecificSettingsScreen.ACTIVITY_INFO.screen {
        activityFullSync(device)
    }
}

private fun DeviceSettingsScope.activityFullSync(device: GBDevice) {
    action(
        key = BangleJSConstants.PREF_BANGLEJS_ACTIVITY_FULL_SYNC_TRIGGER,
        title = R.string.pref_activity_full_sync_trigger_title,
        summary = R.string.pref_activity_full_sync_trigger_summary,
        icon = R.drawable.ic_refresh,
    ) { context, _ ->
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.pref_activity_full_sync_trigger_title)
            .setMessage(R.string.pref_activity_full_sync_trigger_warning)
            .setIcon(R.drawable.ic_refresh)
            .setPositiveButton(R.string.start) { _, _ -> startActivityFullSync(context, device) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
        true
    }
}

private fun startActivityFullSync(context: Context, device: GBDevice) {
    val statusKey = BangleJSConstants.PREF_BANGLEJS_ACTIVITY_FULL_SYNC_STATUS
    val prefs = GBApplication.getDeviceSpecificSharedPrefs(device.address)
    // The listener fires only when the value changes, so remove the status of an earlier sync.
    prefs.edit { remove(statusKey) }

    val padding = context.resources.getDimensionPixelSize(R.dimen.dialog_margin)
    val progressContainer = FrameLayout(context).apply {
        setPadding(padding, padding, padding, 0)
        addView(LinearProgressIndicator(context).apply { isIndeterminate = true })
    }

    val dialog = MaterialAlertDialogBuilder(context)
        .setCancelable(false)
        .setMessage(R.string.sony_anc_optimizer_status_starting)
        .setView(progressContainer)
        .setNegativeButton(R.string.cancel) { _, _ ->
            GBApplication.deviceService(device)
                .onSendConfiguration(BangleJSConstants.PREF_BANGLEJS_ACTIVITY_FULL_SYNC_STOP)
        }
        .create()

    val statusListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
        if (key != statusKey) return@OnSharedPreferenceChangeListener
        val status = sharedPrefs.getString(statusKey, null) ?: return@OnSharedPreferenceChangeListener
        LOG.debug("Activity full sync status: {}", status)
        when (status) {
            "start" -> dialog.setMessage(context.getString(R.string.busy_task_fetch_activity_data))
            "end" -> dialog.dismiss()
            else -> dialog.setMessage(status)
        }
    }
    prefs.registerOnSharedPreferenceChangeListener(statusListener)
    // SharedPreferences keeps only a weak reference to the listener. This dismiss listener keeps it alive.
    dialog.setOnDismissListener { prefs.unregisterOnSharedPreferenceChangeListener(statusListener) }
    (context as? LifecycleOwner)?.lifecycle?.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) dialog.dismiss()
    })
    dialog.show()

    GBApplication.deviceService(device)
        .onSendConfiguration(BangleJSConstants.PREF_BANGLEJS_ACTIVITY_FULL_SYNC_START)
}
