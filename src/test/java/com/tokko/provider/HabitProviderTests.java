package com.tokko.provider;

import android.content.ContentResolver;
import android.database.Cursor;

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
    private static final String HABIT_GROUP_PREFIX = "HabitGroup";
    private static final int NUM_HABIT_GROUPS = 20;
    private static final long HABIT_GROUP_START_TIME = 0;
    private static final long HABIT_GROUP_TIME_INCREMENT = DateTimeConstants.MILLIS_PER_HOUR;
    private HabitProvider mProvider;
    private ContentResolver mContentResolver;

    @Before
    public void setup() {
        mProvider = new HabitProvider();
        mContentResolver = Robolectric.application.getContentResolver();
        mProvider.onCreate();
        ShadowContentResolver.registerProvider(HabitProvider.AUTHORITY, mProvider);
        mProvider.seed(NUM_HABIT_GROUPS, HABIT_GROUP_PREFIX, HABIT_GROUP_START_TIME, HABIT_GROUP_TIME_INCREMENT);
    }

    @Test
    public void preconditions() {
        Assert.assertNotNull(mProvider);
        Assert.assertNotNull(mContentResolver);
    }

    @Test
    public void getHabitGroups() {
        Cursor c = mContentResolver.query(HabitProvider.URI_GET_HABIT_GROUPS, null, null, null, HabitProvider.HABIT_GROUP_ID + " DESC");
        assertNotNull(c);
        assertEquals(NUM_HABIT_GROUPS, c.getCount());
        assertEquals(3, c.getColumnNames().length);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_ID) >= 0);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_TIME) >= 0);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_TITLE) >= 0);
        int postfix = 0;
        long time = HABIT_GROUP_START_TIME;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            assertEquals(HABIT_GROUP_PREFIX + postfix++, c.getString(c.getColumnIndex(HabitProvider.HABIT_GROUP_TITLE)));
            assertEquals(time, c.getLong(c.getColumnIndex(HabitProvider.HABIT_GROUP_TIME)));
            time += HABIT_GROUP_TIME_INCREMENT;
        }
    }

    @Test
    public void getSingleHabitGroup() {
        String title = HABIT_GROUP_PREFIX + "3";
        Cursor c = mContentResolver.query(HabitProvider.URI_GET_HABIT_GROUPS, null, String.format("%s=?", HabitProvider.HABIT_GROUP_TITLE), new String[]{title}, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertEquals(3, c.getColumnNames().length);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_ID) >= 0);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_TIME) >= 0);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_TITLE) >= 0);
        assertTrue(c.moveToFirst());
        assertEquals(title, c.getString(c.getColumnIndex(HabitProvider.HABIT_GROUP_TITLE)));
    }

    @Test
    public void getHabitGroupTitles() {
        Cursor c = mContentResolver.query(HabitProvider.URI_GET_HABIT_GROUPS, new String[]{HabitProvider.HABIT_GROUP_TITLE}, null, null, HabitProvider.HABIT_GROUP_ID + " DESC");
        assertNotNull(c);
        assertEquals(NUM_HABIT_GROUPS, c.getCount());
        assertEquals(1, c.getColumnNames().length);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_ID) == -1);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_TIME) == -1);
        assertTrue(c.getColumnIndex(HabitProvider.HABIT_GROUP_TITLE) >= 0);
        int postfix = 0;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            assertEquals(HABIT_GROUP_PREFIX + postfix++, c.getString(c.getColumnIndex(HabitProvider.HABIT_GROUP_TITLE)));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void queryInvalidUri_ThrowsIllegalStateException() {
        mContentResolver.query(HabitProvider.URI_GET_HABIT_INVALID, null, null, null, null);
    }

}
