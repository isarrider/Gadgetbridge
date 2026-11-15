package nodomain.freeyourgadget.gadgetbridge.service.devices.nikon

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Adapted from <a href="https://github.com/gkoh/furble/blob/master/lib/furble/NikonSmart.cpp">furble</a>.
 */
object NikonCrypto {
    private val KEY = "ffffaa5511223300".hexToByteArray()
    private val IV = "0102030405060708".hexToByteArray()

    private val SALTS = listOf(
        "704066e40433d552",
        "ed4b8fac15f7e47b",
        "24471f118b5ea1fc",
        "05960c312b8c7f41",
        "fda588c1eba8b1f3",
        "991660561bd3d550",
        "cd32687fa9e28a30",
        "2a8fe834dec7ebf4",
    ).map { it.hexToByteArray() }

    /**
     * Verifies the hash sent by the camera, and calculates the response hash.
     *
     * @return the hash to send back in stage 3, or null if no salt matches the hash sent by camera
     */
    fun computeReply(localNonce: ByteArray, remoteNonce: ByteArray, remoteHash: ByteArray): ByteArray? {
        val salt = SALTS.firstOrNull { hash(it + remoteNonce + localNonce).contentEquals(remoteHash) }
            ?: return null
        return hash(salt + localNonce + remoteNonce)
    }

    private fun hash(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("Blowfish/CBC/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(KEY, "Blowfish"), IvParameterSpec(IV))
        val encrypted = cipher.doFinal(data)
        return encrypted.copyOfRange(encrypted.size - 8, encrypted.size)
    }
}
