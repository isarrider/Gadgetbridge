package nodomain.freeyourgadget.gadgetbridge.service.navigation

import android.os.SystemClock
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst
import nodomain.freeyourgadget.gadgetbridge.model.NavigationInfoSpec
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec
import nodomain.freeyourgadget.gadgetbridge.model.NotificationType
import nodomain.freeyourgadget.gadgetbridge.service.AbstractDeviceSupport
import nodomain.freeyourgadget.gadgetbridge.util.NavigationUtils
import nodomain.freeyourgadget.gadgetbridge.util.Prefs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Sends navigation instructions as a notification. The notification ID is reused, updating the existing notification.
 */
class NavigationNotificationSender(private val support: AbstractDeviceSupport) {
    private var lastSent: NavigationInfoSpec? = null
    private var lastSentAt = 0L
    private var posted = false
    private var googleMapsNearAlertSent = false

    fun onSetNavigationInfo(nav: NavigationInfoSpec) {
        if (nav.instruction.isNullOrBlank() && nav.distanceToTurn.isNullOrBlank() && nav.ETA.isNullOrBlank()) {
            clear()
            return
        }

        val destinationReached = isDestinationReached(nav)
        val googleMapsDecision = if (!destinationReached && nav.source == NavigationInfoSpec.Source.GOOGLE_MAPS) {
            decideGoogleMapsSend(nav)
        } else {
            null
        }

        if (destinationReached) {
            if (lastSent?.let { isDestinationReached(it) } == true) {
                return
            }
        } else if (googleMapsDecision != null) {
            if (googleMapsDecision == GoogleMapsDecision.NONE) {
                return
            }
        } else if (!shouldSend(nav, support.devicePrefs)) {
            return
        }

        val context = support.context
        val spec = NotificationSpec(NOTIFICATION_ID)
        spec.type = NotificationType.GENERIC_NAVIGATION
        spec.sourceAppId = NavigationUtils.getIconPackageName(nav.nextAction)
        spec.sourceName = context.getString(R.string.pref_header_navigation)
        if (destinationReached) {
            spec.title = context.getString(R.string.navigation_destination_reached)
        } else {
            val title = nav.distanceToTurn?.takeIf { it.isNotBlank() } ?: spec.sourceName
            val symbol = if (support.devicePrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_NAVIGATION_SYMBOLS, false)) {
                NavigationUtils.getActionSymbol(nav.nextAction)
            } else {
                ""
            }
            spec.title = if (symbol.isEmpty()) title else "$symbol $title"
            spec.body = listOfNotNull(
                nav.instruction?.takeIf { it.isNotBlank() },
                nav.ETA?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.navigation_eta, it) },
                nav.distanceToTarget?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.navigation_distance_left, it) },
            ).joinToString("\n")
        }

        LOG.debug("Sending navigation notification for {}", nav)
        support.onDeleteNotification(NOTIFICATION_ID)
        support.onNotification(spec)
        posted = true
        lastSent = nav
        lastSentAt = SystemClock.elapsedRealtime()
        if (googleMapsDecision != null) {
            googleMapsNearAlertSent = when (googleMapsDecision) {
                GoogleMapsDecision.INITIAL -> nav.distanceToTurnMeters?.let { it <= GOOGLE_MAPS_NEAR_ALERT_METERS } == true
                GoogleMapsDecision.NEAR_ALERT -> true
                GoogleMapsDecision.NONE -> googleMapsNearAlertSent
            }
        }
    }

    private fun clear() {
        lastSent = null
        googleMapsNearAlertSent = false
        if (!posted) {
            return
        }
        LOG.debug("Clearing navigation notification")
        support.onDeleteNotification(NOTIFICATION_ID)
        posted = false
    }

    private fun decideGoogleMapsSend(nav: NavigationInfoSpec): GoogleMapsDecision {
        val last = lastSent ?: return GoogleMapsDecision.INITIAL
        if (last.source != NavigationInfoSpec.Source.GOOGLE_MAPS) {
            return GoogleMapsDecision.INITIAL
        }
        if (nav.nextAction != last.nextAction || nav.instruction != last.instruction) {
            return GoogleMapsDecision.INITIAL
        }
        if (googleMapsNearAlertSent) {
            return GoogleMapsDecision.NONE
        }
        val distance = nav.distanceToTurnMeters ?: return GoogleMapsDecision.NONE
        return if (distance <= GOOGLE_MAPS_NEAR_ALERT_METERS) {
            GoogleMapsDecision.NEAR_ALERT
        } else {
            GoogleMapsDecision.NONE
        }
    }

    private fun shouldSend(nav: NavigationInfoSpec, devicePrefs: Prefs): Boolean {
        val last = lastSent ?: return true

        if (devicePrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_NAVIGATION_CONTINUOUS_UPDATES, false)) {
            val updateRate = devicePrefs.getInt(DeviceSettingsPreferenceConst.PREF_NAVIGATION_UPDATE_RATE, 5000)
            return SystemClock.elapsedRealtime() - lastSentAt >= updateRate
        }

        if (nav.nextAction != last.nextAction || nav.instruction != last.instruction) {
            return true
        }

        val remainder = distanceAfterTurn(nav) ?: return false
        val lastRemainder = distanceAfterTurn(last) ?: return false
        return lastRemainder - remainder > NEW_SEGMENT_DISTANCE_METERS
    }

    private fun distanceAfterTurn(nav: NavigationInfoSpec): Int? {
        val toTarget = nav.distanceToTargetMeters ?: return null
        val toTurn = nav.distanceToTurnMeters ?: return null
        return toTarget - toTurn
    }

    private fun isDestinationReached(nav: NavigationInfoSpec): Boolean {
        val toTarget = nav.distanceToTargetMeters ?: return false
        // CoMaps ends navigation abruptly when distance to destination is ~15m, sends no propper "destination reached" notification
        return nav.nextAction == NavigationInfoSpec.ACTION_FINISH && toTarget <= FINISH_DISTANCE_METERS
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(NavigationNotificationSender::class.java)

        private const val NOTIFICATION_ID = 0x4E415649
        private const val FINISH_DISTANCE_METERS = 15
        private const val NEW_SEGMENT_DISTANCE_METERS = 5
        private const val GOOGLE_MAPS_NEAR_ALERT_METERS = 100
    }

    private enum class GoogleMapsDecision {
        INITIAL,
        NEAR_ALERT,
        NONE,
    }
}
