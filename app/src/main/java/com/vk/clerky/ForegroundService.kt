package com.vk.clerky

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import java.io.IOException
import kotlin.math.abs

class ForegroundService : Service(), View.OnTouchListener {
    private var windowManager: WindowManager? = null
    private var button: ImageView? = null
    private var params: WindowManager.LayoutParams? = null
    private var process: Process? = null
    private val handler = Handler()
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var downTapTime: Long = 0

    private var longPressed = Runnable {
        try {
            //disable the button before taking screenshot
            params!!.alpha = 0f
            windowManager!!.updateViewLayout(button, params)
            process!!.outputStream.write("""input keyevent ${KeyEvent.KEYCODE_POWER} """.toByteArray())
            process!!.outputStream.flush()
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

    override fun onCreate() {
        super.onCreate()
        try {
            process = Runtime.getRuntime().exec("su")
        } catch (e: IOException) {
            logThis("[ERROR] " + e.stackTraceToString())
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        button = ImageView(this)
        button?.setImageResource(R.drawable.button)
        params = WindowManager.LayoutParams(
            100, 100,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        //        params.gravity= Gravity.CENTER_VERTICAL|Gravity.END;
        windowManager?.addView(button, params)
        button?.setOnTouchListener(this)
    }

    override fun onDestroy() {
        if (button != null) windowManager!!.removeView(button)
        super.onDestroy()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val curTime = System.currentTimeMillis()
        logThis("onTouch:  $event")

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //remember the initial position.
                initialX = params!!.x
                initialY = params!!.y

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
                        process!!.outputStream.write(
                            """su -c input keyevent ${KeyEvent.KEYCODE_BACK}""".toByteArray()
                        )
                        process!!.outputStream.flush()
                    } catch (e: IOException) {
                        logThis("[ERROR] Shell command failed  " + e.stackTraceToString())

                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.rawY - initialTouchY) > 100 && abs(event.rawX - initialTouchX) > 100) handler.removeCallbacks(
                    longPressed
                )
                //Calculate the X and Y coordinates of the view.
                params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                params!!.y = initialY + (event.rawY - initialTouchY).toInt()

                //Update the layout with new X & Y coordinate
                windowManager!!.updateViewLayout(button, params)
                return true
            }
        }
        return false
    }

}