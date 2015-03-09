package com.tokko.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class HabitProvider extends ContentProvider {
    public static final String AUTHORITY = "com.tokko.provider.HabitProvider";

    public static final String DATABASE_NAME = "habitmaster";

    public static final String TABLE_HABIT_GROUPS = "habitgroups";

    public static final String HABIT_GROUP_ID = "_ID";
    public static final String HABIT_GROUP_TITLE = "title";
    public static final String HABIT_GROUP_TIME = "time";

    public static final Uri URI_GET_HABIT_GROUPS = Uri.parse("");

    DatabaseOpenHelper db;
    SQLiteDatabase sdb;

    public HabitProvider() {
    }

    public int seed(int numEntries, String habitGroupPrefix, long habitGroupTimeStart, long habitGroupTimeIncrement) {
        sdb = db.getWritableDatabase();
        sdb.beginTransaction();
        sdb.delete(TABLE_HABIT_GROUPS, null, null);
        int inserted = 0;
        while (numEntries-- > 0) {
            ContentValues cv = new ContentValues();
            cv.put(HABIT_GROUP_TITLE, habitGroupPrefix + numEntries);
            cv.put(HABIT_GROUP_TIME, habitGroupTimeStart + habitGroupTimeIncrement * numEntries);
            inserted += sdb.insertOrThrow(TABLE_HABIT_GROUPS, null, cv);
        }
        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        return inserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
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
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;

        public DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private static final String CREATE_HABIT_GROUPS = "CREATE TABLE IF NOT EXISTS " + TABLE_HABIT_GROUPS + "(" +
                HABIT_GROUP_ID + " INTEGER PRIMARY KEY, " +
                HABIT_GROUP_TITLE + " TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                HABIT_GROUP_TIME + " INTEGER NOT NULL DEFAULT 0);";

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_HABIT_GROUPS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            for (int version = oldVersion; version <= newVersion; version++) {
                switch (version) {

                    default:
                        break;
                }
            }
        }


    }
}
