package nodomain.freeyourgadget.gadgetbridge.devices

import dagger.hilt.EntryPoints
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType
import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks that every [DeviceType]'s coordinator binding builds the same class as the enum's own recipe. It runs on
 * the application's real Hilt graph, which is what it checks. It's removed together with the enum's recipes.
 */
class DeviceCoordinatorBindingsMatchRecipesTest : TestBase() {
    @Test
    fun everyBindingBuildsTheClassOfTheEnumsRecipe() {
        val factory = EntryPoints.get(app, DeviceCoordinatorFactoryEntryPoint::class.java).deviceCoordinatorFactory()

        val mismatches = DeviceType.entries.mapNotNull { type ->
            val expected = type.deviceCoordinator.javaClass
            val actual = runCatching { factory.getDeviceCoordinator(type).javaClass }
                .getOrElse { return@mapNotNull "$type: ${it.message}" }
            if (actual == expected) null else "$type: bound to ${actual.name}, the recipe builds ${expected.name}"
        }

        assertTrue(mismatches.joinToString("\n", "Bindings that don't match the recipes:\n"), mismatches.isEmpty())
    }
}
