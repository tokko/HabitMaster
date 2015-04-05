package com.tokko.config;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tokko.R;
import com.tokko.Util.TimeUtils;
import com.tokko.provider.HabitProvider;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;

public class HabitgroupEditorFragment extends Fragment implements View.OnClickListener {
    protected static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_HOUR = "extra_hour";
    private static final String EXTRA_MINUTE = "extra_minute";
    private static final String EXTRA_WEEKDAYS = "extra_weekdays";

    private Button okButton;
    private Button deleteButton;
    private Button cancelButton;
    private EditText titleEditText;
    private long id = -1;
    private HabitGroupEditorHost host;
    private Button setTimeButton;
    private int hour;
    private int minute;
    private Button pickWeekdaysButton;
    private ArrayList<Integer> weekdays;
    private String title;


    public static HabitgroupEditorFragment newInstance(long id){
        Bundle b = new Bundle();
        b.putLong(EXTRA_ID, id);
        HabitgroupEditorFragment f = new HabitgroupEditorFragment();
        f.setArguments(b);
        return f;
    }

    protected Uri getUri(){
        return HabitProvider.URI_HABIT_GROUPS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            title = savedInstanceState.getString(EXTRA_TITLE, "");
            id = savedInstanceState.getLong(EXTRA_ID, -1);
            hour = savedInstanceState.getInt(EXTRA_HOUR);
            minute = savedInstanceState.getInt(EXTRA_MINUTE);
            weekdays = savedInstanceState.getIntegerArrayList(EXTRA_WEEKDAYS);
        }
        else if(getArguments() != null){
            id = getArguments().getLong(EXTRA_ID, -1);
            if(id > -1){
                Cursor c = getActivity().getContentResolver().query(getUri(), null, String.format("%s=?", HabitProvider.ID), new String[]{String.valueOf(id)}, null);
                if(!c.moveToFirst()) throw new IllegalStateException("No habit group found");
                if(c.getCount() != 1) throw new IllegalStateException("Expected only one habit group");
                long time = c.getLong(c.getColumnIndex(HabitProvider.TIME));
                hour = TimeUtils.extractHours(time);
                minute = TimeUtils.extractMinutes(time);
                title = c.getString(c.getColumnIndex(HabitProvider.TITLE));
                c.close();

                weekdays = new ArrayList<>();
                c = getActivity().getContentResolver().query(HabitProvider.URI_REPEATING, null, HabitProvider.whereEquals(HabitProvider.HABIT_GROUP), HabitProvider.idArgs(id), null);
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
                    weekdays.add(c.getInt(c.getColumnIndex(HabitProvider.WEEKDAY)));
                c.close();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.habitgroupeditor, null);
        okButton = (Button) v.findViewById(R.id.habitgroupeditor_ok);
        deleteButton = (Button) v.findViewById(R.id.habitgroupeditor_delete);
        cancelButton = (Button) v.findViewById(R.id.habitgroupeditor_cancel);
        titleEditText = (EditText) v.findViewById(R.id.habitgroupedit_title);
        setTimeButton = (Button) v.findViewById(R.id.habitgroupedit_pickTime);
        pickWeekdaysButton = (Button) v.findViewById(R.id.habitgroupedit_pickWeekday);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleEditText.setText(title);

        deleteButton.setEnabled(id > -1);
        okButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        setTimeButton.setOnClickListener(this);
        pickWeekdaysButton.setOnClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            host = (HabitGroupEditorHost) activity;
        }
        catch (ClassCastException ignored){
            throw new IllegalStateException("Parent activity must implement proper interface");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_TITLE, titleEditText.getText().toString());
        outState.putLong(EXTRA_ID, id);
        outState.putInt(EXTRA_HOUR, hour);
        outState.putInt(EXTRA_MINUTE, minute);
        outState.putIntegerArrayList(EXTRA_WEEKDAYS, weekdays);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.habitgroupedit_pickWeekday:
                host.editorPickWeekdays(weekdays);
                return;
            case R.id.habitgroupedit_pickTime:
                host.editorPickTime(hour, minute);
                return;
            case R.id.habitgroupeditor_ok:
                ContentValues cv = new ContentValues();
                cv.put(HabitProvider.TITLE, titleEditText.getText().toString());
                cv.put(HabitProvider.TIME, hour * DateTimeConstants.MILLIS_PER_HOUR + minute * DateTimeConstants.MILLIS_PER_MINUTE);
                if(id == -1)
                    getActivity().getContentResolver().insert(getUri(), cv);
                else
                    getActivity().getContentResolver().update(getUri(), cv, String.format("%s=?", HabitProvider.ID), new String[]{String.valueOf(id)});
                break;
            case R.id.habitgroupeditor_delete:
                getActivity().getContentResolver().delete(getUri(), String.format("%s=?", HabitProvider.ID), new String[]{String.valueOf(id)});
        }
        host.onEditFinished();
    }

    public void onTimePicked(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public void onWeekdaysPicked(ArrayList<Integer> weekdays) {
        this.weekdays = weekdays;
    }

    public interface HabitGroupEditorHost{
        public void editorPickWeekdays(ArrayList<Integer> currentWeekdays);

        public void editorPickTime(int currentHour, int currentMinute);

        public void onEditFinished();
    }
}
