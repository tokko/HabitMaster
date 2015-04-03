package com.tokko.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

public class TimePickerDialogFragment extends DialogFragment implements android.app.TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_HOUR = "extra_hour";
    public static final String EXTRA_MINUTE = "extra_minute";

    int id;
    private TimePickerDialogCallbacks host;

    public static TimePickerDialogFragment newInstance(int id, int currentHour, int currentMinute){
        Bundle b = new Bundle();
        b.putInt(EXTRA_ID, id);
        b.putInt(EXTRA_HOUR, currentHour);
        b.putInt(EXTRA_MINUTE, currentMinute);
        TimePickerDialogFragment f = new TimePickerDialogFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = 0;
        int minute = 0;
        if(savedInstanceState != null){
            id = savedInstanceState.getInt(EXTRA_ID);
        }
        else if(getArguments() != null){
            id = getArguments().getInt(EXTRA_ID);
            hour = getArguments().getInt(EXTRA_HOUR);
            minute = getArguments().getInt(EXTRA_MINUTE);
        }
        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_ID, id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            host = (TimePickerDialogCallbacks) activity;
        }
        catch (ClassCastException ignored){
            throw new IllegalStateException("Host activity must implement proper interface");
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i2) {
        host.onTimeSet(id, i, i2);
    }

    public interface TimePickerDialogCallbacks{
        public void onTimeSet(int id, int hour, int minute);
    }
}
