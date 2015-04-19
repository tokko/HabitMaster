package com.tokko.notificationmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Bootreceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager.registerSchedulingListener(context);
        NotificationManager.scheduleReminders(context.getApplicationContext());
    }
}
