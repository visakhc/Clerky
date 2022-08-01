package com.vk.clerky

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import com.vk.clerky.databinding.FloatingLayoutBinding


@RequiresApi(Build.VERSION_CODES.M)
class ForegroundService : Service() {
    private var flashStatus: Boolean = false

    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var windowManager: WindowManager? = null
    private var maximizeBtn: Button? = null
    private var descEditArea: EditText? = null
    private var saveBtn: Button? = null
    private var binding: FloatingLayoutBinding? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup

        binding = FloatingLayoutBinding.inflate(LayoutInflater.from(this))

        // maximizeBtn = floatView?.findViewById(R.id.buttonMaximize)
        //    descEditArea = floatView?.findViewById(R.id.descEditText)
        //  saveBtn = floatView?.findViewById(R.id.saveBtn)

        descEditArea?.setText(Common.currentDesc)
        descEditArea?.setSelection(descEditArea?.text.toString().length)
        descEditArea?.isCursorVisible = false


        LAYOUT_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_TOAST
        }
        floatWindowLayoutParam = WindowManager.LayoutParams(
            (width * 0.55f).toInt(), (height * 0.58f).toInt(),
            LAYOUT_TYPE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        floatWindowLayoutParam?.gravity = Gravity.RIGHT or Gravity.END
        floatWindowLayoutParam?.x = 0
        floatWindowLayoutParam?.y = 0

        windowManager?.addView(binding?.root, floatWindowLayoutParam)

        binding?.btTorch?.setOnClickListener {
            turnOnFlash()
        }

        maximizeBtn?.setOnClickListener {
            stopSelf()
            windowManager?.removeView(binding?.root)
            val backToHome = Intent(this@ForegroundService, MainActivity::class.java)
            backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(backToHome)
        }

        descEditArea?.doOnTextChanged { text, start, before, count ->
            Common.currentDesc = descEditArea?.text.toString()
        }


        floatView?.setOnTouchListener(object : OnTouchListener {
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
                        windowManager?.updateViewLayout(floatView, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })

        descEditArea?.setOnTouchListener { _, _ ->
            descEditArea?.isCursorVisible = true
            val floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam
            floatWindowLayoutParamUpdateFlag?.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            windowManager?.updateViewLayout(binding?.root, floatWindowLayoutParamUpdateFlag)
            false
        }

        saveBtn?.setOnClickListener {
            Common.savedDesc = descEditArea?.text.toString()
            descEditArea?.isCursorVisible = false
            val floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam
            floatWindowLayoutParamUpdateFlag?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager?.updateViewLayout(binding?.root, floatWindowLayoutParamUpdateFlag)
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding?.root?.applicationWindowToken, 0)
            Toast.makeText(this@ForegroundService, "Text Saved?!", Toast.LENGTH_SHORT).show()
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
        windowManager?.removeView(binding?.root)
    }
}