package nodomain.freeyourgadget.gadgetbridge.devices.garmin

import android.net.Uri
import androidx.core.content.edit
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.venu.GarminVenu3Coordinator
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.install.*
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind
import nodomain.freeyourgadget.gadgetbridge.model.workouts.*
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.workouts.GarminWorkoutFitEncoder
import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.*

class GarminWorkoutInstallHandlerTest : TestBase() {
    @get:Rule val folder = TemporaryFolder()
    private val coordinator = GarminVenu3Coordinator()
    private fun workout(name: String): ByteArray = GarminWorkoutFitEncoder.encode(WorkoutTemplate(
        vendorId = "garmin", name = name, activityKind = ActivityKind.RUNNING,
        steps = mutableListOf(WorkoutStepNode(type = WorkoutNodeType.STEP, stepType = WorkoutStepType.ACTIVE,
            duration = WorkoutDuration(WorkoutDurationType.TIME, 60_000), target = WorkoutTarget.NONE))), 0L)!!.outgoingMessage
    private fun file(name: String, bytes: ByteArray) = Uri.fromFile(folder.newFile(name).apply { writeBytes(bytes) })
    private fun handler() = GarminWorkoutInstallHandler(context, coordinator)
    private fun device(): GBDevice = mock(GBDevice::class.java).also { `when`(it.address).thenReturn("AA:BB:CC:DD:EE:FF") }

    @Test fun batchPreservesWorkoutNamesAndExactEncodedBytes() {
        val first = workout("Intervals")
        val second = workout("Long run")
        val files = listOf(file("one.fit", first), file("two.fit", second))
        assertTrue(coordinator.findInstallHandlerV2(files, context) is WorkoutInstallHandler)
        val result = handler().prepare(files) as ParsedFiles.Supported
        assertEquals(listOf("Intervals", "Long run"), result.items.map { it.displayName })
        assertArrayEquals(first, (result.items[0] as GarminWorkoutInstallItem).bytes)
        assertArrayEquals(second, (result.items[1] as GarminWorkoutInstallItem).bytes)
    }
    @Test fun mixedCorruptAndEmptyBatchesAreRejectedBeforeTransfer() {
        val valid = file("valid.fit", workout("Easy"))
        val bad = file("bad.fit", "not fit".toByteArray())
        assertTrue(handler().prepare(listOf(valid, bad)) is ParsedFiles.Unsupported)
        val corrupt = workout("Corrupt").also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        assertTrue(handler().prepare(listOf(file("corrupt.fit", corrupt))) is ParsedFiles.Unsupported)
        assertTrue(handler().prepare(emptyList()) is ParsedFiles.Unsupported)
    }
    @Test fun compatibilityUsesTheDevicesWorkoutCapabilityAndReportsOverrides() {
        val parsed = handler().prepare(listOf(file("easy.fit", workout("Easy")))) as ParsedFiles.Supported
        assertTrue(handler().checkCompatibility(parsed, device()) is Compatibility.Incompatible)
        GBApplication.getDeviceSpecificSharedPrefs("AA:BB:CC:DD:EE:FF").edit { putBoolean("install_unsupported_files", true) }
        assertTrue((handler().checkCompatibility(parsed, device()) as Compatibility.Compatible).warnings.isNotEmpty())
        GBApplication.getDeviceSpecificSharedPrefs("AA:BB:CC:DD:EE:FF").edit {
            putStringSet(GarminPreferences.PREF_GARMIN_CAPABILITIES, setOf(GarminCapability.WORKOUT_DOWNLOAD.name))
        }
        assertEquals(Compatibility.Compatible(), handler().checkCompatibility(parsed, device()))
    }
}
