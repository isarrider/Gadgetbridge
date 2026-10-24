package nodomain.freeyourgadget.gadgetbridge.activities

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.color.MaterialColors
import nodomain.freeyourgadget.gadgetbridge.GBApplication
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.adapter.DeviceStatusDot
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice

/** The dropdown under the toolbar's device action, listing every paired device. */
object DeviceStatusMenu {
    interface Callbacks {
        fun onDeviceSelected(device: GBDevice)
        fun onRetry(device: GBDevice)
        fun onManageDevices()
    }

    @JvmStatic
    fun show(anchor: View, devices: List<GBDevice>, callbacks: Callbacks) {
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

        devices.sortedWith(compareBy<GBDevice> { DeviceStatusDot.rank(it) }.thenBy { it.aliasOrName.lowercase() })
            .forEach { device ->
                val row = inflater.inflate(R.layout.item_device_status_row, content, false)
                row.findViewById<ImageView>(R.id.device_status_icon).setImageResource(device.deviceCoordinator.defaultIconResource)
                DeviceStatusDot.apply(row.findViewById(R.id.device_status_dot), device)
                row.findViewById<TextView>(R.id.device_status_name).text = device.aliasOrName
                row.findViewById<TextView>(R.id.device_status_state).text = device.getStateString(context)
                val battery = row.findViewById<TextView>(R.id.device_status_battery)
                val level = device.getBatteryLevel(0)
                battery.visibility = if (device.isInitialized && level >= 0) View.VISIBLE else View.GONE
                battery.text = context.getString(R.string.battery_percentage_str, level.toString())
                val retry = row.findViewById<View>(R.id.device_status_retry)
                retry.visibility = if (device.isInitialized || device.isConnecting) View.GONE else View.VISIBLE
                retry.setOnClickListener {
                    popup.dismiss()
                    callbacks.onRetry(device)
                }
                row.setOnClickListener {
                    popup.dismiss()
                    callbacks.onDeviceSelected(device)
                }
                content.addView(row)
            }

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

    private fun action(inflater: LayoutInflater, parent: ViewGroup, @StringRes text: Int, onClick: () -> Unit): View =
        (inflater.inflate(R.layout.item_device_status_action, parent, false) as TextView).apply {
            setText(text)
            setOnClickListener { onClick() }
        }
}
