package nodomain.freeyourgadget.gadgetbridge.impl

import nodomain.freeyourgadget.gadgetbridge.model.DeviceService
import nodomain.freeyourgadget.gadgetbridge.model.NavigationInfoSpec
import nodomain.freeyourgadget.gadgetbridge.service.DeviceActionHandler
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport
import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper

class GBDeviceServiceTest : TestBase() {
    @Test
    fun isBuiltFromTheGivenContext() {
        val service = GBDeviceService(app)
        assertNull(service.device)

        val device = createDummyGDevice("AA:BB:CC:DD:EE:FF")
        assertSame(device, service.forDevice(device).device)
    }

    @Test
    fun navigationSourceSurvivesServiceRoundTrip() {
        val service = GBDeviceService(app)
        service.onSetNavigationInfo(
            NavigationInfoSpec().apply {
                source = NavigationInfoSpec.Source.GOOGLE_MAPS
                instruction = "Turn right"
                nextAction = NavigationInfoSpec.ACTION_TURN_RIGHT
                distanceToTurn = "90 m"
            }
        )
        ShadowLooper.shadowMainLooper().idle()

        val intent = shadowOf(app).getNextStartedService()
        assertNotNull(intent)

        val device = createDummyGDevice("AA:BB:CC:DD:EE:FF")
        val support = mock(DeviceSupport::class.java)
        DeviceActionHandler.handle(device, support, app, intent, DeviceService.ACTION_SETNAVIGATIONINFO)

        val captor = ArgumentCaptor.forClass(NavigationInfoSpec::class.java)
        verify(support).onSetNavigationInfo(captor.capture())
        assertEquals(NavigationInfoSpec.Source.GOOGLE_MAPS, captor.value.source)
    }
}
