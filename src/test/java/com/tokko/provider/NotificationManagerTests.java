package com.tokko.provider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.tokko.BuildConfig;
import com.tokko.Util.TimeUtils;
import com.tokko.notificationmanager.NotificationManager;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.List;

@Config(emulateSdk = 19, constants = BuildConfig.class, manifest = "src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class NotificationManagerTests {
    private Context context;
    private ShadowAlarmManager sam;

    @Before
    public void setup(){
        context = RuntimeEnvironment.application.getApplicationContext();
        TimeUtils.setCurrentTime(new DateTime().withDate(2010, 5, 3).withTime(6, 0, 0, 0).withField(DateTimeFieldType.dayOfWeek(), 1));
        ShadowContentResolver.registerProvider(HabitProvider.AUTHORITY, new HabitProvider(){
            @Override
            public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
                MatrixCursor mc = new MatrixCursor(new String[]{ID, TIME, TITLE, WEEKDAY});
                mc.addRow(new Object[]{1, TimeUtils.timeToLong("12:00"), "1", 1});
                mc.addRow(new Object[]{1, TimeUtils.timeToLong("12:00"), "1", 2});
                mc.addRow(new Object[]{2, TimeUtils.timeToLong("12:00"), "2", 2});
                return mc;
            }
        });
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sam = Shadows.shadowOf(am);
    }

    @Test
    public void scheduleReminders_CorrectNumberOfAlarms(){
        NotificationManager.scheduleReminders(context);

        List<ShadowAlarmManager.ScheduledAlarm> scheduledAlarms = sam.getScheduledAlarms();
        Assert.assertNotNull(scheduledAlarms);
        Assert.assertEquals(3, scheduledAlarms.size());

    }

    @Test
    public void scheduleReminders_AlarmsAtCorrectTime(){
        NotificationManager.scheduleReminders(context);
        List<ShadowAlarmManager.ScheduledAlarm> scheduledAlarms = sam.getScheduledAlarms();
        Assert.assertEquals(TimeUtils.getCurrentTime().withTime(12, 0, 0, 0).getMillis(), scheduledAlarms.get(0).triggerAtTime);
        Assert.assertEquals(TimeUtils.getCurrentTime().withTime(12, 0, 0, 0).withDayOfWeek(2).getMillis(), scheduledAlarms.get(1).triggerAtTime);
        Assert.assertEquals(TimeUtils.getCurrentTime().withTime(12, 0, 0, 0).withDayOfWeek(2).getMillis(), scheduledAlarms.get(2).triggerAtTime);
    }

    @Test
    public void scheduleReminders_CancelAlarms(){
        NotificationManager.scheduleReminders(context);
        NotificationManager.cancelAllAlarms(context);
        List<ShadowAlarmManager.ScheduledAlarm> scheduledAlarms = sam.getScheduledAlarms();
        Assert.assertEquals(0, scheduledAlarms.size());
    }

    @Test
    public void receiverRegistered(){
        List<ShadowApplication.Wrapper> registeredReceivers = ShadowApplication.getInstance().getRegisteredReceivers();

        Assert.assertFalse(registeredReceivers.isEmpty());

        boolean receiverFound = false;
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (!receiverFound)
                receiverFound = NotificationManager.class.getSimpleName().equals(
                        wrapper.broadcastReceiver.getClass().getSimpleName());
        }

        Assert.assertTrue(receiverFound);
    }
}
