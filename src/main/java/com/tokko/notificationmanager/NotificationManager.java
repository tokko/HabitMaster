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

    public static void ScheduleReminders(Context context){
        Cursor reminders = context.getContentResolver().query(HabitProvider.URI_REMINDERS, null, null, null, null);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (reminders.moveToFirst(); !reminders.isAfterLast(); reminders.moveToNext()){
            String title = reminders.getString(reminders.getColumnIndex(HabitProvider.TITLE));
            long time = reminders.getLong(reminders.getColumnIndex(HabitProvider.TIME));
            int weekday = reminders.getInt(reminders.getColumnIndex(HabitProvider.WEEKDAY));
            long id = reminders.getLong(reminders.getColumnIndex(HabitProvider.ID));

            DateTime dt = TimeUtils.getCurrentTime()
                    .withTime(TimeUtils.extractHours(time), TimeUtils.extractMinutes(time), 0, 0)
                    .withField(DateTimeFieldType.dayOfWeek(), weekday);
            if(dt.isBefore(TimeUtils.getCurrentTime().getMillis()))
                dt = dt.withFieldAdded(DurationFieldType.weekyears(), 1);
            Intent intent = new Intent(context.getApplicationContext(), NotificationManager.class).putExtra(EXTRA_GROUP_TITLE, title);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, dt.getMillis(), pendingIntent);
        }
        reminders.close();
    }
    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
