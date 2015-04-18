package com.tokko.config;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tokko.R;
import com.tokko.Util.TimeUtils;
import com.tokko.provider.HabitProvider;
import com.tokko.provider.HabitWithConnection;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;

public class HabitgroupEditorFragment extends ListFragment implements View.OnClickListener {
    protected static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_HOUR = "extra_hour";
    private static final String EXTRA_MINUTE = "extra_minute";
    private static final String EXTRA_WEEKDAYS = "extra_weekdays";
    private static final String EXTRA_ITEMS = "extra_items";

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
    private ArrayList<Integer> weekdays = new ArrayList<>();
    private String title;

    private ArrayAdapter<HabitWithConnection> adapter;

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

        setListAdapter(adapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayList<HabitWithConnection> data;
        if(savedInstanceState != null) {
            data = savedInstanceState.getParcelableArrayList(EXTRA_ITEMS);
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, data);
        }
        else{
            Cursor cursor = getActivity().getContentResolver().query(HabitProvider.URI_HABITS_WITH_CONNECTION, null, null, HabitProvider.idArgs(id), null);
            data = HabitWithConnection.fromCursor(cursor);
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, data);
        }
        setListAdapter(adapter);
        setItemsChecked(data);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if(getListView().isItemChecked(position)) adapter.getItem(position).habitGroup = this.id;
        else adapter.getItem(position).habitGroup = null;
    }

    private void setItemsChecked(ArrayList<HabitWithConnection> data){
        for (int i = 0; i<data.size(); i++)
            getListView().setItemChecked(i, data.get(i).habitGroup != null);
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
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_TITLE, titleEditText.getText().toString());
        outState.putLong(EXTRA_ID, id);
        outState.putInt(EXTRA_HOUR, hour);
        outState.putInt(EXTRA_MINUTE, minute);
        outState.putIntegerArrayList(EXTRA_WEEKDAYS, weekdays);
        outState.putParcelableArrayList(EXTRA_ITEMS, getItems());
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
        ArrayList<HabitWithConnection> data = getItems();
        ops.addAll(HabitWithConnection.toInsertOperations(data));
    }

    private ArrayList<HabitWithConnection> getItems() {
        ArrayList<HabitWithConnection> items = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            items.add(adapter.getItem(i));
        }
        return items;
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

public interface HabitGroupEditorHost{
        public void editorPickWeekdays(ArrayList<Integer> currentWeekdays);

        public void editorPickTime(int currentHour, int currentMinute);

        public void onEditFinished();
    }
}
