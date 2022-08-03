package com.vk.clerky

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnTouchListener
import androidx.core.app.NotificationCompat
import com.vk.clerky.databinding.FloatingLayoutBinding


class ForegroundService : Service() {
    private var flashStatus: Boolean = false
    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var windowManager: WindowManager? = null
    private var binding: FloatingLayoutBinding? = null


    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground()
        else startForeground(1, Notification())

        setUpFloatView()
        handleFloatingView()
        handleEvents()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleFloatingView() {
        binding?.root?.setOnTouchListener(object : OnTouchListener {
            val floatWindowLayoutUpdateParam = floatWindowLayoutParam
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = floatWindowLayoutUpdateParam!!.x.toDouble()
                        y = floatWindowLayoutUpdateParam.y.toDouble()
                        px = event.rawX.toDouble()
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        floatWindowLayoutUpdateParam!!.x = (x + event.rawX - px).toInt()
                        floatWindowLayoutUpdateParam.y = (y + event.rawY - py).toInt()
                        windowManager?.updateViewLayout(binding?.root, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })
    }

    private fun handleEvents() {

        binding?.btTorch?.setOnClickListener {
            turnOnFlash()
        }

    }

    private fun setUpFloatView() {
        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup

        binding = FloatingLayoutBinding.inflate(LayoutInflater.from(this))

        // maximizeBtn = floatView?.findViewById(R.id.buttonMaximize)
        // descEditArea = floatView?.findViewById(R.id.descEditText)
        // saveBtn = floatView?.findViewById(R.id.saveBtn)


        LAYOUT_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_TOAST
        }
        floatWindowLayoutParam = WindowManager.LayoutParams(
/*(width * 0.25f).toInt(), (height * 0.58f).toInt(),*/
            /*   LinearLayout.LayoutParams.WRAP_CONTENT,
               LinearLayout.LayoutParams.WRAP_CONTENT,*/
            700, 700,
            LAYOUT_TYPE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        floatWindowLayoutParam?.gravity = Gravity.CENTER
        floatWindowLayoutParam?.x = 0
        floatWindowLayoutParam?.y = 0

        windowManager?.addView(binding?.root, floatWindowLayoutParam)
    }

    private fun turnOnFlash() {
        val isFlashAvailable =
            applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)

        if (!isFlashAvailable) {
            shortToast("Flash is not available")
            return
        }

        val mCameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val mCameraId = mCameraManager.cameraIdList[0]
            flashStatus = if (flashStatus) {
                mCameraManager.setTorchMode(mCameraId, false)
                false
            } else {
                mCameraManager.setTorchMode(mCameraId, true)
                true
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        windowManager?.removeView(binding?.root)

        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, MainActivity::class.java)
        this.sendBroadcast(broadcastIntent)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

}