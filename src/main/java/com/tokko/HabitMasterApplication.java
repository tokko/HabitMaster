package com.tokko;

import android.app.Application;

import com.tokko.provider.HabitProvider;

/**
 * Created by 79417 on 2015-04-02.
 */
public class HabitMasterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG)
            getContentResolver().call(HabitProvider.URI_HABIT_GROUPS, "seed", null, null);
    }
}
