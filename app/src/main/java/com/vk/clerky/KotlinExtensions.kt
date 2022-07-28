package com.vk.clerky

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


fun Context.longToast(message: String?) {
    logThis("[TOAST]  $message", getCaller())
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.shortToast(message: String?) {
    logThis("[TOAST]  $message", getCaller())
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.longToast(message: String?) {
    logThis("[TOAST]  $message", getCaller())
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

fun Fragment.shortToast(message: String?) {
    logThis("[TOAST]  $message", getCaller())
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun View.longToast(message: String?) {
    logThis("[TOAST]  $message", getCaller())
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun View.shortToast(message: String?) {
    logThis("[TOAST]  $message", getCaller())
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun logThis(message: Any?, caller: String = getCaller(), TAG: String = "logThis") {
    // println("$TAG -------*--------> $message")
    val traceEnabled = false //set to false to disable trace and vice versa
    if (traceEnabled) {
        Log.d(TAG, "$caller-->$message")
    } else {
        Log.d(TAG, "---->  $message")
    }

}

fun Any?.toLog(TAG: String = "logThis", caller: String = getCaller()) {
    // println("$TAG -------*--------> $message")
    val traceEnabled = false
    if (traceEnabled) {
        Log.d(TAG, "$caller-->  $this")
    } else {
        Log.d(TAG, "---->  $this")
    }

}

fun Fragment.setStatusBarColor(color: Int) {
    requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    requireActivity().window.statusBarColor = color
}

fun createPartFromString(string: String): RequestBody {
    return string.toRequestBody(MultipartBody.FORM)
}

fun checkStatus(online: String?): Boolean {
    return when (online) {
        "0" -> false
        "1" -> true
        else -> false
    }
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.inVisible() {
    this.visibility = View.INVISIBLE
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

@SuppressLint("ResourceType")
inline fun View.snack(
    @IntegerRes messageRes: Int,
    length: Int = Snackbar.LENGTH_LONG,
    f: Snackbar.() -> Unit
) {
    snack(resources.getString(messageRes), length, f)
}

inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
    val snack = Snackbar.make(this, message, length)
    snack.f()
    snack.show()
}

fun View.showSnack(message: String, length: Int = Snackbar.LENGTH_LONG) {
    val snack = Snackbar.make(this, message, length)
    snack.show()
}

fun View.appBackSnack(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    val snack = Snackbar.make(this, message, length)
    snack.setActionTextColor(Color.parseColor("#FFFFFF"))
    snack.view.setBackgroundColor(Color.parseColor("#0B3089"))
    snack.show()
}

fun View.errorSnack(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    val snack = Snackbar.make(this, message, length)
    snack.setActionTextColor(Color.parseColor("#FFFFFF"))
    snack.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
    snack.show()
}

@SuppressLint("ResourceType")
fun Snackbar.action(@IntegerRes actionRes: Int, color: Int? = null, listener: (View) -> Unit) {
    action(view.resources.getString(actionRes), color, listener)
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(color) }
}

inline fun View.errorSnackAction(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    f: Snackbar.() -> Unit
) {
    val snack = Snackbar.make(this, message, length)
    snack.f()
    snack.setActionTextColor(Color.parseColor("#FFFFFF"))
    snack.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
    snack.show()
}

private const val STACK_TRACE_LEVELS_UP = 5

fun getCaller(): String {  //to log the where this log is called from
    return "[line:${getLineNumber()} method: ${getMethodName()}() class: ${getClassName()}]"
}

private fun getLineNumber(): Int {
    return Thread.currentThread().stackTrace[STACK_TRACE_LEVELS_UP].lineNumber
}


private fun getClassName(): String {
    return Thread.currentThread().stackTrace[STACK_TRACE_LEVELS_UP].fileName
}


private fun getMethodName(): String {
    return Thread.currentThread().stackTrace[STACK_TRACE_LEVELS_UP].methodName
}

