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
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.DeviceSettingsSpec
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.ListEntry
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.dsl.deviceSettings
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractBLClassicDeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator
import nodomain.freeyourgadget.gadgetbridge.devices.GenericSpo2SampleProvider
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider
import nodomain.freeyourgadget.gadgetbridge.devices.SleepSessionProvider
import nodomain.freeyourgadget.gadgetbridge.devices.TimeSampleProvider
import nodomain.freeyourgadget.gadgetbridge.entities.AbstractActivitySample
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.model.Spo2Sample
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

    override fun supportsHeartRateMeasurement(device: GBDevice): Boolean = true

    override fun supportsDataFetching(device: GBDevice): Boolean = true

    override fun supportsCharts(device: GBDevice): Boolean = true

    override fun supportsActivityTracking(device: GBDevice): Boolean = true

    override fun supportsStepCounter(device: GBDevice): Boolean = true

    override fun supportsActiveCalories(device: GBDevice): Boolean = true

    override fun getSampleProvider(device: GBDevice, session: DaoSession): SampleProvider<out AbstractActivitySample>? =
        OnePlusActivitySampleProvider(device, session)

    override fun supportsSpo2(device: GBDevice): Boolean = true

    override fun supportsSleepMeasurement(device: GBDevice): Boolean = true

    override fun getSpo2SampleProvider(device: GBDevice, session: DaoSession): TimeSampleProvider<out Spo2Sample> =
        GenericSpo2SampleProvider(device, session)

    override fun getSleepSessionProvider(device: GBDevice, session: DaoSession): SleepSessionProvider =
        OnePlusSleepSessionProvider(device, session)

    override fun getDeviceSettings(device: GBDevice): DeviceSettingsSpec = deviceSettings {
        screen(
            key = "oneplus_health",
            title = R.string.pref_header_health,
            icon = R.drawable.ic_heartrate,
        ) {
            list(
                key = PREF_HR_MONITORING,
                title = R.string.oneplus_hr_monitoring_title,
                icon = R.drawable.ic_heartrate,
                entries = listOf(
                    ListEntry.Res(HR_MODE_OFF, R.string.oneplus_hr_monitoring_off),
                    ListEntry.Res(HR_MODE_SMART, R.string.oneplus_hr_monitoring_smart),
                    ListEntry.Res(HR_MODE_REALTIME, R.string.oneplus_hr_monitoring_realtime),
                ),
                defaultValue = HR_MODE_OFF,
            )
        }
        screen(
            key = "oneplus_developer",
            title = R.string.action_debug,
            icon = R.drawable.ic_developer_mode,
        ) {
            switchSetting(
                key = PREF_DIAGNOSTICS,
                title = R.string.oneplus_diagnostics_title,
                summary = R.string.oneplus_diagnostics_summary,
                icon = R.drawable.ic_developer_mode,
                defaultValue = false,
            )
        }
    }

    companion object {
        /** Heart-rate monitoring mode: how the watch measures and how often Gadgetbridge pulls the history. */
        const val PREF_HR_MONITORING = "oneplus_hr_monitoring"
        const val PREF_DIAGNOSTICS = "oneplus_diagnostics"
        const val HR_MODE_OFF = "off"
        const val HR_MODE_SMART = "smart"
        const val HR_MODE_REALTIME = "realtime"
    }
}
