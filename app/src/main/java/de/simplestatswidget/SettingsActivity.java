package de.simplestatswidget;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

public class SettingsActivity extends Activity {

    private int mAppWidgetId; // widget id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_layout);

        // remove widget, if user uses the back button
        setResult(RESULT_CANCELED);

        // get permissions if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
                    && (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CALL_LOG}, 142);
            } else {
                if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_SMS}, 142);
                }
                if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, 142);
                }
            }
        }

        // get widget id
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // create widget on save button click
        Button button = (Button) findViewById(R.id.savebutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Context context = SettingsActivity.this;

                // set layout
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                RemoteViews views = new RemoteViews(context.getPackageName(),
                        R.layout.widget_layout);
                appWidgetManager.updateAppWidget(mAppWidgetId, views);

                // create widget
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);

                // update widget data (according to new settings)
                ComponentName thisWidget = new ComponentName(context,
                        SimpleStatsWidgetProvider.class);
                int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                Intent intent = new Intent(context.getApplicationContext(),
                        UpdateWidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
                context.startService(intent);

                finish();
            }
        });
    }
}
