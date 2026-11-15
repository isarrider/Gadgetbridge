package nodomain.freeyourgadget.gadgetbridge.service.devices.nikon

import nodomain.freeyourgadget.gadgetbridge.util.GB
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NikonCryptoTest {
    @Test
    fun testComputeReply() {
        assertReply("6b8b4567327b23c6", "5e1206613ec14e8a", "b1bcc5d24aa975ac", "fd64f717655658db")
        assertReply("30e083b0718f72c1", "0264f5af41bfac74", "36f06a020f33371a", "14da7beba5434d1d")
        assertReply("30e083b0718f72c1", "31bccbc241eec446", "70c33d40471bdd8f", "1c2ec0e914ba50f8")
    }

    @Test
    fun testComputeReplyInvalidHash() {
        assertNull(
            NikonCrypto.computeReply(
                GB.hexStringToByteArray("6b8b4567327b23c6"),
                GB.hexStringToByteArray("5e1206613ec14e8a"),
                GB.hexStringToByteArray("0000000000000000"),
            )
        )
    }

    private fun assertReply(localNonce: String, remoteNonce: String, remoteHash: String, expected: String) {
        assertArrayEquals(
            GB.hexStringToByteArray(expected),
            NikonCrypto.computeReply(
                GB.hexStringToByteArray(localNonce),
                GB.hexStringToByteArray(remoteNonce),
                GB.hexStringToByteArray(remoteHash),
            )
        )
    }
}
