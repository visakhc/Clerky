package com.vk.clerky

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.IOException
import kotlin.math.abs


class ForegroundService : Service(), View.OnTouchListener {
    private var windowManager: WindowManager? = null
    private var button: ImageView? = null
    private var view: ConstraintLayout? = null
    private var params: WindowManager.LayoutParams? = null
    private val handler = Handler()
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var downTapTime: Long = 0

    private var longPressed = Runnable {
        try {
            //disable the button before taking screenshot
            params?.alpha = 0f
            windowManager?.updateViewLayout(button, params)

            Thread.sleep(1000)
            params?.alpha = 1.0f
            windowManager?.updateViewLayout(button, params)
        } catch (e: IOException) {
            logThis("[ERROR] " + e.stackTraceToString())
        } catch (e: InterruptedException) {
            logThis("[ERROR] " + e.stackTraceToString())
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null
    }

    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var maximizeBtn: Button? = null
    private var descEditArea: EditText? = null
    private var saveBtn: Button? = null
    override fun onCreate() {
        super.onCreate()
        // The screen height and width are calculated, cause
        // the height and width of the floating window is set depending on this
        // The screen height and width are calculated, cause
        // the height and width of the floating window is set depending on this
        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        // To obtain a WindowManager of a different Display,
        // we need a Context for that display, so WINDOW_SERVICE is used

        // To obtain a WindowManager of a different Display,
        // we need a Context for that display, so WINDOW_SERVICE is used
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // A LayoutInflater instance is created to retrieve the
        // LayoutInflater for the floating_layout xml

        // A LayoutInflater instance is created to retrieve the
        // LayoutInflater for the floating_layout xml
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // inflate a new view hierarchy from the floating_layout xml

        // inflate a new view hierarchy from the floating_layout xml
        floatView = inflater.inflate(R.layout.clerky_layout, null) as ViewGroup

        // The Buttons and the EditText are connected with
        // the corresponding component id used in floating_layout xml file

        // The Buttons and the EditText are connected with
        // the corresponding component id used in floating_layout xml file

        // Just like MainActivity, the text written
        // in Maximized will stay

        // Just like MainActivity, the text written
        // in Maximized will stay

        // WindowManager.LayoutParams takes a lot of parameters to set the
        // the parameters of the layout. One of them is Layout_type.

        // WindowManager.LayoutParams takes a lot of parameters to set the
        // the parameters of the layout. One of them is Layout_type.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // If API Level is more than 26, we need TYPE_APPLICATION_OVERLAY
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // If API Level is lesser than 26, then we can
            // use TYPE_SYSTEM_ERROR,
            // TYPE_SYSTEM_OVERLAY, TYPE_PHONE, TYPE_PRIORITY_PHONE.
            // But these are all
            // deprecated in API 26 and later. Here TYPE_TOAST works best.
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST
        }

        // Now the Parameter of the floating-window layout is set.
        // 1) The Width of the window will be 55% of the phone width.
        // 2) The Height of the window will be 58% of the phone height.
        // 3) Layout_Type is already set.
        // 4) Next Parameter is Window_Flag. Here FLAG_NOT_FOCUSABLE is used. But
        // problem with this flag is key inputs can't be given to the EditText.
        // This problem is solved later.
        // 5) Next parameter is Layout_Format. System chooses a format that supports
        // translucency by PixelFormat.TRANSLUCENT

        // Now the Parameter of the floating-window layout is set.
        // 1) The Width of the window will be 55% of the phone width.
        // 2) The Height of the window will be 58% of the phone height.
        // 3) Layout_Type is already set.
        // 4) Next Parameter is Window_Flag. Here FLAG_NOT_FOCUSABLE is used. But
        // problem with this flag is key inputs can't be given to the EditText.
        // This problem is solved later.
        // 5) Next parameter is Layout_Format. System chooses a format that supports
        // translucency by PixelFormat.TRANSLUCENT
        floatWindowLayoutParam = WindowManager.LayoutParams(
            (width * 0.55f).toInt(), (height * 0.58f).toInt(),
            LAYOUT_TYPE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // The Gravity of the Floating Window is set.
        // The Window will appear in the center of the screen

        // The Gravity of the Floating Window is set.
        // The Window will appear in the center of the screen
        floatWindowLayoutParam!!.gravity = Gravity.CENTER

        // X and Y value of the window is set

        // X and Y value of the window is set
        floatWindowLayoutParam!!.x = 0
        floatWindowLayoutParam!!.y = 0

        // The ViewGroup that inflates the floating_layout.xml is
        // added to the WindowManager with all the parameters

        // The ViewGroup that inflates the floating_layout.xml is
        // added to the WindowManager with all the parameters
        windowManager.addView(floatView, floatWindowLayoutParam)
/*
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        button = ImageView(this)
        button?.setImageResource(R.drawable.button)
        params = WindowManager.LayoutParams(
            100, 100,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        // params.gravity= Gravity.CENTER_VERTICAL|Gravity.END;
        val layoutInflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = layoutInflater.inflate(R.layout.clerky_layout, null);
        val pwindo = PopupWindow(
            popupView,
           200,
200        );
        windowManager?.addView(button, params)
        button?.setOnClickListener {
            shortToast("THISS")
        //    windowManager?.addView(popupView, params)
            initiatePopupWindow(it)

            //  pwindo.showAsDropDown(it, 50, -30)
        }
        // button?.setOnTouchListener(this)*/
    }

    private fun initiatePopupWindow(anchor: View) {
        try {
            val popup = PopupWindow(this)
            popup.contentView = LayoutInflater.from(this).inflate(R.layout.clerky_layout, null)
            popup.width = 500
            popup.height = 500
            popup.showAsDropDown(anchor)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        if (button != null) windowManager?.removeView(button)
        super.onDestroy()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val curTime = System.currentTimeMillis()
        logThis("onTouch:  $event")

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                //remember the initial position.
                initialX = params?.x!!
                initialY = params?.y!!

                //get the touch location
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                downTapTime = System.currentTimeMillis()

                //long click detector
                handler.postDelayed(longPressed, ViewConfiguration.getLongPressTimeout().toLong())
                return true
            }
            MotionEvent.ACTION_UP -> {
                //delete the long click listener if it hasn't been called yet
                handler.removeCallbacks(longPressed)

                //As we implemented on touch listener with ACTION_MOVE,
                //we have to check if the previous action was ACTION_DOWN
                //to identify if the user clicked the view or not.

                //down followed by up = a click
                val timeDiff = curTime - downTapTime
                if (timeDiff <= 150) {
                    try {
                        shortToast("up...up")
                    } catch (e: IOException) {
                        logThis("[ERROR] Shell command failed  " + e.stackTraceToString())
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                shortToast("ACTION_MOVE")
                if (abs(event.rawY - initialTouchY) > 100 && abs(event.rawX - initialTouchX) > 100) handler.removeCallbacks(
                    longPressed
                )
                //Calculate the X and Y coordinates of the view.
                params?.x = initialX + (event.rawX - initialTouchX).toInt()
                params?.y = initialY + (event.rawY - initialTouchY).toInt()

                //Update the layout with new X & Y coordinate
                windowManager?.updateViewLayout(button, params)
                return true
            }
        }
        return false
    }

}