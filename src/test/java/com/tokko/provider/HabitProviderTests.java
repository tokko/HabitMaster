package com.tokko.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.logging.Handler;

@Config(emulateSdk = 18, manifest = "app/src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class HabitProviderTests extends TestCase {
    private static final String HABIT_GROUP_PREFIX = "HabitGroup";
    private static final int NUM_HABIT_GROUPS = 20;
    private static final int NUM_HABITS = 10;
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
        mProvider.seed(NUM_HABIT_GROUPS, HABIT_GROUP_PREFIX, HABIT_GROUP_START_TIME, HABIT_GROUP_TIME_INCREMENT, NUM_HABITS);
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
    @Test(expected = IllegalStateException.class)
    public void insertInvalidUri_ThrowsIllegalStateException() {
        mContentResolver.insert(HabitProvider.URI_GET_HABIT_INVALID, null);
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
        before.moveToPosition(before.getCount() / 2);
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

    @Test
    public void testDeleteRepeating(){
        Cursor before = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, null, null, null);
        before.moveToPosition(before.getCount()/2);
        long id = before.getLong(before.getColumnIndex(HabitProvider.ID));
        before.close();
        int deleted = mContentResolver.delete(HabitProvider.URI_REPEATING, HabitProvider.whereEquals(HabitProvider.HABIT_GROUP), HabitProvider.idArgs(id));
        assertEquals(4, deleted);
        Cursor after = mContentResolver.query(HabitProvider.URI_REPEATING, null, HabitProvider.whereEquals(HabitProvider.HABIT_GROUP), HabitProvider.idArgs(id), null);
        assertEquals(0, after.getCount());
        after.close();
    }

    @Test
    public void testGetRepeatings(){
        Cursor c = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, null, null, null);
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            long id = c.getLong(c.getColumnIndex(HabitProvider.ID));
            Cursor c2 = mContentResolver.query(HabitProvider.URI_REPEATING, null, HabitProvider.whereEquals(HabitProvider.HABIT_GROUP), HabitProvider.idArgs(id), null);
            assertEquals(4, c2.getCount());
            c2.close();
        }
        c.close();
    }

    @Test
    public void testGetHabits(){
        Cursor c = mContentResolver.query(HabitProvider.URI_HABITS, null, null, null, null);
        assertNotNull(c);
        assertEquals(10, c.getCount());
        int i = NUM_HABITS-1;
        for(assertTrue(c.moveToFirst()); !c.isAfterLast(); c.moveToNext()){
            assertEquals("HABIT" + i--, c.getString(c.getColumnIndex(HabitProvider.TITLE)));
            assertEquals(0, c.getLong(c.getColumnIndex(HabitProvider.TIME)));
        }
        c.close();
    }

    @Test
    public void testDeleteHabit(){
        Cursor habits = mContentResolver.query(HabitProvider.URI_HABITS, null, null, null, null);
        habits.moveToFirst();
        long id = habits.getLong(habits.getColumnIndex(HabitProvider.ID));
        int deleted = mContentResolver.delete(HabitProvider.URI_HABITS, HabitProvider.whereID(), HabitProvider.idArgs(id));
        assertEquals(1, deleted);
        Cursor after = mContentResolver.query(HabitProvider.URI_HABITS, null, null, null, null);
        assertEquals(habits.getCount()-1, after.getCount());
        for(after.moveToFirst(); !after.isAfterLast(); after.moveToNext())
            assertTrue(id != after.getLong(after.getColumnIndex(HabitProvider.ID)));
        habits.close();
        after.close();
    }

    @Test
    public void updateHabit(){
        final String newTitle = "bananfisk2000";
        Cursor habits = mContentResolver.query(HabitProvider.URI_HABITS, null, null, null, null);
        habits.moveToFirst();
        long id = habits.getLong(habits.getColumnIndex(HabitProvider.ID));
        habits.close();
        ContentValues cv = new ContentValues();
        cv.put(HabitProvider.TITLE, newTitle);
        int updated = mContentResolver.update(HabitProvider.URI_HABITS, cv, HabitProvider.whereID(), HabitProvider.idArgs(id));
        assertEquals(1, updated);
        habits = mContentResolver.query(HabitProvider.URI_HABITS, null, HabitProvider.whereID(), HabitProvider.idArgs(id), null);
        habits.moveToFirst();
        assertEquals(newTitle, habits.getString(habits.getColumnIndex(HabitProvider.TITLE)));
        habits.close();
    }

    @Test
    public void insertRepeating(){
        final int weekday = 1;
        Cursor repeating = mContentResolver.query(HabitProvider.URI_REPEATING, null, null, null, null);
        repeating.moveToFirst();
        long id = repeating.getLong(repeating.getColumnIndex(HabitProvider.ID));
        ContentValues cv = new ContentValues();
        cv.put(HabitProvider.HABIT_GROUP, id);
        cv.put(HabitProvider.WEEKDAY, weekday);
        Uri insertedUri = mContentResolver.insert(HabitProvider.URI_REPEATING, cv);
        long newRowId = ContentUris.parseId(insertedUri);
        Cursor repeating2 = mContentResolver.query(HabitProvider.URI_REPEATING, null, null, null, null);
        assertEquals(repeating.getCount()+1, repeating2.getCount());
        Cursor c = mContentResolver.query(HabitProvider.URI_REPEATING, null, HabitProvider.whereID(), HabitProvider.idArgs(newRowId), null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(1, c.getCount());
        assertEquals(weekday, c.getInt(c.getColumnIndex(HabitProvider.WEEKDAY)));
        assertEquals(id, c.getLong(c.getColumnIndex(HabitProvider.HABIT_GROUP)));

        repeating.close();
        repeating2.close();
        c.close();
    }

    @Test
    public void getConnections(){
        Cursor c = mContentResolver.query(HabitProvider.URI_HABITS_IN_GROUP, null, null, null, null);
        assertNotNull(c);
        assertTrue(c.getCount() > 0);
        assertEquals(3, c.getColumnCount());
        c.close();
    }

    @Test
    public void removeConnections(){
        Cursor c = mContentResolver.query(HabitProvider.URI_HABITS_IN_GROUP, null, null, null, null);
        c.moveToFirst();
        long habitGroup = c.getLong(c.getColumnIndex(HabitProvider.HABIT_GROUP));
        int deleted = mContentResolver.delete(HabitProvider.URI_HABITS_IN_GROUP, HabitProvider.whereEquals(HabitProvider.HABIT_GROUP), HabitProvider.idArgs(habitGroup));
        assertEquals(NUM_HABITS/2, deleted);
        Cursor c2 = mContentResolver.query(HabitProvider.URI_HABITS_IN_GROUP, null, null, null, null);
        assertEquals(c.getCount()-deleted, c2.getCount());
        for(c2.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            assertTrue(c2.getLong(c.getColumnIndex(HabitProvider.HABIT_GROUP)) != habitGroup);
        c.close();
        c2.close();
    }

    @Test
    public void testInsertConnection(){
        Cursor c = mContentResolver.query(HabitProvider.URI_HABITS_IN_GROUP, null, null, null, null);
        Cursor groups = mContentResolver.query(HabitProvider.URI_HABIT_GROUPS, null, null, null, null);
        Cursor habits = mContentResolver.query(HabitProvider.URI_HABITS, null, null, null, null);
        groups.moveToLast();
        habits.moveToLast();
        long groupId = groups.getLong(groups.getColumnIndex(HabitProvider.ID));
        long habitId = habits.getLong(habits.getColumnIndex(HabitProvider.ID));
        ContentValues cv = new ContentValues();
        cv.put(HabitProvider.HABIT, habitId);
        cv.put(HabitProvider.HABIT_GROUP, groupId);
        Uri newRowUri = mContentResolver.insert(HabitProvider.URI_HABIT_GROUPS, cv);
        long newId = ContentUris.parseId(newRowUri);
        assertTrue(newId != -1);
        Cursor c2 = mContentResolver.query(HabitProvider.URI_HABITS_IN_GROUP, null, HabitProvider.whereID(), HabitProvider.idArgs(newId), null);
        assertNotNull(c2);
        assertTrue(c2.moveToFirst());
        assertEquals(1, c2.getCount());
        assertEquals(groupId, c2.getLong(c.getColumnIndex(HabitProvider.HABIT_GROUP)));
        assertEquals(habitId, c2.getLong(c.getColumnIndex(HabitProvider.HABIT)));
        c2.close();
        c2 = mContentResolver.query(HabitProvider.URI_HABITS_IN_GROUP, null, null, null, null);
        assertEquals(c.getCount()+1, c2.getCount());
        c.close();
        c2.close();
        groups.close();
        habits.close();

    }
}
