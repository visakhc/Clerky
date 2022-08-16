package com.vk.clerky

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.PixelFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.view.*
import android.view.View.OnTouchListener
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.vk.clerky.adapter.ClipboardAdapter
import com.vk.clerky.databinding.FloatingLayoutBinding


class ForegroundService : Service(), ClipboardAdapter.OnClickListener,
    ClipboardManager.OnPrimaryClipChangedListener {
    lateinit var clipboard: ClipboardManager
    private var flashStatus: Boolean = false
    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var windowManager: WindowManager? = null
    private var binding: FloatingLayoutBinding? = null
    private var screenshotPath = ""

    private val clipboardAdapter by lazy { ClipboardAdapter(this) }
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground()
        else startForeground(1, Notification())
        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        setUpFloatView()
        handleFloatingView()
        handleEvents()
        clipDataToAdapter()
    }


    private fun clipDataToAdapter() {

        //  binding?.viewMain?.hide()

        val clip = clipboard.primaryClip
        clipboard.addPrimaryClipChangedListener(this)
        binding?.viewClipRecycler?.show()
        binding?.rvClipText?.apply {
            layoutManager = LinearLayoutManager(this@ForegroundService)
            adapter = clipboardAdapter
        }
        // longToast(clip?.label.toString())

        clipboardAdapter.updateList(clipboard.primaryClipDescription?.label.toString())
        //   binding?.etAddToClipboard?.setText(clip?.toString())
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

    private fun enableKeyboard() {
        val floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam!!

        if (floatWindowLayoutParamUpdateFlag.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE != 0) {
            floatWindowLayoutParamUpdateFlag.flags =
                floatWindowLayoutParamUpdateFlag.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        }
        windowManager!!.updateViewLayout(binding?.root, floatWindowLayoutParamUpdateFlag)

    }

    private fun disableKeyboard() {
        val floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam!!

        if (floatWindowLayoutParamUpdateFlag.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE == 0) {
            floatWindowLayoutParamUpdateFlag.flags =
                floatWindowLayoutParamUpdateFlag.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        windowManager!!.updateViewLayout(binding?.root, floatWindowLayoutParamUpdateFlag)

    }

    private fun handleEvents() {
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
/*
binding?.etAddToClipboard?.performClick()
*/
        binding?.btTorch?.setOnClickListener {
            turnOnFlash()
        }
        binding?.btClipboard?.setOnClickListener {
            //   binding?.etAddToClipboard?.show()
            val floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam!!
            //Layout Flag is changed to FLAG_NOT_TOUCH_MODAL which helps to take inputs inside floating window, but
            //while in EditText the back button won't work and FLAG_LAYOUT_IN_SCREEN flag helps to keep the window
            //always over the keyboard
            floatWindowLayoutParamUpdateFlag.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            //WindowManager is updated with the Updated Parameters
            windowManager!!.updateViewLayout(binding?.root, floatWindowLayoutParamUpdateFlag)
            // enableKeyboard()

        }
        binding?.btData?.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip

            binding?.etAddToClipboard?.setText(clip?.toString())
        }

        //Floating Window Layout Flag is set to FLAG_NOT_FOCUSABLE, so no input is possible to the EditText. But that's a problem.
        //So, the problem is solved here. The Layout Flag is changed when the EditText is touched.
        /*  binding?.etAddToClipboard?.setOnTouchListener(OnTouchListener { v, event ->
  *//*
            binding?.etAddToClipboard?.setCursorVisible(true)
*//*
            val floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam!!
            //Layout Flag is changed to FLAG_NOT_TOUCH_MODAL which helps to take inputs inside floating window, but
            //while in EditText the back button won't work and FLAG_LAYOUT_IN_SCREEN flag helps to keep the window
            //always over the keyboard
            floatWindowLayoutParamUpdateFlag.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            //WindowManager is updated with the Updated Parameters
            windowManager!!.updateViewLayout(binding?.root, floatWindowLayoutParamUpdateFlag)
            false
        })*/
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


        LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        floatWindowLayoutParam = WindowManager.LayoutParams(
/*(width * 0.25f).toInt(), (height * 0.58f).toInt(),*/
            /*   LinearLayout.LayoutParams.WRAP_CONTENT,
               LinearLayout.LayoutParams.WRAP_CONTENT,*/
            700, 700,
            LAYOUT_TYPE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        floatWindowLayoutParam?.gravity = Gravity.CENTER
        floatWindowLayoutParam?.x = 0
        floatWindowLayoutParam?.y = 0

        windowManager?.addView(binding?.root, floatWindowLayoutParam)
        // windowManager?.addView(floatView, floatWindowLayoutParam)
    }

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                /*    queryRelativeDataColumn(uri!!)*/queryDataColumn(uri!!)
            } else {
                queryDataColumn(uri!!)
            }
        }
    }

    private fun queryDataColumn(uri: Uri) {
        val projection = arrayOf(
            MediaStore.Images.Media.DATA
        )
        contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                if (path.contains("screenshot", true) and (screenshotPath != path)) {
                    screenshotPath = path
                    binding?.btData?.load(screenshotPath)
                    logThis("[queryDataColumn] Took Screenshot  $path")
                }
            }
        }
    }

    private fun queryRelativeDataColumn(uri: Uri) {
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val relativePathColumn =
                cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val displayNameColumn =
                cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val name = cursor.getString(displayNameColumn)
                val relativePath = cursor.getString(relativePathColumn)
                if (name.contains("screenshot", true) or
                    relativePath.contains("screenshot", true) and (screenshotPath != name)
                ) {
                    screenshotPath = relativePath
                    logThis("[queryRelativeDataColumn] Took Screenshot $name $relativePath")
                }
            }
        }
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
        /**/contentResolver.unregisterContentObserver(contentObserver)
        val clipboard: ClipboardManager =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        windowManager?.removeView(binding?.root)
        clipboard.removePrimaryClipChangedListener(this)
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

    override fun onItemClick(text: String) {
        shortToast(text)
    }

    override fun onPrimaryClipChanged() {
        clipboardAdapter.updateList(clipboard.primaryClipDescription?.label.toString())
    }

}