/*  Copyright (C) 2026 Pedro

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.activities.soundpeats

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_EQ_DRAFT
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_EQ_MASTER
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SOUNDPEATS_EQ_PRESET
import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsCustomEqStore
import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsEqImport
import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsEqPresets
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.util.GB
import nodomain.freeyourgadget.gadgetbridge.util.kotlin.getParcelableCompat

/**
 * Graphical editor for a Soundpeats custom equalizer profile.
 *
 * The eight graphic bands are edited by dragging the points in [SoundpeatsEqualizerView]; the
 * profile can then be saved under a name and applied straight to the earbuds.
 */
class SoundpeatsEqEditorActivity : AbstractGBActivity() {
    private lateinit var device: GBDevice
    private lateinit var equalizerView: SoundpeatsEqualizerView
    private lateinit var masterSeekBar: SeekBar
    private lateinit var masterLabel: TextView
    private val bandSeekBars = mutableListOf<SeekBar>()
    private val bandValueLabels = mutableListOf<TextView>()
    private var syncingSliders = false

    private val bandKeys = SoundpeatsCustomEqStore.EDIT_BANDS.indices.map { SoundpeatsCustomEqStore.bandKey(it) }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importProfile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val parcelable = intent.getParcelableCompat<GBDevice>(GBDevice.EXTRA_DEVICE)
        if (parcelable == null) {
            finish()
            return
        }
        device = parcelable

        val density = resources.displayMetrics.density
        val padding = (16 * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }

        equalizerView = SoundpeatsEqualizerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setGains(readGains())
            onGainsChanged = { gains, released ->
                if (!syncingSliders) {
                    syncingSliders = true
                    gains.forEachIndexed { index, gain ->
                        bandSeekBars.getOrNull(index)?.progress =
                            (gain - SoundpeatsCustomEqStore.MIN_GAIN_DB).toInt()
                        bandValueLabels.getOrNull(index)?.text = formatDb(gain.toInt())
                    }
                    syncingSliders = false
                }
                writeGains(gains)
                if (released) {
                    applyDraft()
                }
            }
        }
        root.addView(equalizerView)

        // breathing room between the graph and the slider stack
        root.addView(
            android.view.View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (20 * density).toInt(),
                )
            }
        )

        val gains = readGains()
        SoundpeatsCustomEqStore.EDIT_BANDS.forEachIndexed { index, frequency ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = (44 * density).toInt()
                setPadding(0, (2 * density).toInt(), 0, (2 * density).toInt())
            }

            row.addView(
                TextView(this).apply {
                    text = getString(bandLabel(frequency))
                    width = (56 * density).toInt()
                }
            )

            val valueLabel = TextView(this).apply {
                text = formatDb(gains.getOrElse(index) { 0.0 }.toInt())
                gravity = Gravity.END
                width = (56 * density).toInt()
            }
            bandValueLabels.add(valueLabel)

            val seekBar = SeekBar(this).apply {
                max = (SoundpeatsCustomEqStore.MAX_GAIN_DB - SoundpeatsCustomEqStore.MIN_GAIN_DB).toInt()
                progress = (gains.getOrElse(index) { 0.0 } - SoundpeatsCustomEqStore.MIN_GAIN_DB).toInt()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (!fromUser || syncingSliders) {
                            return
                        }
                        val gain = SoundpeatsCustomEqStore.MIN_GAIN_DB + progress
                        valueLabel.text = formatDb(gain.toInt())
                        val updated = equalizerView.getGains().toMutableList()
                        updated[index] = gain
                        syncingSliders = true
                        equalizerView.setGains(updated)
                        syncingSliders = false
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        writeGains(equalizerView.getGains())
                        applyDraft()
                    }
                })
            }
            bandSeekBars.add(seekBar)
            row.addView(
                seekBar,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f),
            )
            row.addView(valueLabel)
            root.addView(row)
        }

        root.addView(
            TextView(this).apply {
                text = getString(R.string.soundpeats_eq_master_gain)
                setPadding(0, padding, 0, 0)
            }
        )

        masterLabel = TextView(this).apply {
            gravity = Gravity.END
            text = formatDb(readMaster())
        }
        root.addView(masterLabel)

        masterSeekBar = SeekBar(this).apply {
            max = (SoundpeatsCustomEqStore.MAX_MASTER_GAIN_DB - SoundpeatsCustomEqStore.MIN_MASTER_GAIN_DB)
            progress = (readMaster() - SoundpeatsCustomEqStore.MIN_MASTER_GAIN_DB).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    masterLabel.text = formatDb(masterFromProgress(progress))
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    writeMaster(masterFromProgress(seekBar?.progress ?: 0))
                    applyDraft()
                }
            })
        }
        root.addView(masterSeekBar)

        root.addView(
            MaterialButton(this).apply {
                text = getString(R.string.soundpeats_custom_eq_save)
                setOnClickListener { promptForName() }
            }
        )

        root.addView(
            MaterialButton(this).apply {
                text = getString(R.string.soundpeats_custom_eq_import)
                setOnClickListener { importLauncher.launch(arrayOf("text/plain", "application/octet-stream", "*/*")) }
            }
        )

        root.addView(
            MaterialButton(this).apply {
                text = getString(R.string.soundpeats_custom_eq_reset)
                setOnClickListener {
                    val flat = List(SoundpeatsCustomEqStore.EDIT_BANDS.size) { 0.0 }
                    equalizerView.setGains(flat)
                    syncingSliders = true
                    bandSeekBars.forEachIndexed { index, seekBar ->
                        seekBar.progress = (flat[index] - SoundpeatsCustomEqStore.MIN_GAIN_DB).toInt()
                        bandValueLabels.getOrNull(index)?.text = formatDb(flat[index].toInt())
                    }
                    syncingSliders = false
                    writeGains(flat)
                    applyDraft()
                }
            }
        )

        setContentView(android.widget.ScrollView(this).apply { addView(root) })
        supportActionBar?.setTitle(R.string.soundpeats_custom_eq)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun prefs() = GBApplication.getDeviceSpecificSharedPrefs(device.address)

    private fun readGains(): List<Double> =
        bandKeys.map { prefs().getInt(it, 0).toDouble() }

    private fun writeGains(gains: List<Double>) {
        val editor = prefs().edit()
        gains.forEachIndexed { index, gain ->
            editor.putInt(bandKeys[index], gain.toInt())
        }
        editor.apply()
    }

    private fun readMaster(): Int = prefs().getInt(PREF_SOUNDPEATS_EQ_MASTER, SoundpeatsEqPresets.MASTER_GAIN_DB.toInt())

    private fun writeMaster(value: Int) {
        prefs().edit().putInt(PREF_SOUNDPEATS_EQ_MASTER, value).apply()
    }

    private fun masterFromProgress(progress: Int): Int =
        SoundpeatsCustomEqStore.MIN_MASTER_GAIN_DB + progress

    private fun formatDb(value: Int): String = getString(R.string.soundpeats_db_value, value)

    /** Sends the current draft to the earbuds without saving it as a profile. */
    private fun applyDraft() {
        GBApplication.deviceService(device).onSendConfiguration(PREF_SOUNDPEATS_EQ_DRAFT)
    }

    private fun promptForName() {
        val input = EditText(this).apply { hint = getString(R.string.soundpeats_custom_eq_name) }

        AlertDialog.Builder(this)
            .setTitle(R.string.soundpeats_custom_eq_save)
            .setView(input)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = input.text.toString().trim().ifEmpty {
                    getString(R.string.soundpeats_custom_eq)
                }
                val profile = SoundpeatsCustomEqStore.save(
                    prefs = prefs(),
                    name = name,
                    masterGainDb = readMaster().toDouble(),
                    gains = equalizerView.getGains(),
                )
                prefs().edit()
                    .putString(PREF_SOUNDPEATS_EQ_PRESET, SoundpeatsCustomEqStore.customValue(profile.id))
                    .apply()
                GBApplication.deviceService(device).onSendConfiguration(PREF_SOUNDPEATS_EQ_PRESET)
                GB.toast(this, getString(R.string.soundpeats_custom_eq_saved, name), Toast.LENGTH_SHORT, GB.INFO)
            }
            .show()
    }

    /** Reads an AutoEQ style parametric profile and loads it into the editor. */
    private fun importProfile(uri: android.net.Uri) {
        val content = try {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (e: Exception) {
            null
        }

        val filters = content?.let { SoundpeatsEqImport.parse(it) }.orEmpty()
        if (filters.isEmpty()) {
            GB.toast(this, getString(R.string.soundpeats_custom_eq_import_failed), Toast.LENGTH_LONG, GB.ERROR)
            return
        }

        val gains = SoundpeatsEqImport.toBandGains(filters)
        equalizerView.setGains(gains)
        writeGains(gains)
        applyDraft()
        GB.toast(this, getString(R.string.soundpeats_custom_eq_import_done, filters.size), Toast.LENGTH_SHORT, GB.INFO)
    }

    private fun bandLabel(frequency: Int): Int = when (frequency) {
        62 -> R.string.soundpeats_eq_band_62
        125 -> R.string.soundpeats_eq_band_125
        250 -> R.string.soundpeats_eq_band_250
        500 -> R.string.soundpeats_eq_band_500
        1000 -> R.string.soundpeats_eq_band_1000
        2000 -> R.string.soundpeats_eq_band_2000
        4000 -> R.string.soundpeats_eq_band_4000
        else -> R.string.soundpeats_eq_band_8000
    }

    companion object {
        private const val UNUSED = 0
    }
}
