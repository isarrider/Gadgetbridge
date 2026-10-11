/*  Copyright (C) 2025 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppos

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.DrawableRes
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.install.InstallHandlerV2
import nodomain.freeyourgadget.gadgetbridge.install.InstallItem
import nodomain.freeyourgadget.gadgetbridge.install.ParsedFiles
import nodomain.freeyourgadget.gadgetbridge.install.Compatibility
import nodomain.freeyourgadget.gadgetbridge.util.UriUtils.fileNameOf
import nodomain.freeyourgadget.gadgetbridge.util.audio.AudioInfo
import nodomain.freeyourgadget.gadgetbridge.util.audio.MusicUtils
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

class ZeppOsMusicInstallHandler(
    private val context: Context,
    private val coordinator: ZeppOsCoordinator,
) : InstallHandlerV2 {
    override fun prepare(files: List<Uri>): ParsedFiles {
        val audioInfos = files.associateWith { audioInfoOf(context, it) }
        val notMp3 = audioInfos.filterValues { it == null }.keys
        if (notMp3.isNotEmpty()) {
            LOG.warn("Not mp3 files: {}", notMp3)
            return ParsedFiles.Unsupported(
                context.getString(
                    R.string.install_files_not_supported,
                    notMp3.joinToString(", ") { fileNameOf(context, it) })
            )
        }

        val pictureSizePx = (PICTURE_SIZE_DP * context.resources.displayMetrics.density).roundToInt()
        val items = audioInfos.mapNotNull { (uri, audioInfo) ->
            audioInfo?.let {
                ZeppOsMusicInstallItem(
                    uri,
                    it,
                    title(it),
                    coordinator.defaultIconResource,
                    details(it),
                    MusicUtils.albumArt(context, uri, pictureSizePx)
                )
            }
        }
        return ParsedFiles.Supported(
            items,
            context.getString(R.string.fw_upgrade_notice, context.getString(R.string.menuitem_music))
        )
    }

    override fun checkCompatibility(parsedFiles: ParsedFiles.Supported, device: GBDevice): Compatibility {
        if (!coordinator.supportsMusicUpload(device)) {
            return Compatibility.Incompatible(context.getString(R.string.fwapp_install_device_not_supported))
        }
        return Compatibility.Compatible()
    }

    private fun title(audioInfo: AudioInfo): String {
        val track = audioInfo.trackNumber?.substringBefore('/')?.trim()?.ifEmpty { null }
            ?: return audioInfo.title
        return context.getString(R.string.install_music_track_title, track, audioInfo.title)
    }

    private fun details(audioInfo: AudioInfo): String? = listOfNotNull(
        audioInfo.artist,
        audioInfo.album?.ifEmpty { null },
    ).joinToString(" · ").ifEmpty { null }

    companion object {
        private val LOG = LoggerFactory.getLogger(ZeppOsMusicInstallHandler::class.java)

        private const val PICTURE_SIZE_DP = 48

        /**
         * Returns the audio info of the file, or null if it is not an mp3 file.
         */
        @JvmStatic
        fun audioInfoOf(context: Context, uri: Uri): AudioInfo? {
            val audioInfo = MusicUtils.audioInfoFromUri(context, uri) ?: return null
            if (audioInfo.mimeType != "audio/mpeg" || audioInfo.extension != "mp3") {
                return null
            }
            return audioInfo
        }
    }
}

class ZeppOsMusicInstallItem(
    val uri: Uri,
    val audioInfo: AudioInfo,
    displayName: String,
    @DrawableRes icon: Int,
    details: String?,
    picture: Bitmap?,
) : InstallItem(displayName, icon, audioInfo.fileSize, details, picture)
