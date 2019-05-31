package de.simplestatswidget

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.CallLog
import android.view.View
import android.widget.RemoteViews

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class UpdateWidgetService : Service() {

    private val smsCount: Int
        get() {
            // get settings
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            var smsCharCountString = prefs.getString("smsCharCount", "160")
            if (smsCharCountString == null) {
                smsCharCountString = "160"
            }
            val smsCharCount = Integer.parseInt(smsCharCountString)
            val calendar = GregorianCalendar()
            // set a date for this month
            val thisMonth = GregorianCalendar(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), 1).time
            // get count of sent sms for this month
            val sentMessage = Uri.parse("content://sms/sent/")
            val cr = this.contentResolver
            var smscount = 0
            val c = cr.query(sentMessage, null, null, null, null)
            if (c != null) {
                while (c.moveToNext()) {
                    val dateString = c.getString(c.getColumnIndexOrThrow("date"))
                    val date = Date(java.lang.Long.valueOf(dateString))
                    if (date.after(thisMonth)) {
                        val message = c.getString(c.getColumnIndexOrThrow("body"))
                        smscount += Math.round(message.length.toDouble() / smsCharCount + 0.5).toInt()
                    }
                }
                c.close()
            }
            // count sms reverse if checked in preferences
            val countReverse = prefs.getBoolean("countReverse", false)
            if (countReverse) {
                var smsMaxString = prefs.getString("smsMax", "0")
                if (smsMaxString == null) {
                    smsMaxString = "0"
                }
                val smsMax = Integer.parseInt(smsMaxString)
                smscount = smsMax - smscount
            }

            return smscount
        }

    private val calls: String
        get() {
            // get settings
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val round = prefs.getBoolean("round", false)
            val calendar = GregorianCalendar()
            // set a date for this month
            val thisMonth = GregorianCalendar(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), 1).time
            // get duration of outgoing calls for this month
            val allCalls = Uri.parse("content://call_log/calls")
            val cr = this.contentResolver
            var callduration = 0
            val c = cr.query(allCalls, null, "type = " + CallLog.Calls.OUTGOING_TYPE, null, null)
            if (c != null) {
                while (c.moveToNext()) {
                    val dateString = c.getString(c.getColumnIndexOrThrow("date"))
                    val date = Date(java.lang.Long.valueOf(dateString))
                    if (date.after(thisMonth)) {
                        val duration = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("duration")))
                        if (duration > 0) {
                            callduration += if (round) {
                                Math.round(duration.toDouble() / 60 + 0.5).toInt() * 60
                            } else {
                                duration
                            }
                        }
                    }
                }
                c.close()
            }
            // get duration of outgoing calls for this month
            val countReverse = prefs.getBoolean("countReverse", false)
            var negative = false
            if (countReverse) {
                var callMaxString = prefs.getString("callMax", "0")
                if (callMaxString == null) {
                    callMaxString = "0"
                }
                val callMax = Integer.parseInt(callMaxString)
                callduration = callMax * 60 - callduration
                if (callduration < 0) {
                    negative = true
                    callduration *= -1
                }
            }
            // Set the text
            val minutes = callduration / 60
            val seconds = callduration % 60
            var minutesString = minutes.toString()
            var secondsString = seconds.toString()
            if (minutes < 10) {
                minutesString = "0$minutesString"
            }
            if (seconds < 10) {
                secondsString = "0$secondsString"
            }
            if (negative) {
                minutesString = "- $minutesString"
            }

            return "$minutesString:$secondsString"
        }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // check permissions and get data
        val smsText: String?
        val callText: String?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                smsText = smsCount.toString()
                callText = calls
            } else {
                smsText = null
                callText = null
            }
        } else {
            smsText = smsCount.toString()
            callText = calls
        }

        // update widgets with new data
        updateWidgets(intent, smsText, callText)
        stopSelf()

        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun updateWidgets(intent: Intent, smsText: String?, callText: String?) {
        // get ids
        val appWidgetManager = AppWidgetManager.getInstance(this
                .applicationContext)
        val allWidgetIds = intent
                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)

        // update all widgets
        for (widgetId in allWidgetIds) {
            val remoteViews = RemoteViews(this
                    .applicationContext.packageName,
                    R.layout.widget_layout)
            // set data
            if (smsText == null && callText == null) {
                remoteViews.setViewVisibility(R.id.widget_sms_header, View.GONE)
                remoteViews.setTextViewText(R.id.sms_text, "missing")
                remoteViews.setViewVisibility(R.id.widget_calls_header, View.GONE)
                remoteViews.setTextViewText(R.id.calls_text, "permissions")
            } else {
                if (smsText != null) {
                    // set text color
                    if (smsText.startsWith("-")) {
                        remoteViews.setTextColor(R.id.sms_text, Color.RED)
                    } else {
                        remoteViews.setTextColor(R.id.sms_text, Color.WHITE)
                    }
                    remoteViews.setTextViewText(R.id.sms_text, smsText)
                }
                if (callText != null) {
                    // set text color
                    if (callText.startsWith("- ")) {
                        remoteViews.setTextColor(R.id.calls_text, Color.RED)
                    } else {
                        remoteViews.setTextColor(R.id.calls_text, Color.WHITE)
                    }
                    remoteViews.setTextViewText(R.id.calls_text, callText)
                }
            }

            // register an onClickListener
            val clickIntent = Intent(this.applicationContext,
                    SimpleStatsWidgetProvider::class.java)

            clickIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    allWidgetIds)

            val pendingIntent = PendingIntent.getBroadcast(
                    applicationContext, 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }
}
