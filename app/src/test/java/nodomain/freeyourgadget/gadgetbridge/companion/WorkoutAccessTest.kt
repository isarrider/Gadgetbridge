package nodomain.freeyourgadget.gadgetbridge.companion

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.Signature
import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import org.junit.Assert.*
import org.junit.Test
import org.robolectric.Shadows

@Suppress("DEPRECATION")
class WorkoutAccessTest : TestBase() {
    private val pkg = "org.example.training"
    private val watch = "AA:BB:CC:DD:EE:FF"
    private fun install(signature: String = "1234", uid: Int = 12001) {
        Shadows.shadowOf(context.packageManager).installPackage(PackageInfo().apply {
            packageName = pkg
            applicationInfo = ApplicationInfo().apply { packageName = pkg; this.uid = uid }
            signatures = arrayOf(Signature(signature))
        })
    }
    @Test fun anUnapprovedCallerCannotChooseItsIdentityOrAnotherDevice() {
        install()
        val access = WorkoutAccess(context)
        assertThrows(SecurityException::class.java) { access.owner(12001, watch) }
        access.approve(pkg, watch)
        assertTrue(access.owner(12001, watch).startsWith("$pkg|"))
        assertThrows(SecurityException::class.java) { access.owner(12002, watch) }
        assertThrows(SecurityException::class.java) { access.owner(12001, "11:22:33:44:55:66") }
    }
    @Test fun revocationAndSignerChangesInvalidateApprovalAndQueuedOwners() {
        install()
        val access = WorkoutAccess(context)
        access.approve(pkg, watch)
        val owner = access.owner(12001, watch)
        assertTrue(access.ownerAuthorized(owner, watch))
        install("5678")
        assertFalse(access.authorized(pkg, watch))
        assertFalse(access.ownerAuthorized(owner, watch))
        access.approve(pkg, watch)
        access.revoke(pkg, watch)
        assertFalse(access.authorized(pkg, watch))
    }
}
