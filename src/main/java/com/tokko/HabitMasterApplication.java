package com.tokko;

import android.app.Application;

import com.tokko.notificationmanager.Bootreceiver;
import com.tokko.provider.HabitProvider;

public class HabitMasterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new Bootreceiver().onReceive(this, null);
        if(BuildConfig.BUILD_TYPE.equals("mock"))
            getContentResolver().call(HabitProvider.URI_HABIT_GROUPS, "seed", null, null);
    }
}
