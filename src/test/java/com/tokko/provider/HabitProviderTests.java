package com.tokko.provider;

import android.content.ContentResolver;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

@Config(emulateSdk = 18, manifest = "app/src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class HabitProviderTests extends TestCase {

    private HabitProvider mProvider;
    private ContentResolver mContentResolver;

    @Before
    public void setup() {
        mProvider = new HabitProvider();
        mContentResolver = Robolectric.application.getContentResolver();
        mProvider.onCreate();
        ShadowContentResolver.registerProvider(HabitProvider.AUTHORITY, mProvider);
        mProvider.seed(20, "HabitGroup", 0, DateTimeConstants.MILLIS_PER_HOUR);
    }

    @Test
    public void preconditions() {
        Assert.assertNotNull(mProvider);
        Assert.assertNotNull(mContentResolver);
    }
}
