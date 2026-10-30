/* SPDX-License-Identifier: AGPL-3.0-or-later */
package nodomain.freeyourgadget.gadgetbridge.companion

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest
import java.util.Locale

internal fun digest(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

/** Store approval against the installed signer, never a caller-supplied package extra. */
class WorkoutAccess(private val context: Context) {
    private val prefs = context.getSharedPreferences("workout_companions", Context.MODE_PRIVATE)
    private fun key(pkg: String, device: String) = "$pkg|${device.uppercase(Locale.ROOT)}"

    @Suppress("DEPRECATION")
    internal fun signer(pkg: String): String {
        val pm = context.packageManager
        val signatures = if (Build.VERSION.SDK_INT >= 28) pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo?.apkContentsSigners
            else pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES).signatures
        require(!signatures.isNullOrEmpty()) { "Package has no signing identity" }
        return signatures.map { digest(it.toByteArray()) }.sorted().joinToString(":")
    }

    fun approve(pkg: String, device: String) {
        check(prefs.edit().putString(key(pkg, device), signer(pkg)).commit())
    }

    fun revoke(pkg: String, device: String) {
        check(prefs.edit().remove(key(pkg, device)).commit())
    }

    fun approvals(): List<Pair<String, String>> = prefs.all.keys.mapNotNull {
        it.split('|', limit = 2).takeIf { parts -> parts.size == 2 }?.let { parts -> parts[0] to parts[1] }
    }

    fun authorized(pkg: String, device: String): Boolean = runCatching {
        prefs.getString(key(pkg, device), null) == signer(pkg)
    }.getOrDefault(false)

    fun owner(uid: Int, device: String): String {
        val pkg = context.packageManager.getPackagesForUid(uid).orEmpty().firstOrNull { authorized(it, device) }
            ?: throw SecurityException("Workout access has not been granted for this caller and device")
        return "$pkg|${signer(pkg)}"
    }

    fun ownerAuthorized(owner: String, device: String): Boolean {
        val pkg = owner.substringBefore('|')
        return authorized(pkg, device) && runCatching { "$pkg|${signer(pkg)}" == owner }.getOrDefault(false)
    }
}
