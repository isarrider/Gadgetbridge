package nodomain.freeyourgadget.gadgetbridge.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.color.MaterialColors
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.adapter.DeviceStatusDot
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceManager
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.util.kotlin.getParcelableCompat

/** The dropdown under the toolbar's device action, listing every paired device. */
object DeviceStatusMenu {
    private const val MAX_VISIBLE_ROWS = 5.5f

    interface Callbacks {
        fun onDeviceSelected(device: GBDevice)
        fun onConnect(device: GBDevice)
        fun onSync(device: GBDevice)
        fun onManageDevices()
    }

    @JvmStatic
    fun show(anchor: View, callbacks: Callbacks) {
        val context = anchor.context
        val density = context.resources.displayMetrics.density
        val inflater = LayoutInflater.from(context)
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, (6 * density).toInt(), 0, (6 * density).toInt())
            background = AppCompatResources.getDrawable(context, R.drawable.bg_device_status_menu)
        }
        val popup = PopupWindow(content, (288 * density).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = 8 * density
        }

        val rows = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        content.addView(object : ScrollView(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                if (rows.childCount <= MAX_VISIBLE_ROWS) return
                val cap = (rows.getChildAt(0).measuredHeight * MAX_VISIBLE_ROWS).toInt()
                if (measuredHeight > cap) setMeasuredDimension(measuredWidth, cap)
            }
        }.apply { addView(rows) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        fun fill(changed: GBDevice? = null) {
            rows.removeAllViews()
            GBApplication.app().deviceManager.devices
                .map { if (changed != null && it.address == changed.address) changed else it }
                .forEach { rows.addView(row(inflater, rows, it, popup, callbacks)) }
        }
        fill()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) =
                fill(intent.getParcelableCompat<GBDevice>(GBDevice.EXTRA_DEVICE))
        }
        val broadcasts = LocalBroadcastManager.getInstance(context)
        broadcasts.registerReceiver(receiver, IntentFilter().apply {
            addAction(GBDevice.ACTION_DEVICE_CHANGED)
            addAction(DeviceManager.ACTION_DEVICES_CHANGED)
        })
        popup.setOnDismissListener { broadcasts.unregisterReceiver(receiver) }

        content.addView(View(context).apply {
            setBackgroundColor(MaterialColors.getColor(this, R.attr.row_separator))
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, density.toInt().coerceAtLeast(1)).apply {
            topMargin = (6 * density).toInt()
            bottomMargin = (6 * density).toInt()
        })
        content.addView(action(inflater, content, R.string.device_menu_manage) {
            popup.dismiss()
            callbacks.onManageDevices()
        })

        popup.showAsDropDown(anchor, 0, 0, Gravity.END)
    }

    private fun row(inflater: LayoutInflater, parent: ViewGroup, device: GBDevice, popup: PopupWindow, callbacks: Callbacks): View {
        val context = parent.context
        val row = inflater.inflate(R.layout.item_device_status_row, parent, false)
        row.findViewById<ImageView>(R.id.device_status_icon).setImageResource(device.deviceCoordinator.defaultIconResource)
        DeviceStatusDot.apply(row.findViewById(R.id.device_status_dot), device)
        row.findViewById<TextView>(R.id.device_status_name).text = device.aliasOrName
        row.findViewById<TextView>(R.id.device_status_state).text =
            if (device.isBusy) device.busyTask else device.getStateString(context)
        val battery = row.findViewById<TextView>(R.id.device_status_battery)
        val level = device.getBatteryLevel(0)
        battery.visibility = if (device.isInitialized && level >= 0) View.VISIBLE else View.GONE
        battery.text = context.getString(R.string.battery_percentage_str, level.toString())
        val sync = row.findViewById<View>(R.id.device_status_sync)
        sync.visibility = if (device.isInitialized && !device.isBusy) View.VISIBLE else View.GONE
        sync.setOnClickListener { callbacks.onSync(device) }
        row.setOnClickListener {
            when {
                device.isInitialized -> if (!device.isBusy) callbacks.onSync(device)
                device.isConnecting -> {
                    popup.dismiss()
                    callbacks.onDeviceSelected(device)
                }
                else -> callbacks.onConnect(device)
            }
        }
        return row
    }

    private fun action(inflater: LayoutInflater, parent: ViewGroup, @StringRes text: Int, onClick: () -> Unit): View =
        (inflater.inflate(R.layout.item_device_status_action, parent, false) as TextView).apply {
            setText(text)
            setOnClickListener { onClick() }
        }
}
