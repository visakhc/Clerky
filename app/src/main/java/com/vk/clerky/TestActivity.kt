package com.vk.clerky

import android.os.Bundle
import android.provider.Telephony
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        findViewById<Button>(R.id.button).setOnClickListener {
            getAllSms()
        }
        logThis("SIZE " + getAllSms().size)
    }

    private fun getAllSms(): List<String> {
        val lstSms: MutableList<String> = ArrayList()
        try {
            val cr = contentResolver
            val c = cr.query(
                Telephony.Sms.Inbox.CONTENT_URI, arrayOf(Telephony.Sms.Inbox.BODY),
                null, null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
            )!!
            // Default
            // sort
            // order);
            val totalSMS: Int = c.count
            logThis("totalSMS $totalSMS")

            if (c.moveToFirst()) {
                for (i in 0 until totalSMS) {
                    lstSms.add(c.getString(0))
                    c.moveToNext()
                }
            } else {
                throw RuntimeException("You have no SMS in Inbox")
            }
            c.close()
        } catch (e: Exception) {
            logThis("[ERROR]  ${e.stackTraceToString()}")
        }
        return lstSms
    }
}