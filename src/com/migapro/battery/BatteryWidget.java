package com.migapro.battery;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.RemoteViews;


public class BatteryWidget extends AppWidgetProvider {
	private static final String ACTION_BATTERY_UPDATE = "com.migapro.battery.action.UPDATE";
	private int batteryLevel = 0;
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		
		LogFile.log("onEnabled()");

		turnAlarmOnOff(context, true);
		context.startService(new Intent(context, ScreenMonitorService.class));
	}
	
	public static void turnAlarmOnOff(Context context, boolean turnOn) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_BATTERY_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		if (turnOn) { // Add extra 1 sec because sometimes ACTION_BATTERY_CHANGED is called after the first alarm
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, 300 * 1000, pendingIntent);
			LogFile.log("Alarm set");
		} else {
			alarmManager.cancel(pendingIntent);
			LogFile.log("Alarm disabled");
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		LogFile.log("onUpdate()");

		// Sometimes when the phone is booting, onUpdate method gets called before onEnabled()
		int currentLevel = calculateBatteryLevel(context);
		if (batteryChanged(currentLevel)) {
			batteryLevel = currentLevel;
			LogFile.log("Battery changed");
		}
		updateViews(context);
	}
	
	private boolean batteryChanged(int currentLevelLeft) {
		return (batteryLevel != currentLevelLeft);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		LogFile.log("onReceive() " + intent.getAction());
		
		if (intent.getAction().equals(ACTION_BATTERY_UPDATE)) {
			int currentLevel = calculateBatteryLevel(context);
			if (batteryChanged(currentLevel)) {
				LogFile.log("Battery changed");
				batteryLevel = currentLevel;
				updateViews(context);
			}
		}
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		
		LogFile.log("onDisabled()");
		
		turnAlarmOnOff(context, false);
		context.stopService(new Intent(context, ScreenMonitorService.class));
	}
	
	private int calculateBatteryLevel(Context context) {
		LogFile.log("calculateBatteryLevel()");
		
		Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		return level * 100 / scale;
	}
	
	private void updateViews(Context context) {
		LogFile.log("updateViews()");
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		views.setTextViewText(R.id.batteryText, batteryLevel + "%");
		
		ComponentName componentName = new ComponentName(context, BatteryWidget.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(componentName, views);
	}

}
