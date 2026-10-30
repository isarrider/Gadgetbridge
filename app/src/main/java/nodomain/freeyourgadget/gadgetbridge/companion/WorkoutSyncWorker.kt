/* SPDX-License-Identifier: AGPL-3.0-or-later */
package nodomain.freeyourgadget.gadgetbridge.companion

import android.content.Context
import android.net.Uri
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.install.*
import java.io.File
import java.util.concurrent.TimeUnit

/** Persists requests across app/process restarts. A transfer acknowledgement is not proof the watch shows the workout. */
class WorkoutSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        try { return MUTEX.withLock {
        val store = WorkoutJobStore(File(applicationContext.filesDir, "companion-workouts"))
        val id = inputData.getString("id") ?: return@withLock Result.failure()
        val job = store.get(id) ?: return@withLock Result.failure()
        if (job.state in WorkoutJobStore.TERMINAL) return@withLock Result.success()
        if (!WorkoutAccess(applicationContext).ownerAuthorized(job.owner, job.device)) {
            store.update(id, if (job.state == "QUEUED") "FAILED" else "UNCERTAIN", "access_revoked"); return@withLock Result.success()
        }
        if (job.state == "TRANSFERRING") {
            // Process died during an upload; blindly retrying could duplicate a completed workout.
            store.update(id, "UNCERTAIN", "transfer_interrupted"); return@withLock Result.success()
        }
        val device = GBApplication.app().deviceManager.getDeviceByAddress(job.device)
        if (device == null) { store.update(id, "FAILED", "unknown_device"); return@withLock Result.success() }
        if (!store.file(job).isFile) { store.update(id, "FAILED", "missing_file"); return@withLock Result.success() }
        val session = withContext(Dispatchers.Main) {
            if (InstallManager.forDevice(device)?.state?.value?.let { it !is InstallState.Finished } == true || device.isBusy) null
            else InstallManager.start(device, listOf(Uri.fromFile(store.file(job))))
        } ?: return@withLock Result.success() // Still queued; reconnect/busy-state events wake it again.
        try {
            val prepared = withTimeoutOrNull(30_000) {
                session.state.first { it is InstallState.Ready || it is InstallState.Blocked }
            }
            if (prepared == null) return@withLock Result.success() // Keep the durable request queued while disconnected.
            if (prepared !is InstallState.Ready || session.handler !is WorkoutInstallHandler) {
                store.update(id, "FAILED", "unsupported_workout"); return@withLock Result.success()
            }
            if (prepared.warnings.isNotEmpty()) {
                store.update(id, "FAILED", "confirmation_required"); return@withLock Result.success()
            }
            val started = withContext(Dispatchers.Main) {
                if (InstallManager.forDevice(device) !== session || !WorkoutAccess(applicationContext).ownerAuthorized(job.owner, job.device) || store.get(id)?.state != "QUEUED") false
                else if (!store.claim(id)) false else {
                    InstallManager.install(session, null)
                    session.state.value is InstallState.Installing
                }
            }
            if (!started) {
                if (store.get(id)?.state == "TRANSFERRING") store.update(id, "FAILED", "device_not_ready")
                return@withLock Result.success()
            }
            val result = withTimeoutOrNull(120_000) { session.state.first { it is InstallState.Finished } } as? InstallState.Finished
            when {
                result == null -> store.update(id, "UNCERTAIN", "transfer_timeout")
                result.items.all { it.status is ItemStatus.Installed } -> store.update(id, "TRANSFERRED")
                else -> store.update(id, "UNCERTAIN", "transfer_not_confirmed")
            }
            Result.success()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (store.get(id)?.state in WorkoutJobStore.TERMINAL) Result.success()
            else if (runAttemptCount < 4) Result.retry()
            else { store.update(id, "UNCERTAIN", e.message.orEmpty().take(256)); Result.success() }
        } finally {
            withContext(kotlinx.coroutines.NonCancellable + Dispatchers.Main) { InstallManager.close(session) }
        }
        } } finally {
            inputData.getString("id")?.let { id ->
                runCatching {
                    val job = WorkoutJobStore(File(applicationContext.filesDir, "companion-workouts")).get(id) ?: return@runCatching
                    if (WorkoutAccess(applicationContext).ownerAuthorized(job.owner, job.device)) {
                        applicationContext.sendBroadcast(android.content.Intent("nodomain.freeyourgadget.gadgetbridge.action.WORKOUT_JOB_CHANGED")
                            .setPackage(job.owner.substringBefore('|')).putExtra("requestId", job.request).putExtra("device", job.device).putExtra("state", job.state))
                    }
                }
            }
        }
    }

    companion object {
        // Device sessions are shared with the interactive installer. Avoid competing companion workers.
        private val MUTEX = Mutex()
        @JvmStatic fun enqueue(context: Context, id: String) {
            WorkoutSyncDispatcher.register(context)
            val request = OneTimeWorkRequestBuilder<WorkoutSyncWorker>().setInputData(workDataOf("id" to id))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS).build()
            WorkManager.getInstance(context).enqueueUniqueWork("companion-workout-$id", ExistingWorkPolicy.KEEP, request)
        }
        @JvmStatic fun recover(context: Context) {
            val dir = File(context.filesDir, "companion-workouts")
            if (!dir.isDirectory) return
            val pending = WorkoutJobStore(dir).all().filter { it.state !in WorkoutJobStore.TERMINAL }
            if (pending.isEmpty()) WorkoutSyncDispatcher.idle(context)
            else pending.forEach { enqueue(context, it.id) }
        }
    }
}
