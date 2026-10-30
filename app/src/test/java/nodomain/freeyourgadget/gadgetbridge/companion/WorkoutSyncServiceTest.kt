package nodomain.freeyourgadget.gadgetbridge.companion

import android.content.Intent
import android.os.Process
import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import org.junit.Assert.*
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowBinder

class WorkoutSyncServiceTest : TestBase() {
    @Test fun bindingDoesNotGrantAccessToJobOperations() {
        val controller = Robolectric.buildService(WorkoutSyncService::class.java).create()
        val api = IWorkoutSyncService.Stub.asInterface(controller.get().onBind(Intent()))
        ShadowBinder.setCallingUid(23456)
        try {
            assertEquals(1, api.apiVersion)
            assertTrue(api.devices.isEmpty())
            assertThrows(SecurityException::class.java) { api.getJob("request", "AA:BB:CC:DD:EE:FF") }
            assertThrows(SecurityException::class.java) { api.cancelJob("request", "AA:BB:CC:DD:EE:FF") }
            assertThrows(SecurityException::class.java) { api.forgetJob("request", "AA:BB:CC:DD:EE:FF") }
            assertThrows(SecurityException::class.java) { api.submitWorkout("request", "AA:BB:CC:DD:EE:FF", android.net.Uri.parse("content://example/workout")) }
        } finally {
            ShadowBinder.setCallingUid(Process.myUid())
            controller.destroy()
        }
    }
}
