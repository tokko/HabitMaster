package com.tokko;

import android.app.Application;

import com.tokko.provider.HabitProvider;

public class HabitMasterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG)
            getContentResolver().call(HabitProvider.URI_HABIT_GROUPS, "seed", null, null);
    }
}
