/* SPDX-License-Identifier: AGPL-3.0-or-later */
package nodomain.freeyourgadget.gadgetbridge.companion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.util.kotlin.getDevice
import java.io.File
import java.util.concurrent.TimeUnit

/** Reconnects and idle transitions wake queued jobs; periodic reconciliation covers lost events. */
object WorkoutSyncDispatcher {
    private var registered = false
    @Volatile private var pending = false
    private val scope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.IO)
    @Synchronized fun register(context: Context) {
        pending = true
        val app = context.applicationContext
        if (!registered) {
            registered = true
            LocalBroadcastManager.getInstance(app).registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (!pending) return
                    val device = intent.getDevice() ?: return
                    if (!device.isInitialized || device.isBusy) return
                    scope.launch {
                        val store = WorkoutJobStore(File(app.filesDir, "companion-workouts"))
                        store.all().filter { it.device.equals(device.address, true) && it.state == "QUEUED" }
                            .forEach { WorkoutSyncWorker.enqueue(app, it.id) }
                    }
                }
            }, IntentFilter(GBDevice.ACTION_DEVICE_CHANGED))
        }
        WorkManager.getInstance(app).enqueueUniquePeriodicWork("companion-workout-reconcile", ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WorkoutSyncRecoveryWorker>(15, TimeUnit.MINUTES).build())
    }
    @Synchronized fun idle(context: Context) {
        val dir = File(context.filesDir, "companion-workouts")
        if (dir.isDirectory && WorkoutJobStore(dir).all().any { it.state !in WorkoutJobStore.TERMINAL }) {
            register(context)
            return
        }
        pending = false
        WorkManager.getInstance(context).cancelUniqueWork("companion-workout-reconcile")
    }
}

class WorkoutSyncRecoveryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        WorkoutSyncWorker.recover(applicationContext)
        return Result.success()
    }
}
