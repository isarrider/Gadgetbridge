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
package nodomain.freeyourgadget.gadgetbridge.util.healthconnect

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Health Connect silently drops any permission the app requests but does not declare in its
 * manifest - the consent screen simply does not show that data type, and the syncer then skips it
 * because the permission was never granted. That failure is invisible from the code side, so this
 * test keeps the manifest and [HealthConnectPermissionManager.getRequiredHealthConnectPermissions]
 * in sync.
 */
class HealthConnectManifestPermissionsTest {

    private val manifest: String by lazy {
        // Gradle runs unit tests with the module directory as the working directory.
        File("src/main/AndroidManifest.xml").readText()
    }

    @Test
    fun allRequestedHealthConnectPermissionsAreDeclaredInManifest() {
        val declared = Regex("android\\.permission\\.health\\.[A-Z0-9_]+")
            .findAll(manifest)
            .map { it.value }
            .toSet()

        val requested = HealthConnectPermissionManager.getRequiredHealthConnectPermissions()

        val missing = requested - declared
        assertTrue(
            "These Health Connect permissions are requested at runtime but not declared in " +
                    "AndroidManifest.xml, so Health Connect will never grant them: $missing",
            missing.isEmpty()
        )
    }

    @Test
    fun bodyCompositionPermissionsAreRequested() {
        val requested = HealthConnectPermissionManager.getRequiredHealthConnectPermissions()

        // A scale reports weight and impedance only; the composition values are derived by
        // Gadgetbridge, so these types have to be requested explicitly for them to be exported.
        val expected = setOf(
            "android.permission.health.WRITE_BODY_FAT",
            "android.permission.health.WRITE_BODY_WATER_MASS",
            "android.permission.health.WRITE_BONE_MASS",
            "android.permission.health.WRITE_LEAN_BODY_MASS"
        )

        val missing = expected - requested
        assertTrue("Body composition permissions are not requested at runtime: $missing", missing.isEmpty())
    }
}
