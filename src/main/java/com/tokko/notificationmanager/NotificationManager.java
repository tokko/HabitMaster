package com.tokko.notificationmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.tokko.Util.TimeUtils;
import com.tokko.provider.HabitProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;

public class NotificationManager extends BroadcastReceiver {
    public static final String ACTION_HABIT_GROUP_TRIGGER = "ACTION_HABIT_GROUP_TRIGGER";
    private static final String EXTRA_GROUP_TITLE = "extratitle";

    private static int id = 0;
    public static void scheduleReminders(Context context){
        Cursor reminders = context.getContentResolver().query(HabitProvider.URI_REMINDERS, null, null, null, null);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (reminders.moveToFirst(); !reminders.isAfterLast(); reminders.moveToNext()){
            String title = reminders.getString(reminders.getColumnIndex(HabitProvider.TITLE));
            long time = reminders.getLong(reminders.getColumnIndex(HabitProvider.TIME));
            int weekday = reminders.getInt(reminders.getColumnIndex(HabitProvider.WEEKDAY));

            DateTime dt = TimeUtils.getCurrentTime()
                    .withTime(TimeUtils.extractHours(time), TimeUtils.extractMinutes(time), 0, 0)
                    .withField(DateTimeFieldType.dayOfWeek(), weekday);
            if(dt.isBefore(TimeUtils.getCurrentTime().getMillis()))
                dt = dt.withFieldAdded(DurationFieldType.weekyears(), 1);
            am.set(AlarmManager.RTC_WAKEUP, dt.getMillis(), getPendingIntent(context, id++, title));
        }
        reminders.close();
    }

    private static PendingIntent getPendingIntent(Context context, int i){
        return getPendingIntent(context, i, null);
    }
    private static PendingIntent getPendingIntent(Context context, int i, String title){
        Intent intent = new Intent(String.format("%s%d", ACTION_HABIT_GROUP_TRIGGER, i)).putExtra(EXTRA_GROUP_TITLE, title);
        return PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }

    public static void cancelAllAlarms(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        while(--id>0)
            am.cancel(getPendingIntent(context, id));
    }
}
