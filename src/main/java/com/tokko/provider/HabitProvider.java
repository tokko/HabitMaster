package com.tokko.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;

public class HabitProvider extends ContentProvider {
    public static final String AUTHORITY = "com.tokko.provider.HabitProvider";

    public static final String DATABASE_NAME = "habitmaster";

    public static final String TABLE_HABIT_GROUPS = "habitgroups";
    public static final String TABLE_HABITS = "habits";

    public static final String ID = "_id";
    public static final String TITLE = "title";
    public static final String TIME = "time";
    public static final String WEEKDAY = "weekday";
    public static final String HABIT_GROUP = "habitgroup";

    private static final String TABLE_REPEATING = "repeating";

    private static final int KEY_INVALID = 0;
    private static final int KEY_HABIT_GROUPS = 1;
    private static final int KEY_REPEATING = 2;
    private static final int KEY_HABITS = 3;

    private static final String ACTION_HABIT_GROUPS = "HABIT_GROUPS";
    private static final String ACTION_REPEATING = "REPEATING";
    private static final String ACTION_HABITS = "HABITS";

    public static final Uri URI_GET_HABIT_INVALID = makeUri(KEY_INVALID, "SLASK");

    public static final Uri URI_HABIT_GROUPS = makeUri(KEY_HABIT_GROUPS, ACTION_HABIT_GROUPS);
    public static final Uri URI_REPEATING = makeUri(KEY_REPEATING, ACTION_REPEATING);
    public static final Uri URI_HABITS = makeUri(KEY_HABITS, ACTION_HABITS);

    private static UriMatcher um;
    DatabaseOpenHelper db;
    SQLiteDatabase sdb;

    public HabitProvider() {
    }

    private static Uri makeUri(int keyGetHabitGroups, String action) {
        if (um == null)
            um = new UriMatcher(UriMatcher.NO_MATCH);
        um.addURI(AUTHORITY, action, keyGetHabitGroups);
        return Uri.parse(String.format("content://%s/%s", AUTHORITY, action));
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if(method.equals("seed")){
            seed(20, "HABITGROUP", 0, 1, 10);
        }
        return super.call(method, arg, extras);
    }

    public void seed(int numEntries, String habitGroupPrefix, long habitGroupTimeStart, long habitGroupTimeIncrement, int numHabits) {
        sdb = db.getWritableDatabase();
        sdb.beginTransaction();
        sdb.delete(TABLE_HABIT_GROUPS, null, null);
        sdb.delete(TABLE_REPEATING, null, null);
        sdb.delete(TABLE_HABITS, null, null);
        while (numEntries-- > 0) {
            ContentValues cv = new ContentValues();
            cv.put(TITLE, habitGroupPrefix + numEntries);
            cv.put(TIME, habitGroupTimeStart + habitGroupTimeIncrement * numEntries);
            long id = sdb.insertOrThrow(TABLE_HABIT_GROUPS, null, cv);
            for(int i = 1; i < 5; i++){
                cv.clear();
                cv.put(HABIT_GROUP, id);
                cv.put(WEEKDAY, i);
                sdb.insertOrThrow(TABLE_REPEATING, null, cv);
            }
        }
        while(numHabits-- > 0){
            ContentValues cv = new ContentValues();
            cv.put(TITLE, "HABIT"+numHabits);
            sdb.insertOrThrow(TABLE_HABITS, null, cv);
        }
        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        getContext().getContentResolver().notifyChange(URI_HABIT_GROUPS, null);
    }

    public static String whereEquals(String field){
        return String.format("%s=?", field);
    }
    public static String whereID(){
        return whereEquals(ID);
    }

    public static String[] idArgs(Number id){
        return new String[]{String.valueOf(id)};
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleted;
        sdb = db.getWritableDatabase();
        switch (um.match(uri)){
            case KEY_HABIT_GROUPS:
                deleted = sdb.delete(TABLE_HABIT_GROUPS, selection, selectionArgs);
                if(deleted > 0)
                    getContext().getContentResolver().notifyChange(URI_HABIT_GROUPS, null);
                return deleted;
            case KEY_REPEATING:
                deleted = sdb.delete(TABLE_REPEATING, selection, selectionArgs);
                if(deleted > 0)
                    getContext().getContentResolver().notifyChange(URI_REPEATING, null);
                return deleted;
            case KEY_HABITS:
                deleted = sdb.delete(TABLE_HABITS, selection, selectionArgs);
                if(deleted > 0)
                    getContext().getContentResolver().notifyChange(URI_HABITS, null);
                return deleted;
            default:
                throw new IllegalStateException("Unknown uri");
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        return "";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        db = new DatabaseOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        sdb = db.getReadableDatabase();
        Cursor c;
        switch (um.match(uri)) {
            case KEY_HABIT_GROUPS:
                c = sdb.query(TABLE_HABIT_GROUPS, projection, selection, selectionArgs, null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), URI_HABIT_GROUPS);
                return c;
            case KEY_REPEATING:
                c = sdb.query(TABLE_REPEATING, projection, selection, selectionArgs, null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), URI_REPEATING);
                return c;
            case KEY_HABITS:
                c = sdb.query(TABLE_HABITS, projection, selection, selectionArgs, null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), URI_HABITS);
                return c;
            default:
                throw new IllegalStateException("Unknown uri");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        sdb = db.getWritableDatabase();
        int updated;
        switch (um.match(uri)){
            case KEY_HABIT_GROUPS:
                updated = sdb.update(TABLE_HABIT_GROUPS, values, selection, selectionArgs);
                if(updated > 0)
                    getContext().getContentResolver().notifyChange(URI_HABIT_GROUPS, null);
                return updated;
            case KEY_HABITS:
                updated = sdb.update(TABLE_HABITS, values, selection, selectionArgs);
                if(updated > 0)
                    getContext().getContentResolver().notifyChange(URI_HABITS, null);
                return updated;
            default:
                throw new IllegalStateException("Unknown uri");
        }
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 4;

        public DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_HABIT_GROUPS);
            db.execSQL(CREATE_HABIT_GROUPS_REPEAT);
            db.execSQL(CREATE_TABLE_HABITS);
        }

        private static final String CREATE_HABIT_GROUPS = "CREATE TABLE IF NOT EXISTS " + TABLE_HABIT_GROUPS + "(" +
                ID + " INTEGER PRIMARY KEY, " +
                TITLE + " TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                TIME + " INTEGER NOT NULL DEFAULT 0);";

        private static final String CREATE_HABIT_GROUPS_REPEAT = "CREATE TABLE IF NOT EXISTS " + TABLE_REPEATING + "(" +
                ID + " INTEGER PRIMARY KEY, " +
                HABIT_GROUP + " INTEGER NOT NULL, " +
                WEEKDAY + " INTEGER NOT NULL);";

        private static final String CREATE_TABLE_HABITS = "CREATE TABLE IF NOT EXISTS " + TABLE_HABITS + "(" +
                ID + " INTEGER PRIMARY KEY, " +
                TITLE + " TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                TIME + " INTEGER NOT NULL DEFAULT 0);";

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABIT_GROUPS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPEATING);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
            onCreate(db);
            newVersion = oldVersion-1;
            for (int version = oldVersion; version <= newVersion; version++) {
                switch (version) {

                    default:
                        break;
                }
            }
        }




    }
}
