package nodomain.freeyourgadget.gadgetbridge.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
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
    private val dependencies = DeviceSupportDependencies(context)
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
