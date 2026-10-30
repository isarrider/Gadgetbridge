/* SPDX-License-Identifier: AGPL-3.0-or-later */
package nodomain.freeyourgadget.gadgetbridge.companion

import android.app.Activity
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity

/** A foreground, user-initiated grant. The app identity comes from Android's startActivityForResult. */
class WorkoutCompanionActivity : AbstractGBActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val access = WorkoutAccess(this)
        val pkg = callingPackage
        if (pkg == null) {
            val approved = access.approvals()
            val labels = approved.map { (app, device) -> "$app\n$device" }.toTypedArray()
            MaterialAlertDialogBuilder(this).setTitle(R.string.workout_companions_title)
                .setMessage(if (approved.isEmpty()) getString(R.string.workout_companions_none) else null)
                .setItems(labels) { _, index ->
                    val (app, device) = approved[index]
                    MaterialAlertDialogBuilder(this).setMessage(getString(R.string.workout_companions_revoke, app, device))
                        .setPositiveButton(R.string.ok) { _, _ -> access.revoke(app, device); finish() }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }.setOnCancelListener { finish() }.show()
                }.setNegativeButton(android.R.string.cancel) { _, _ -> finish() }.setOnCancelListener { finish() }.show()
            return
        }
        setResult(Activity.RESULT_CANCELED)
        val label = runCatching { packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString() }.getOrElse { finish(); return }
        val signer = runCatching { access.signer(pkg) }.getOrElse { finish(); return }
        val devices = GBApplication.app().deviceManager.devices.filter { it.deviceCoordinator.supportsWorkoutFileImport(it) }
        if (devices.isEmpty()) {
            MaterialAlertDialogBuilder(this).setMessage(R.string.workout_companions_no_device)
                .setPositiveButton(android.R.string.ok) { _, _ -> finish() }.setOnCancelListener { finish() }.show()
            return
        }
        var selected = 0
        val body = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val pad = (24 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, 0)
        }
        body.addView(android.widget.TextView(this).apply { text = getString(R.string.workout_companions_request, label, pkg) })
        val choices = android.widget.RadioGroup(this)
        devices.forEachIndexed { index, device ->
            choices.addView(android.widget.RadioButton(this).apply { id = index + 1; text = "${device.aliasOrName} (${device.address})"; filterTouchesWhenObscured = true })
        }
        choices.check(selected + 1)
        choices.setOnCheckedChangeListener { _, id -> selected = id - 1 }
        body.addView(choices)
        val dialog = MaterialAlertDialogBuilder(this).setTitle(R.string.workout_companions_title)
            .setView(android.widget.ScrollView(this).apply { addView(body) })
            .setPositiveButton(R.string.workout_companions_allow) { _, _ ->
                if (runCatching { access.signer(pkg) == signer }.getOrDefault(false)) { access.approve(pkg, devices[selected].address); setResult(Activity.RESULT_OK) }
                finish()
            }.setNegativeButton(android.R.string.cancel) { _, _ -> finish() }.setOnCancelListener { finish() }.create()
        dialog.setOnShowListener { dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).filterTouchesWhenObscured = true }
        dialog.show()
    }
}
