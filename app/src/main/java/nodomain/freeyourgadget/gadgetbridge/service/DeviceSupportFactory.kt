/*  Copyright (C) 2015-2024 115ek, Andreas Böhler, Andreas Shimokawa, Andrew
    Watkins, angelpup, Carsten Pfeiffer, Cre3per, criogenic, DanialHanif, Daniel
    Dakhno, Daniele Gobbetti, Daniel Thompson, Da Pa, Dmytro Bielik, Frank Ertl,
    GeekosaurusR3x, Gordon Williams, Jean-François Greffier, jfgreffier, jhey,
    João Paulo Barraca, Jochen S, Johannes Krude, José Rebelo, ladbsoft,
    Lesur Frederic, mamucho, Manuel Ruß, maxirnilian, mkusnierz, narektor,
    Noodlez, odavo32nof, opavlov, pangwalla, Pavel Elagin, Petr Kadlec, Petr
    Vaněk, protomors, Quallenauge, Quang Ngô, Raghd Hamzeh, Sami Alaoui,
    Sebastian Kranz, sedy89, Sergey Trofimov, Sophanimus, Stefan Bora, Taavi
    Eomäe, thermatk, tiparega, Vadim Kaushan, x29a, xaos, Yukai Li

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
package nodomain.freeyourgadget.gadgetbridge.service

import android.bluetooth.BluetoothManager
import android.content.Context
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import nodomain.freeyourgadget.gadgetbridge.BuildConfig
import nodomain.freeyourgadget.gadgetbridge.GBException
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.service.devices.pebble.PebbleSupport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.EnumSet

class DeviceSupportFactory internal constructor(private val dependencies: DeviceSupportDependencies) {
    private val context: Context = dependencies.context

    // Null on a phone without Bluetooth. The class-name and TCP paths still pass it to setContext, whose adapter
    // parameter is nullable for that reason.
    private val btAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    @Synchronized
    @Throws(GBException::class)
    fun createDeviceSupport(device: GBDevice): DeviceSupport? {
        if (device.deviceCoordinator.connectionType == DeviceCoordinator.ConnectionType.USB) {
            return createUsbDeviceSupport(device)
        }

        val deviceAddress = device.address
        val indexFirstColon = deviceAddress.indexOf(":")
        return if (indexFirstColon > 0) {
            if (indexFirstColon == deviceAddress.lastIndexOf(":")) { // only one colon
                createTCPDeviceSupport(device)
            } else {
                // multiple colons -- bt?
                createBTDeviceSupport(device)
            }
        } else {
            // no colon at all, maybe a class name?
            createClassNameDeviceSupport(device)
        }
    }

    @Throws(GBException::class)
    private fun createClassNameDeviceSupport(device: GBDevice): DeviceSupport? {
        val className = device.address
        try {
            val deviceSupportClass = Class.forName(className)
            val constructor = deviceSupportClass.getConstructor()
            val support = constructor.newInstance() as DeviceSupport
            // has to create the device itself
            support.setContext(device, btAdapter, dependencies)
            return support
        } catch (e: ClassNotFoundException) {
            LOG.warn("Unable to find DeviceSupport class {} for {}", className, device.address)
            return null // not a class, or not known at least
        } catch (e: Exception) {
            throw GBException("Error creating DeviceSupport instance for $className", e)
        }
    }

    @Throws(GBException::class)
    private fun createServiceDeviceSupport(device: GBDevice): ServiceDeviceSupport {
        val coordinator = device.deviceCoordinator

        val supportInstance = coordinator.createDeviceSupport(device)

        val initialFlags = coordinator.initialFlags
        if (BuildConfig.DEBUG && initialFlags.contains(ServiceDeviceSupport.Flags.BUSY_CHECKING)) {
            val devicePrefs = dependencies.scopedPrefs.getDevicePrefs(device)
            if (devicePrefs.getBoolean("pref_device_debug_remove_busy_checking", false)) {
                LOG.warn("Removing BUSY_CHECKING flag from {}", device.address)
                initialFlags.remove(ServiceDeviceSupport.Flags.BUSY_CHECKING)
            }
        }

        return ServiceDeviceSupport(supportInstance, initialFlags)
    }

    @Throws(GBException::class)
    private fun createBTDeviceSupport(gbDevice: GBDevice): DeviceSupport? {
        if (btAdapter == null) {
            LOG.warn("Unable to create bt device support for {} - no bt adapter", gbDevice.address)
            return null
        }
        if (!btAdapter.isEnabled) {
            LOG.warn("Unable to create bt device support for {} - bt is disabled", gbDevice.address)
            return null
        }

        try {
            val deviceSupport: DeviceSupport = createServiceDeviceSupport(gbDevice)
            deviceSupport.setContext(gbDevice, btAdapter, dependencies)
            return deviceSupport
        } catch (e: Exception) {
            throw GBException(context.getString(R.string.cannot_connect_bt_address_invalid_), e)
        }
    }

    @Throws(GBException::class)
    private fun createTCPDeviceSupport(gbDevice: GBDevice): DeviceSupport {
        try {
            val deviceSupport: DeviceSupport = ServiceDeviceSupport(PebbleSupport(), EnumSet.of(ServiceDeviceSupport.Flags.BUSY_CHECKING))
            deviceSupport.setContext(gbDevice, btAdapter, dependencies)
            return deviceSupport
        } catch (e: Exception) {
            throw GBException("cannot connect to $gbDevice", e) // FIXME: localize
        }
    }

    @Throws(GBException::class)
    private fun createUsbDeviceSupport(gbDevice: GBDevice): DeviceSupport? {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
        if (manager == null) {
            LOG.warn("No UsbManager available on this device")
            return null
        }

        val accessory = findUsbAccessory(manager, gbDevice)
        if (accessory == null) {
            LOG.warn("No compatible USB accessory attached for {}", gbDevice.address)
            return null
        }

        try {
            val deviceSupport: DeviceSupport = createServiceDeviceSupport(gbDevice)
            deviceSupport.setContext(gbDevice, accessory, dependencies)
            return deviceSupport
        } catch (e: Exception) {
            throw GBException("cannot connect to $gbDevice", e)
        }
    }

    private fun findUsbAccessory(usbManager: UsbManager, gbDevice: GBDevice): UsbAccessory? {
        val accessoryList = usbManager.accessoryList
        if (accessoryList == null) {
            LOG.warn("USB accessory list is null")
            return null
        }

        for (candidate in accessoryList) {
            LOG.debug("Checking compatibility with {}", candidate)
            if (!usbManager.hasPermission(candidate)) {
                // when we get here, we must already have permission. If not, it's not this device
                continue
            }
            // We don't check if the coordinator supports it, since it might be an unsupported device
            // that was added as a test device. Once we reach here, we must be able to match by
            // serial number.
            if (gbDevice.address == "usb:" + candidate.serial) {
                return candidate
            }
        }

        return null
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(DeviceSupportFactory::class.java)
    }
}
