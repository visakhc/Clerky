package com.vk.clerky

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {
    private var DRAW_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, DRAW_REQUEST_CODE)
        } else if (Settings.canDrawOverlays(this@MainActivity)) {
            logThis("canDrawOverlays")
            val intent = Intent(this@MainActivity, ForegroundService::class.java)
            startService(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DRAW_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                val intent = Intent(this, ForegroundService::class.java)
                startService(intent)
            }
        }
    }
    val cont = this@MainActivity
}