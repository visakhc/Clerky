package com.vk.clerky

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    private var minimizeBtn: Button? = null
    private var dialog: AlertDialog? = null
    private var descEditArea: EditText? = null
    private var save: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        minimizeBtn = findViewById(R.id.buttonMinimize)
        descEditArea = findViewById(R.id.descEditText)
        save = findViewById(R.id.saveBtn)

        if (isMyServiceRunning()) {
            stopService(Intent(this@MainActivity, ForegroundService::class.java))
        }
        if (checkOverlayDisplayPermission()) {
            startService(Intent(this@MainActivity, ForegroundService::class.java))
            finish()
        } else {
            requestOverlayDisplayPermission()
        }
        minimizeBtn?.setOnClickListener {
            if (checkOverlayDisplayPermission()) {
                startService(Intent(this@MainActivity, ForegroundService::class.java))
                finish()
            } else {
                requestOverlayDisplayPermission()
            }
        }

    }

    private fun isMyServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (ForegroundService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkOverlayDisplayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayDisplayPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("Screen Overlay Permission Needed")
        builder.setMessage("Enable 'Display over other apps' from System Settings.")
        builder.setPositiveButton("Open Settings") { _, _ ->
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, RESULT_OK)
        }
        dialog = builder.create()
        dialog?.show()
    }

}