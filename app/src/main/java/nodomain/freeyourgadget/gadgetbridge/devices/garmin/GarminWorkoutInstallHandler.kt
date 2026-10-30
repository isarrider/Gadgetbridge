/* SPDX-License-Identifier: AGPL-3.0-or-later */
package nodomain.freeyourgadget.gadgetbridge.devices.garmin

import android.content.Context
import android.net.Uri
import android.os.Bundle
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.install.*
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.FileType
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.messages.FitWorkout
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.messages.FitWorkoutStep
import nodomain.freeyourgadget.gadgetbridge.util.UriUtils.fileNameOf

/** Atomic preparation: an invalid or mixed batch is rejected before any file is sent. */
class GarminWorkoutInstallHandler(private val context: Context, private val coordinator: GarminCoordinator) : WorkoutInstallHandler {
    override fun prepare(files: List<Uri>): ParsedFiles {
        if (files.isEmpty()) return ParsedFiles.Unsupported(context.getString(R.string.fwinstaller_file_not_compatible_to_device))
        if (files.size > 32) return ParsedFiles.Unsupported(context.getString(R.string.workout_batch_limit))
        var totalBytes = 0L
        val items = ArrayList<InstallItem>()
        for (uri in files) {
            val handler = GarminFitFileInstallHandler(uri, Bundle.EMPTY, context)
            val records = handler.fitFile?.records.orEmpty()
            val workout = records.filterIsInstance<FitWorkout>().singleOrNull()
            val steps = records.filterIsInstance<FitWorkoutStep>()
            if (handler.fileType != FileType.FILETYPE.WORKOUTS || workout == null || steps.isEmpty() || workout.numValidSteps != steps.size ||
                steps.map { it.messageIndex } != steps.indices.toList() ||
                steps.any { it.durationType == null || it.durationValue == null || it.targetType == null } || handler.rawBytes == null) {
                return ParsedFiles.Unsupported(context.getString(R.string.install_files_not_supported, fileNameOf(context, uri)))
            }
            totalBytes += handler.rawBytes.size
            if (totalBytes > 20L * 1024 * 1024) return ParsedFiles.Unsupported(context.getString(R.string.workout_batch_limit))
            items += GarminWorkoutInstallItem(workout.wktName?.takeIf { it.isNotBlank() } ?: fileNameOf(context, uri), handler.rawBytes, coordinator.defaultIconResource)
        }
        return ParsedFiles.Supported(items, context.getString(R.string.fw_upgrade_notice, context.getString(R.string.menuitem_workout)))
    }

    override fun checkCompatibility(parsedFiles: ParsedFiles.Supported, device: GBDevice): Compatibility {
        if (coordinator.supports(device, GarminCapability.WORKOUT_DOWNLOAD)) return Compatibility.Compatible()
        if (GBApplication.getDevicePrefs(device).installUnsupportedFiles()) return Compatibility.Compatible(listOf(context.getString(R.string.install_unsupported_files_warning)))
        return Compatibility.Incompatible(context.getString(R.string.fwapp_install_device_not_supported))
    }
}

class GarminWorkoutInstallItem(name: String, val bytes: ByteArray, icon: Int) : InstallItem(name, icon, bytes.size.toLong())
