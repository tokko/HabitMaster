package com.tokko.config;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.tokko.R;
import com.tokko.Util.TimeUtils;
import com.tokko.provider.HabitProvider;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;

public class HabitgroupEditorFragment extends ListFragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    protected static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_HOUR = "extra_hour";
    private static final String EXTRA_MINUTE = "extra_minute";
    private static final String EXTRA_WEEKDAYS = "extra_weekdays";
    private static final String EXTRA_CHECKED = "extra_checked";

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

    private CursorAdapter adapter;

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
        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice, null, new String[]{HabitProvider.TITLE}, new int[]{android.R.id.text1}, 0);
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

        setListAdapter(adapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        if(savedInstanceState != null)
            setItemsChecked(savedInstanceState.getIntegerArrayList(EXTRA_CHECKED));
    }

    private void setItemsChecked(ArrayList<Integer> checked){
        for (Integer integer : checked)
            getListView().setItemChecked(integer, true);
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
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_TITLE, titleEditText.getText().toString());
        outState.putLong(EXTRA_ID, id);
        outState.putInt(EXTRA_HOUR, hour);
        outState.putInt(EXTRA_MINUTE, minute);
        outState.putIntegerArrayList(EXTRA_WEEKDAYS, weekdays);
        outState.putIntegerArrayList(EXTRA_CHECKED, getCheckedItems());
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
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                ContentProviderOperation.Builder opb;
                if(id == -1) {
                    opb = ContentProviderOperation.newInsert(getUri());
                }
                else {
                    opb = ContentProviderOperation.newUpdate(getUri());
                    opb.withSelection(HabitProvider.whereID(), HabitProvider.idArgs(id));
                }
                opb.withValue(HabitProvider.TITLE, titleEditText.getText().toString());
                opb.withValue(HabitProvider.TIME, hour * DateTimeConstants.MILLIS_PER_HOUR + minute * DateTimeConstants.MILLIS_PER_MINUTE);
                ops.add(opb.build());
                persistWeekdays(ops);
                persistHabits(ops);
                try {
                    getActivity().getContentResolver().applyBatch(HabitProvider.AUTHORITY, ops);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                    throw new IllegalStateException("FUCK!");
                }
                break;
            case R.id.habitgroupeditor_delete:
                getActivity().getContentResolver().delete(HabitProvider.URI_HABIT_GROUPS, String.format("%s=?", HabitProvider.ID), new String[]{String.valueOf(id)});
        }
        host.onEditFinished();
    }

    protected void persistHabits(ArrayList<ContentProviderOperation> ops) {
        ops.add(ContentProviderOperation.newDelete(HabitProvider.URI_HABITS_IN_GROUP).withSelection(HabitProvider.whereEquals(HabitProvider.HABIT_GROUP), HabitProvider.idArgs(id)).build());
        for(Integer integer : getCheckedItems())
                ops.add(ContentProviderOperation.newInsert(HabitProvider.URI_HABITS_IN_GROUP).withValue(HabitProvider.HABIT_GROUP, id).withValue(HabitProvider.HABIT, getListView().getItemIdAtPosition(integer)).build());
        }

    private ArrayList<Integer> getCheckedItems() {
        ArrayList<Integer> checked = new ArrayList<>();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (getListView().isItemChecked(i))
                checked.add(i);
        }
        return checked;
    }

    protected void persistWeekdays(ArrayList<ContentProviderOperation> ops){
        ops.add(ContentProviderOperation.newDelete(HabitProvider.URI_REPEATING).withSelection(HabitProvider.whereEquals(HabitProvider.HABIT_GROUP), HabitProvider.idArgs(id)).build());
        for (int i = 0; i < weekdays.size(); i++) {
            ops.add(ContentProviderOperation.newInsert(HabitProvider.URI_REPEATING).withValue(HabitProvider.HABIT_GROUP, id).withValue(HabitProvider.WEEKDAY, weekdays.get(i)).build());
        }
    }

    public void onTimePicked(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public void onWeekdaysPicked(ArrayList<Integer> weekdays) {
        this.weekdays = weekdays;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cl = new CursorLoader(getActivity());
        cl.setUri(HabitProvider.URI_HABITS);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }



public interface HabitGroupEditorHost{
        public void editorPickWeekdays(ArrayList<Integer> currentWeekdays);

        public void editorPickTime(int currentHour, int currentMinute);

        public void onEditFinished();
    }
}
