package com.tokko.provider;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class HabitWithConnection implements Parcelable{
    public long id;
    public Long habitGroup;
    public long habit;
    public String title;

    public HabitWithConnection(Cursor cursor){
        id = cursor.getLong(cursor.getColumnIndex(HabitProvider.ID));
        int i = cursor.getColumnIndex(HabitProvider.HABIT_GROUP);
        habitGroup = cursor.isNull(i) ? null : cursor.getLong(i);
        habit = cursor.getLong(cursor.getColumnIndex(HabitProvider.ID));
        title = cursor.getString(cursor.getColumnIndex(HabitProvider.TITLE));
    }

    public HabitWithConnection(Parcel source) {
        id = source.readLong();
        habit = source.readLong();
        habitGroup = source.readLong();
        title = source.readString();
    }

    public ContentProviderOperation toInsertOperation(){
        return ContentProviderOperation.newInsert(HabitProvider.URI_HABITS_IN_GROUP).withValue(HabitProvider.HABIT, habit).withValue(HabitProvider.HABIT_GROUP, habitGroup).build();
    }

    public static ArrayList<ContentProviderOperation> toInsertOperations(ArrayList<HabitWithConnection> data){
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (HabitWithConnection d : data) {
            if(d.habitGroup == null) continue;
            ops.add(d.toInsertOperation());
        }
        return ops;
    }

    public static ArrayList<HabitWithConnection> fromCursor(Cursor c){
        ArrayList<HabitWithConnection> data = new ArrayList<>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            data.add(new HabitWithConnection(c));
        return data;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(habit);
        dest.writeLong(habitGroup);
        dest.writeString(title);
    }

    public static final Parcelable.Creator<HabitWithConnection> CREATOR = new Parcelable.Creator<HabitWithConnection>() {
        @Override
        public HabitWithConnection createFromParcel(Parcel source) {
            return new HabitWithConnection(source);
        }

        @Override
        public HabitWithConnection[] newArray(int size) {
            return new HabitWithConnection[0];
        }
    };
}
