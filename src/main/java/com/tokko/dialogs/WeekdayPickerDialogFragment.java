package com.tokko.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.tokko.R;

import java.util.ArrayList;
import java.util.List;

public class WeekdayPickerDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String EXTRA_CURRENT = "extra_current";

    private Button okButton;
    private Button cancelButton;
    private ListView list;

    private ArrayAdapter<String> adapter;
    private WeekdayPickerCallbacks host;


    public static DialogFragment newInstance(ArrayList<Integer> currentWeekdays) {
        Bundle b = new Bundle();
        b.putIntegerArrayList(EXTRA_CURRENT, currentWeekdays);
        WeekdayPickerDialogFragment f = new WeekdayPickerDialogFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, getResources().getStringArray(R.array.weekdays));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.weekdaypickerdialogfragment, null);
        cancelButton = (Button) v.findViewById(R.id.weekdaypicker_cancel);
        okButton = (Button) v.findViewById(R.id.weekdaypicker_ok);
        list = (ListView) v.findViewById(android.R.id.list);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        cancelButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        list.setAdapter(adapter);

        ArrayList<Integer> current = new ArrayList<>();
        if(savedInstanceState != null)
            current = savedInstanceState.getIntegerArrayList(EXTRA_CURRENT);
        else if(getArguments() != null)
            current = getArguments().getIntegerArrayList(EXTRA_CURRENT);
        for (int i = 0; i < current.size(); i++) {
            list.setItemChecked(current.get(i)-1, true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SparseBooleanArray checked = list.getCheckedItemPositions();
        ArrayList<Integer> checkedIds = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
             if(checked.get(i)) checkedIds.add(i+1);
        }
        outState.putIntegerArrayList(EXTRA_CURRENT, checkedIds);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.weekdaypicker_ok:
               ArrayList<Integer> weekdays = new ArrayList<>();
                for (int i = 0; i < list.getCount(); i++) {
                     if(list.isItemChecked(i)) weekdays.add(i+1);
                }
                host.onWeekdayPicked(weekdays);
            case R.id.weekdaypicker_cancel:
                dismiss();
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            host = (WeekdayPickerCallbacks) activity;
        }
        catch (ClassCastException ignored){
            throw new IllegalStateException("Parent activity must implement proper interface");
        }
    }

    public interface WeekdayPickerCallbacks{

        public void onWeekdayPicked(ArrayList<Integer> weekdays);
    }
}
