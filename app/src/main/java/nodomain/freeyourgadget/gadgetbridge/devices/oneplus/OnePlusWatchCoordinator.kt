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
package nodomain.freeyourgadget.gadgetbridge.devices.oneplus

import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractBLClassicDeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport
import nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus.OnePlusWatchSupport
import java.util.regex.Pattern

/** OnePlus Watch (2021, model W301GB, advertises as "OnePlus Watch XXXX"). Not the Wear OS "Watch 2". */
class OnePlusWatchCoordinator : AbstractBLClassicDeviceCoordinator() {
    override fun getSupportedDeviceName(): Pattern = Pattern.compile("^OnePlus Watch [0-9A-F]{4}$")

    override fun getManufacturer(): String = "OnePlus"

    override fun isExperimental(): Boolean = true

    override fun getDeviceNameResource(): Int = R.string.devicetype_oneplus_watch_gen1

    override fun getDefaultIconResource(): Int = R.drawable.ic_device_default

    override fun getDeviceKind(device: GBDevice): DeviceCoordinator.DeviceKind = DeviceCoordinator.DeviceKind.WATCH

    override fun createDeviceSupport(device: GBDevice): DeviceSupport = OnePlusWatchSupport()

    // Standard Bluetooth bond (SSP numeric comparison) is all the watch needs; no app-level key.
    override fun getBondingStyle(): Int = BONDING_STYLE_BOND
}
