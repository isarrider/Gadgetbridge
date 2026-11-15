package nodomain.freeyourgadget.gadgetbridge.service.devices.nikon

import nodomain.freeyourgadget.gadgetbridge.service.gallery.GalleryCapability
import nodomain.freeyourgadget.gadgetbridge.service.gallery.GalleryFilter
import nodomain.freeyourgadget.gadgetbridge.service.gallery.GalleryPage
import nodomain.freeyourgadget.gadgetbridge.service.gallery.GallerySource
import nodomain.freeyourgadget.gadgetbridge.service.gallery.GalleryStream
import nodomain.freeyourgadget.gadgetbridge.service.gallery.MediaFile
import nodomain.freeyourgadget.gadgetbridge.service.gallery.Variant
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.EnumSet

/**
 * The [GallerySource] of a Nikon camera, over btbr.
 */
class NikonGallerySource : GallerySource {
    override val capabilities: Set<GalleryCapability> = EnumSet.of(
        //GalleryCapability.FAVORITE,
        //GalleryCapability.DELETE,
        GalleryCapability.FILTER_TYPE,
        //GalleryCapability.FILTER_FAVORITE,
        //GalleryCapability.SEEK,
    )

    override suspend fun acquire() {
        TODO("acquire")
    }

    override fun release() {
        TODO("release")
    }

    override suspend fun listPage(filter: GalleryFilter, after: String?): GalleryPage =
        TODO("listPage")

    override fun open(file: MediaFile, variant: Variant, offset: Long, length: Long): GalleryStream =
        TODO("open")

    override suspend fun setFavorite(files: List<MediaFile>, favorite: Boolean) =
        TODO("setFavorite")

    override suspend fun delete(files: List<MediaFile>) =
        TODO("delete")

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(NikonGallerySource::class.java)
    }
}
