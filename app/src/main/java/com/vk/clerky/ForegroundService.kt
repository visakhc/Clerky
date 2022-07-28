package com.vk.clerky

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ForegroundService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}