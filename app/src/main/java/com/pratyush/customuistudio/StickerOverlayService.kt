
package com.pratyush.customuistudio

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import android.view.View

class StickerOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var sticker: TextView
    private lateinit var params: WindowManager.LayoutParams

    private var receiverRegistered = false

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            if (intent?.action != Intent.ACTION_BATTERY_CHANGED) {
                return
            }

            val level = intent.getIntExtra("level", -1)
            val scale = intent.getIntExtra("scale", 100)

            if (level >= 0 && scale > 0) {
                val percent = level * 100 / scale
                val face = when {
                    percent <= 15 -> "😿"
                    percent >= 90 -> "😺"
                    else -> "🐱"
                }

                sticker.text = "$face $percent%"
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(
            WINDOW_SERVICE
        ) as WindowManager
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!::sticker.isInitialized) {
            createSticker()
        }

        return START_STICKY
    }

    private fun createSticker() {
        sticker = TextView(this).apply {
            text = "🐱 --%"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(
                dp(12), dp(8), dp(12), dp(8)
            )

            background = GradientDrawable().apply {
                setColor(Color.rgb(77, 42, 115))
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), Color.rgb(190, 140, 255))
            }
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = resources.displayMetrics.widthPixels - dp(125)
            y = dp(48)
        }

        sticker.setOnTouchListener(
            object : View.OnTouchListener {
                private var startX = 0
                private var startY = 0
                private var touchX = 0f
                private var touchY = 0f

                override fun onTouch(
                    view: View,
                    event: MotionEvent
                ): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = params.x
                            startY = params.y
                            touchX = event.rawX
                            touchY = event.rawY
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            params.x = startX +
                                (event.rawX - touchX).toInt()
                            params.y = startY +
                                (event.rawY - touchY).toInt()

                            try {
                                windowManager.updateViewLayout(
                                    sticker, params
                                )
                            } catch (_: Exception) {
                                return false
                            }

                            return true
                        }
                    }
                    return true
                }
            }
        )

        windowManager.addView(sticker, params)

        val filter = IntentFilter(
            Intent.ACTION_BATTERY_CHANGED
        )

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(
                batteryReceiver,
                filter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(batteryReceiver, filter)
        }

        receiverRegistered = true
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (_: Exception) {
                // Receiver already unregistered.
            }
            receiverRegistered = false
        }

        if (::sticker.isInitialized) {
            try {
                windowManager.removeView(sticker)
            } catch (_: Exception) {
                // View already removed.
            }
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
