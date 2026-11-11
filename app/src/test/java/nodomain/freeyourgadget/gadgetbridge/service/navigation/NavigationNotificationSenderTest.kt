package nodomain.freeyourgadget.gadgetbridge.service.navigation

import nodomain.freeyourgadget.gadgetbridge.model.NavigationInfoSpec
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec
import nodomain.freeyourgadget.gadgetbridge.service.AbstractDeviceSupport
import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import nodomain.freeyourgadget.gadgetbridge.util.preferences.DevicePrefs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class NavigationNotificationSenderTest : TestBase() {
    private lateinit var support: AbstractDeviceSupport
    private lateinit var sender: NavigationNotificationSender

    @Before
    fun setUpSender() {
        support = mock(AbstractDeviceSupport::class.java)
        `when`(support.context).thenReturn(context)
        `when`(support.devicePrefs).thenReturn(mock(DevicePrefs::class.java))
        sender = NavigationNotificationSender(support)
    }

    private fun googleMapsNav(
        distance: String,
        instruction: String = "Turn right onto Main St",
        action: Int = NavigationInfoSpec.ACTION_TURN_RIGHT,
    ): NavigationInfoSpec = NavigationInfoSpec().apply {
        source = NavigationInfoSpec.Source.GOOGLE_MAPS
        this.instruction = instruction
        this.nextAction = action
        distanceToTurn = distance
    }

    private fun sentNotifications(): List<NotificationSpec> {
        val captor = ArgumentCaptor.forClass(NotificationSpec::class.java)
        verify(support, atLeast(1)).onNotification(captor.capture())
        return captor.allValues
    }

    @Test
    fun sendsInitialAndNearAlertsOnly() {
        sender.onSetNavigationInfo(googleMapsNav("120 m"))
        assertEquals(1, sentNotifications().size)

        sender.onSetNavigationInfo(googleMapsNav("110 m"))
        assertEquals(1, sentNotifications().size)

        sender.onSetNavigationInfo(googleMapsNav("90 m"))
        assertEquals(2, sentNotifications().size)

        sender.onSetNavigationInfo(googleMapsNav("80 m"))
        assertEquals(2, sentNotifications().size)
    }

    @Test
    fun reusesSameNotificationEntry() {
        sender.onSetNavigationInfo(googleMapsNav("120 m"))
        sender.onSetNavigationInfo(googleMapsNav("90 m"))

        val ids = sentNotifications().map { it.id }.distinct()
        assertEquals(1, ids.size)
    }

    @Test
    fun alertsAgainWhenInstructionChanges() {
        sender.onSetNavigationInfo(googleMapsNav("120 m"))
        sender.onSetNavigationInfo(googleMapsNav("90 m"))
        assertEquals(2, sentNotifications().size)

        sender.onSetNavigationInfo(
            googleMapsNav(
                "90 m",
                instruction = "Turn left onto Second St",
                action = NavigationInfoSpec.ACTION_TURN_LEFT,
            )
        )
        assertEquals(3, sentNotifications().size)
    }

    @Test
    fun initialNotificationWithinThresholdAlertsOnlyOnce() {
        sender.onSetNavigationInfo(googleMapsNav("90 m"))
        assertEquals(1, sentNotifications().size)

        sender.onSetNavigationInfo(googleMapsNav("80 m"))
        assertEquals(1, sentNotifications().size)
    }
}
