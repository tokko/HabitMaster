package com.tokko.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
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
        Cursor c = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, null, null, HabitProvider.ID + " DESC");
        assertNotNull(c);
        assertEquals(NUM_HABIT_GROUPS, c.getCount());
        assertEquals(3, c.getColumnNames().length);
        assertTrue(c.getColumnIndex(HabitProvider.ID) >= 0);
        assertTrue(c.getColumnIndex(HabitProvider.TIME) >= 0);
        assertTrue(c.getColumnIndex(HabitProvider.TITLE) >= 0);
        int postfix = 0;
        long time = HABIT_GROUP_START_TIME;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            assertEquals(HABIT_GROUP_PREFIX + postfix++, c.getString(c.getColumnIndex(HabitProvider.TITLE)));
            assertEquals(time, c.getLong(c.getColumnIndex(HabitProvider.TIME)));
            time += HABIT_GROUP_TIME_INCREMENT;
        }
    }

    @Test
    public void getSingleHabitGroup() {
        String title = HABIT_GROUP_PREFIX + "3";
        Cursor c = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, String.format("%s=?", HabitProvider.TITLE), new String[]{title}, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertEquals(3, c.getColumnNames().length);
        assertTrue(c.getColumnIndex(HabitProvider.ID) >= 0);
        assertTrue(c.getColumnIndex(HabitProvider.TIME) >= 0);
        assertTrue(c.getColumnIndex(HabitProvider.TITLE) >= 0);
        assertTrue(c.moveToFirst());
        assertEquals(title, c.getString(c.getColumnIndex(HabitProvider.TITLE)));
    }

    @Test
    public void getHabitGroupTitles() {
        Cursor c = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, new String[]{HabitProvider.TITLE}, null, null, HabitProvider.ID + " DESC");
        assertNotNull(c);
        assertEquals(NUM_HABIT_GROUPS, c.getCount());
        assertEquals(1, c.getColumnNames().length);
        assertTrue(c.getColumnIndex(HabitProvider.ID) == -1);
        assertTrue(c.getColumnIndex(HabitProvider.TIME) == -1);
        assertTrue(c.getColumnIndex(HabitProvider.TITLE) >= 0);
        int postfix = 0;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            assertEquals(HABIT_GROUP_PREFIX + postfix++, c.getString(c.getColumnIndex(HabitProvider.TITLE)));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void queryInvalidUri_ThrowsIllegalStateException() {
        mContentResolver.query(HabitProvider.URI_GET_HABIT_INVALID, null, null, null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void updateInvalidUri_ThrowsIllegalStateException() {
        mContentResolver.update(HabitProvider.URI_GET_HABIT_INVALID, new ContentValues(), null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void deleteInvalidUri_ThrowsIllegalStateException() {
        mContentResolver.delete(HabitProvider.URI_GET_HABIT_INVALID, null, null);
    }

    @Test
    public void testUpdateHabitGroup(){
        Cursor toUpdate = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, null, null, null);
        toUpdate.moveToLast();
        long id = toUpdate.getLong(toUpdate.getColumnIndex(HabitProvider.ID));
        long time  = toUpdate.getInt(toUpdate.getColumnIndex(HabitProvider.TIME));
        toUpdate.close();
        ContentValues cv = new ContentValues();
        String newTitle = "bananfisk2000";
        cv.put(HabitProvider.TITLE, newTitle);
        int updated = mContentResolver.update(HabitProvider.URI_HABIT_GROUPS, cv, HabitProvider.whereID(), HabitProvider.idArgs(id));
        assertEquals(1, updated);
        Cursor afterUpdate = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, HabitProvider.whereID(), HabitProvider.idArgs(id), null);
        assertEquals(1, afterUpdate.getCount());
        assertTrue(afterUpdate.moveToFirst());
        assertEquals(time, afterUpdate.getLong(afterUpdate.getColumnIndex(HabitProvider.TIME)));
        assertEquals(newTitle, afterUpdate.getString(afterUpdate.getColumnIndex(HabitProvider.TITLE)));
        afterUpdate.close();
    }

    @Test
    public void testDeleteHabitgroup(){
        Cursor before = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, null, null, null);
        before.moveToPosition(before.getCount()/2);
        long id = before.getLong(before.getColumnIndex(HabitProvider.ID));
        int count = before.getCount();
        before.close();
        int deleted = mContentResolver.delete(HabitProvider.URI_HABIT_GROUPS, HabitProvider.whereID(), HabitProvider.idArgs(id));
        assertEquals(1, deleted);
        Cursor after = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, null, null, null);
        assertEquals(count-1, after.getCount());
        for(after.moveToFirst(); !after.isAfterLast(); after.moveToNext())
            assertTrue(id != after.getLong(after.getColumnIndex(HabitProvider.ID)));
        after.close();
        after = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, HabitProvider.whereID(), HabitProvider.idArgs(id), null);
        assertEquals(0, after.getCount());
        after.close();
    }
}
