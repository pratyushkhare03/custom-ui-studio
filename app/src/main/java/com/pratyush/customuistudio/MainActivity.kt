
package com.pratyush.customuistudio

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(19, 17, 28))
        }

        val title = TextView(this).apply {
            text = "CUSTOM UI STUDIO"
            textSize = 27f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Make your phone feel like yours."
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        }

        val enable = Button(this).apply {
            text = "Enable Floating Sticker"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startService(
                        Intent(
                            this@MainActivity,
                            StickerOverlayService::class.java
                        )
                    )
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, 1001)
                }
            }
        }

        val disable = Button(this).apply {
            text = "Remove Sticker"
            setOnClickListener {
                stopService(
                    Intent(
                        this@MainActivity,
                        StickerOverlayService::class.java
                    )
                )
            }
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(enable)
        root.addView(disable)

        setContentView(root)
    }

    @Deprecated("Used for the overlay permission return")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            requestCode == 1001 &&
            Settings.canDrawOverlays(this)
        ) {
            startService(
                Intent(this, StickerOverlayService::class.java)
            )
        }
    }
}
