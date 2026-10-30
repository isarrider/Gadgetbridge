/* SPDX-License-Identifier: AGPL-3.0-or-later */
package nodomain.freeyourgadget.gadgetbridge.companion

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import java.io.File

/** Only structured workout jobs are exposed; the device communication service remains private. */
class WorkoutSyncService : Service() {
    private val access by lazy { WorkoutAccess(this) }
    private val store by lazy { WorkoutJobStore(File(filesDir, "companion-workouts")) }
    private val api = object : IWorkoutSyncService.Stub() {
        override fun getApiVersion() = 1
        override fun getDevices(): Array<Bundle> {
            val uid = Binder.getCallingUid()
            return GBApplication.app().deviceManager.devices.mapNotNull { device ->
                if (!device.deviceCoordinator.supportsWorkoutFileImport(device)) return@mapNotNull null
                runCatching { access.owner(uid, device.address) }.getOrNull()?.let {
                    Bundle().apply {
                        putString("device", device.address); putString("name", device.aliasOrName); putBoolean("connected", device.isInitialized)
                    }
                }
            }.toTypedArray()
        }
        override fun submitWorkout(requestId: String, deviceAddress: String, contentUri: Uri): Bundle {
            val owner = access.owner(Binder.getCallingUid(), deviceAddress)
            val device = requireNotNull(GBApplication.app().deviceManager.getDeviceByAddress(deviceAddress)) { "unknown_device" }
            require(device.deviceCoordinator.supportsWorkoutFileImport(device)) { "unsupported_device" }
            require(contentUri.scheme == "content") { "content_uri_required" }
            val provider = packageManager.resolveContentProvider(contentUri.authority ?: "", 0)
                ?: throw IllegalArgumentException("unknown_provider")
            // An approved app must not use Gadgetbridge as a deputy to read another app's private URI.
            if (provider.applicationInfo.uid != Binder.getCallingUid() && checkUriPermission(contentUri, Binder.getCallingPid(), Binder.getCallingUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Caller cannot read this content URI")
            }
            val identity = Binder.clearCallingIdentity()
            try {
                val bytes = contentResolver.openInputStream(contentUri)?.use { input ->
                    val out = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        require(out.size() + read <= WorkoutJobStore.MAX_BYTES) { "file_too_large" }
                        out.write(buffer, 0, read)
                    }
                    out.toByteArray()
                } ?: throw IllegalArgumentException("unreadable_uri")
                val job = store.submit(owner, requestId, deviceAddress, bytes)
                if (job.state == "QUEUED") WorkoutSyncWorker.enqueue(this@WorkoutSyncService, job.id)
                return job.bundle()
            } catch (e: java.io.IOException) {
                throw IllegalStateException("workout_storage_unavailable", e)
            } finally {
                Binder.restoreCallingIdentity(identity)
            }
        }
        override fun getJob(requestId: String, deviceAddress: String): Bundle =
            store.get(access.owner(Binder.getCallingUid(), deviceAddress), requestId, deviceAddress)?.bundle() ?: Bundle().apply { putString("state", "NOT_FOUND") }
        override fun forgetJob(requestId: String, deviceAddress: String) {
            store.forget(access.owner(Binder.getCallingUid(), deviceAddress), requestId, deviceAddress)
        }
        override fun cancelJob(requestId: String, deviceAddress: String): Bundle {
            val job = store.get(access.owner(Binder.getCallingUid(), deviceAddress), requestId, deviceAddress) ?: return Bundle().apply { putString("state", "NOT_FOUND") }
            return store.cancel(job.id).bundle()
        }
    }
    override fun onBind(intent: Intent) = api
}
