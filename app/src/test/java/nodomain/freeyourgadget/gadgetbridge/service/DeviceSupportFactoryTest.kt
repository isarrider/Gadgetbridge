package nodomain.freeyourgadget.gadgetbridge.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import nodomain.freeyourgadget.gadgetbridge.BuildConfig
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.util.preferences.DevicePrefs
import nodomain.freeyourgadget.gadgetbridge.util.preferences.ScopedPrefsProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.EnumSet

class DeviceSupportFactoryTest {
    private val bluetoothAdapter = mockk<BluetoothAdapter> {
        every { isEnabled } returns true
    }
    private val bluetoothManager = mockk<BluetoothManager> {
        every { adapter } returns bluetoothAdapter
    }
    private val context = mockk<Context> {
        every { getSystemService(Context.BLUETOOTH_SERVICE) } returns bluetoothManager
    }
    private val coordinator = mockk<DeviceCoordinator> {
        every { connectionType } returns DeviceCoordinator.ConnectionType.BOTH
    }
    private val scopedPrefs = mockk<ScopedPrefsProvider>()
    private val dependencies = DeviceSupportDependencies(context, scopedPrefs)
    private val factory = DeviceSupportFactory(dependencies)

    private fun device(deviceAddress: String) = mockk<GBDevice> {
        every { address } returns deviceAddress
        every { deviceCoordinator } returns coordinator
    }

    @Test
    fun supportBuiltByClassNameGetsTheFactoryContext() {
        val support = factory.createDeviceSupport(device(NoOpDeviceSupport::class.java.name))

        assertTrue(support is NoOpDeviceSupport)
        assertSame(context, support!!.context)
    }

    @Test
    fun bluetoothSupportIsWrappedAndGetsTheFactoryContext() {
        val device = device("00:11:22:33:44:55")
        val inner = spyk(NoOpDeviceSupport())
        every { coordinator.createDeviceSupport(device) } returns inner
        every { coordinator.initialFlags } returns EnumSet.noneOf(ServiceDeviceSupport.Flags::class.java)

        val support = factory.createDeviceSupport(device)

        assertNotNull(support)
        assertTrue(support is ServiceDeviceSupport)
        assertSame(context, inner.context)
        assertSame(context, support!!.context)
        verify { inner.setContext(device, bluetoothAdapter, dependencies) }
    }

    @Test
    fun bypassBusyCheckingSettingRemovesBusyChecking() {
        val built = busyDeviceSupportWithBusyChecking(bypass = true)

        built.wrapper.onSetTime()

        verify { built.inner.onSetTime() }
    }

    @Test
    fun withoutBypassBusyCheckingSettingABusyDeviceIgnoresCommands() {
        val built = busyDeviceSupportWithBusyChecking(bypass = false)

        built.wrapper.onSetTime()

        verify(exactly = 0) { built.inner.onSetTime() }
    }

    private class Built(val wrapper: DeviceSupport, val inner: DeviceSupport)

    /** Builds a support for a busy device whose coordinator asks for busy checking. */
    private fun busyDeviceSupportWithBusyChecking(bypass: Boolean): Built {
        // The factory only reads the setting in debug builds.
        assumeTrue(BuildConfig.DEBUG)
        val device = device("00:11:22:33:44:55")
        every { device.isBusy } returns true
        // Logged when busy checking ignores a command.
        every { device.busyTask } returns null
        val inner = spyk(NoOpDeviceSupport())
        every { coordinator.createDeviceSupport(device) } returns inner
        every { coordinator.initialFlags } returns EnumSet.of(ServiceDeviceSupport.Flags.BUSY_CHECKING)
        val devicePrefs = mockk<DevicePrefs> {
            every { getBoolean("pref_device_debug_remove_busy_checking", false) } returns bypass
        }
        every { scopedPrefs.getDevicePrefs(device) } returns devicePrefs

        return Built(factory.createDeviceSupport(device)!!, inner)
    }
}

/**
 * A support that does nothing. The factory's class-name path builds a support by reflection, so it needs a real
 * class with a public no-arg constructor; tests that check calls on it wrap it in a MockK spy.
 */
class NoOpDeviceSupport : AbstractDeviceSupport() {
    override fun connect(): Boolean = false

    override fun useAutoConnect(): Boolean = false

    override fun dispose() {}
}
