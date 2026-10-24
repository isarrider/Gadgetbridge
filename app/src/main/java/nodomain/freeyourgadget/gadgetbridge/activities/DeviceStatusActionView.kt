package nodomain.freeyourgadget.gadgetbridge.activities

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.color.MaterialColors
import nodomain.freeyourgadget.gadgetbridge.R
import nodomain.freeyourgadget.gadgetbridge.adapter.DeviceStatusDot
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator.DeviceKind
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice

/** The main toolbar's device action: the device icon with its status dot, plus a count badge for several devices. */
class DeviceStatusActionView(context: Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val iconColor = MaterialColors.getColor(this, R.attr.toolbar_icon)
    private val barColor = MaterialColors.getColor(this, R.attr.toolbar_bg)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics)
        color = barColor
    }
    private val badgeRect = RectF()

    private var icon: Drawable? = null
    private var dotColor = 0
    private var count = 0
    private var dotAlpha = 1f
    private var pulse: ValueAnimator? = null

    init {
        setBackgroundResource(R.drawable.bg_toolbar_icon)
        isClickable = true
        isLongClickable = true
    }

    fun bind(devices: List<GBDevice>) {
        if (devices.isEmpty()) return
        val connected = devices.filter { it.isInitialized }
        val single = devices.singleOrNull() ?: connected.singleOrNull()
        val shown = single ?: devices.maxBy { DeviceStatusDot.rank(it) }
        icon = AppCompatResources.getDrawable(context, if (single == null) R.drawable.ic_devices_wearables else glyph(single))
            ?.mutate()?.apply { setTint(iconColor) }
        dotColor = DeviceStatusDot.color(this, shown)
        count = if (single == null) connected.size else 1
        contentDescription = describe(devices)
        setPulsing(shown.isBusy)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (48 * density).toInt()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val half = resources.getDimension(R.dimen.toolbar_icon_size) / 2
        icon?.let {
            it.setBounds((width / 2f - half).toInt(), (height / 2f - half).toInt(), (width / 2f + half).toInt(), (height / 2f + half).toInt())
            it.draw(canvas)
        }

        val dotX = 34 * density
        val dotY = 19 * density
        paint.color = barColor
        canvas.drawCircle(dotX, dotY, 6 * density, paint)
        paint.color = dotColor
        paint.alpha = (Color.alpha(dotColor) * dotAlpha).toInt()
        canvas.drawCircle(dotX, dotY, 4 * density, paint)
        paint.alpha = 255

        if (count > 1) {
            val text = if (count > 9) context.getString(R.string.badge_overflow) else count.toString()
            val inner = maxOf(14 * density, textPaint.measureText(text) + 6 * density)
            val ring = 2 * density
            badgeRect.set(25 * density, 26 * density, 25 * density + inner + 2 * ring, 26 * density + 14 * density + 2 * ring)
            paint.color = barColor
            canvas.drawRoundRect(badgeRect, badgeRect.height() / 2, badgeRect.height() / 2, paint)
            badgeRect.inset(ring, ring)
            paint.color = iconColor
            canvas.drawRoundRect(badgeRect, badgeRect.height() / 2, badgeRect.height() / 2, paint)
            val baseline = badgeRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(text, badgeRect.centerX(), baseline, textPaint)
        }
    }

    override fun onDetachedFromWindow() {
        setPulsing(false)
        super.onDetachedFromWindow()
    }

    private fun setPulsing(pulsing: Boolean) {
        if (pulsing && animationsEnabled()) {
            if (pulse != null) return
            pulse = ValueAnimator.ofFloat(1f, 0.4f, 1f).apply {
                duration = 1200
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    dotAlpha = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            pulse?.cancel()
            pulse = null
            dotAlpha = 1f
        }
    }

    private fun animationsEnabled(): Boolean =
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f

    private fun describe(devices: List<GBDevice>): String {
        if (devices.size == 1) {
            val device = devices[0]
            val res = when {
                device.isBusy -> R.string.cd_device_syncing
                device.isInitialized -> R.string.cd_device_connected
                else -> R.string.cd_device_disconnected
            }
            return context.getString(res, device.aliasOrName)
        }
        val disconnected = devices.count { !it.isInitialized }
        return when {
            disconnected > 0 -> context.getString(R.string.cd_devices_some_disconnected, devices.size, disconnected)
            devices.any { it.isBusy } -> resources.getQuantityString(R.plurals.cd_devices_syncing, devices.size, devices.size)
            else -> resources.getQuantityString(R.plurals.cd_devices_all_connected, devices.size, devices.size)
        }
    }

    companion object {
        /** A plain glyph for the kind of [device]. */
        @JvmStatic
        @DrawableRes
        fun glyph(device: GBDevice): Int = when (device.deviceCoordinator.getDeviceKind(device)) {
            DeviceKind.WATCH, DeviceKind.FITNESS_BAND, DeviceKind.RING, DeviceKind.SMART_CLOCK -> R.drawable.ic_watch
            DeviceKind.CHEST_STRAP -> R.drawable.ic_ecg
            DeviceKind.SCALE -> R.drawable.ic_monitor_weight
            else -> R.drawable.ic_devices_other
        }
    }
}
