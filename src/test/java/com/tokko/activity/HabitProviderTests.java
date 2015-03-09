package com.tokko.activity;

import android.content.ContentResolver;

import com.tokko.provider.HabitProvider;

import junit.framework.Assert;
import junit.framework.TestCase;

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
    }

    @Test
    public void preconditions() {
        Assert.assertNotNull(mProvider);
        Assert.assertNotNull(mContentResolver);
    }
}
