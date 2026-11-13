package nodomain.freeyourgadget.gadgetbridge.service

import android.bluetooth.BluetoothAdapter
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.util.preferences.DevicePrefs
import nodomain.freeyourgadget.gadgetbridge.util.preferences.ScopedPrefsProvider
import org.junit.Assert.assertSame
import org.junit.Test

class AbstractDeviceSupportTest {
    private val device = mockk<GBDevice>()
    private val bluetoothAdapter = mockk<BluetoothAdapter>()
    private val context = mockk<Context>()
    private val scopedPrefs = mockk<ScopedPrefsProvider>()

    @Test
    fun devicePrefsComeFromTheScopedPrefsProviderForItsDevice() {
        val devicePrefs = mockk<DevicePrefs>()
        every { scopedPrefs.getDevicePrefs(device) } returns devicePrefs
        val support = NoOpDeviceSupport()

        support.setContext(device, bluetoothAdapter, DeviceSupportDependencies(context, scopedPrefs))

        assertSame(devicePrefs, support.devicePrefs)
        assertSame(context, support.context)
    }
}
