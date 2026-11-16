package nodomain.freeyourgadget.gadgetbridge.devices

import dagger.hilt.EntryPoints
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType
import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks that every [DeviceType] has a coordinator in the Hilt map, and that each one can be built. It's a Robolectric
 * test because what it checks is the application's real Hilt graph.
 */
class DeviceCoordinatorBindingsTest : TestBase() {
    @Test
    fun everyDeviceTypeHasACoordinatorThatCanBeBuilt() {
        val factory = EntryPoints.get(app, DeviceCoordinatorFactoryEntryPoint::class.java).deviceCoordinatorFactory()

        val unbound = mutableListOf<DeviceType>()
        val unbuildable = mutableListOf<String>()
        for (type in DeviceType.entries) {
            try {
                factory.getDeviceCoordinator(type)
            } catch (e: Exception) {
                if (e is IllegalStateException && e.message == "No coordinator is bound for DeviceType.$type") {
                    unbound += type
                } else {
                    unbuildable += "$type: $e"
                }
            }
        }

        assertTrue(unboundMessage(unbound), unbound.isEmpty())
        assertTrue(unbuildable.joinToString("\n", "These coordinators can't be built:\n"), unbuildable.isEmpty())
    }

    private fun unboundMessage(unbound: List<DeviceType>): String {
        val example = unbound.firstOrNull() ?: return ""
        val bindingName = example.name.lowercase().split("_")
            .mapIndexed { i, part -> if (i == 0) part else part.replaceFirstChar(Char::uppercase) }
            .joinToString("")
        return """
            |These device types have no coordinator in the Hilt map: ${unbound.joinToString(", ")}.
            |For each one:
            |  1. Give its coordinator an @Inject constructor
            |     (Java: `@Inject public FooCoordinator() {}`, Kotlin: `class FooCoordinator @Inject constructor()`).
            |  2. Bind it in its vendor's coordinator module (e.g. GarminCoordinatorModule), for example:
            |     @Binds
            |     @IntoMap
            |     @DeviceTypeKey(DeviceType.$example)
            |     fun $bindingName(coordinator: FooCoordinator): DeviceCoordinator
        """.trimMargin()
    }
}
