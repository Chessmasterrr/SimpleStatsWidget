package de.simplestatswidget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.RemoteViews

// ToDo
// https://developer.android.com/guide/topics/appwidgets


class SettingsActivity : Activity() {

    var mAppWidgetId: Int = 0 // widget id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get widget id
        this.mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        setContentView(R.layout.settings_layout)

        // remove widget, if user uses the back button
        setResult(RESULT_CANCELED)

        // get permissions if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CALL_LOG), 142)
            } else {
                if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_SMS), 142)
                }
                if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), 142)
                }
            }
        }

        // create widget on save button click
        val button = findViewById<Button>(R.id.savebutton)
        button.setOnClickListener {
            val context = this@SettingsActivity

            // set layout
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val views = RemoteViews(context.packageName,
                    R.layout.widget_layout)
            appWidgetManager.updateAppWidget(mAppWidgetId, views)

            // create widget
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
            setResult(RESULT_OK, resultValue)

            // update widget data (according to new settings)
            val thisWidget = ComponentName(context,
                    SimpleStatsWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val intent = Intent(context.applicationContext,
                    UpdateWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
            context.startService(intent)

            finish()
        }
    }
}
