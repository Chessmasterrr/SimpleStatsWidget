package de.simplestatswidget;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UpdateWidgetService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // check permissions and get data
        String smsText;
        String callText;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) &&
                    (checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)) {
                smsText = String.valueOf(getSmsCount());
                callText = getCalls();
            } else {
                smsText = null;
                callText = null;
            }
        } else {
            smsText = String.valueOf(getSmsCount());
            callText = getCalls();
        }

        updateWidgets(intent, smsText, callText);
        stopSelf();

        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateWidgets(Intent intent, String smsText, String callText) {
        // get ids
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());
        int[] allWidgetIds = intent
                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        // update all widgets
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    R.layout.widget_layout);
            // set data
            if ((smsText == null) && (callText == null)) {
                remoteViews.setViewVisibility(R.id.widget_sms_header, View.GONE);
                remoteViews.setTextViewText(R.id.sms_text, "missing");
                remoteViews.setViewVisibility(R.id.widget_calls_header, View.GONE);
                remoteViews.setTextViewText(R.id.calls_text, "permissions");
            } else {
                if (smsText != null) {
                    remoteViews.setTextViewText(R.id.sms_text, smsText);
                }
                if (callText != null) {
                    remoteViews.setTextViewText(R.id.calls_text, callText);
                }
            }

            // register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(),
                    SimpleStatsWidgetProvider.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getApplicationContext(), 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_sms_header, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private int getSmsCount() {
        // get settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String smsCharCountString = prefs.getString("smsCharCount", "160");
        int smsCharCount = Integer.parseInt(smsCharCountString);

        // set a date for this month
        Calendar calendar = new GregorianCalendar();
        Date this_month = new GregorianCalendar(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), 1).getTime();

        // get count of sent sms for this month
        Uri sentMessage = Uri.parse("content://sms/sent/");
        ContentResolver cr = this.getContentResolver();
        int smscount = 0;
        Cursor c = cr.query(sentMessage, null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                String date_string = c.getString(c.getColumnIndexOrThrow("date"));
                Date date = new Date(Long.valueOf(date_string));
                if (date.after(this_month)) {
                    String message = c.getString(c.getColumnIndexOrThrow("body"));
                    smscount += (int) Math.round((double) message.length() / smsCharCount + 0.5);
                }
            }
            c.close();
        }

        return smscount;
    }

    private String getCalls() {
        // get settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean round = prefs.getBoolean("round", false);

        // set a date for this month
        Calendar calendar = new GregorianCalendar();
        Date this_month = new GregorianCalendar(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), 1).getTime();

        // get duration of outgoing calls for this month
        Uri allCalls = Uri.parse("content://call_log/calls");
        ContentResolver cr = this.getContentResolver();
        int callduration = 0;
        Cursor c = cr.query(allCalls, null, "type = " + CallLog.Calls.OUTGOING_TYPE, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                String date_string = c.getString(c.getColumnIndexOrThrow("date"));
                Date date = new Date(Long.valueOf(date_string));
                if (date.after(this_month)) {
                    int duration = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("duration")));
                    if (duration > 0) {
                        if (round) {
                            callduration += (int) Math.round((double) duration / 60 + 0.5) * 60;
                        } else {
                            callduration += duration;
                        }
                    }
                }
            }
            c.close();
        }

        // Set the text
        int minutes = callduration / 60;
        int seconds = callduration % 60;
        String minutesString = String.valueOf(minutes);
        String secondsString = String.valueOf(seconds);
        if (minutes < 10) {
            minutesString = "0" + minutesString;
        }
        if (seconds < 10) {
            secondsString = "0" + secondsString;
        }

        return minutesString + ":" + secondsString;
    }
}
