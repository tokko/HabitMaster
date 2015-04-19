package com.tokko.notificationmanager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;

import com.tokko.Util.TimeUtils;
import com.tokko.provider.HabitProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;

import java.lang.reflect.Field;

public class NotificationManager extends BroadcastReceiver {
    public static final String ACTION_HABIT_GROUP_TRIGGER = "ACTION_HABIT_GROUP_TRIGGER";
    public static final String EXTRA_GROUP_ID = "extragroupid";

    private static int id = 0;
    public static void scheduleReminders(Context context){
        Cursor reminders = context.getContentResolver().query(HabitProvider.URI_REMINDERS, null, null, null, null);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        cancelAllAlarms(context);
        for (reminders.moveToFirst(); !reminders.isAfterLast(); reminders.moveToNext()){
            long time = reminders.getLong(reminders.getColumnIndex(HabitProvider.TIME));
            int weekday = reminders.getInt(reminders.getColumnIndex(HabitProvider.WEEKDAY));
            long habitGroupId = reminders.getLong(reminders.getColumnIndex(HabitProvider.ID));
            DateTime dt = TimeUtils.getCurrentTime()
                    .withTime(TimeUtils.extractHours(time), TimeUtils.extractMinutes(time), 0, 0)
                    .withField(DateTimeFieldType.dayOfWeek(), weekday);
            if(dt.isBefore(TimeUtils.getCurrentTime().getMillis()))
                dt = dt.withFieldAdded(DurationFieldType.weekyears(), 1);
            am.set(AlarmManager.RTC_WAKEUP, dt.getMillis(), getPendingIntent(context, id++, habitGroupId));
        }
        reminders.close();
    }

    private static PendingIntent getPendingIntent(Context context, int i){
        return getPendingIntent(context, i, -1);
    }
    private static PendingIntent getPendingIntent(Context context, int i, long id){
        Intent intent = new Intent(String.format("%s%d", ACTION_HABIT_GROUP_TRIGGER, i)).putExtra(EXTRA_GROUP_ID, id);
        return PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void registerSchedulingListener(final Context context){
        Field[] fields = HabitProvider.class.getClass().getFields();
        try {
            for (Field field : fields) {
                if (field.getName().startsWith("URI_")) {
                    Uri uri = (Uri) field.get(null);
                    context.getApplicationContext().getContentResolver().registerContentObserver(uri, false ,new ContentObserver(new Handler()){
                        @Override
                        public void onChange(boolean selfChange) {
                            scheduleReminders(context);
                        }
                    });
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        long groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1);
        if(groupId == -1) throw new IllegalStateException("Invalid group id");
        android.app.NotificationManager nm = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Cursor habits = context.getContentResolver().query(HabitProvider.URI_HABITS_IN_GROUP, null, HabitProvider.whereEquals(HabitProvider.HABIT_GROUP), HabitProvider.idArgs(groupId), null);
        for (habits.moveToFirst(); !habits.isAfterLast(); habits.moveToNext()){
            Notification.Builder nb = new Notification.Builder(context.getApplicationContext());
            nb.setAutoCancel(false);
            nb.setContentTitle(habits.getString(habits.getColumnIndex(HabitProvider.TITLE)));
            nb.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            nb.setSmallIcon(android.R.drawable.ic_popup_reminder);
            nm.notify((int) System.currentTimeMillis(), nb.build());
        }
        habits.close();
        scheduleReminders(context.getApplicationContext());
    }

    public static void cancelAllAlarms(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        while(id>=0)
            am.cancel(getPendingIntent(context, id--));
        id = 0;
    }
}
