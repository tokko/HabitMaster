package com.tokko.config;

import android.app.Activity;
import android.os.Bundle;

import com.tokko.dialogs.TimePickerDialogFragment;
import com.tokko.dialogs.WeekdayPickerDialogFragment;
import com.tokko.provider.HabitProvider;

import java.util.ArrayList;


public class ConfigActivity extends Activity implements HabitgroupListFragment.HabitGroupListFragmentHost, HabitgroupEditorFragment.HabitGroupEditorHost, TimePickerDialogFragment.TimePickerDialogCallbacks, WeekdayPickerDialogFragment.WeekdayPickerCallbacks{
    private static final String TAG_EDITOR_FRAGMENT = "editor_fragment";
    private HabitgroupEditorFragment editorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, getListFragment()).commit();
        if(savedInstanceState != null){
            editorFragment = (HabitgroupEditorFragment) getFragmentManager().getFragment(savedInstanceState, TAG_EDITOR_FRAGMENT);
            if(editorFragment != null)
                getFragmentManager().beginTransaction().replace(android.R.id.content, editorFragment).commit();
        }
    }

    protected HabitgroupListFragment getListFragment(){
        return HabitgroupListFragment.newInstance(HabitProvider.URI_HABIT_GROUPS);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(editorFragment != null)
            getFragmentManager().putFragment(outState, TAG_EDITOR_FRAGMENT, editorFragment);
    }

    @Override
    public void editHabitGroup(long id) {
        showEditorFragment(id);
    }

    @Override
    public void addHabitGroup() {
        showEditorFragment(-1);
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            editorFragment = null;
        }
        else
            finish();
    }

    protected HabitgroupEditorFragment getEditorFragment(long id){
        return HabitgroupEditorFragment.newInstance(id);
    }

    private void showEditorFragment(long id){
        editorFragment = getEditorFragment(id);
        getFragmentManager().beginTransaction().addToBackStack(TAG_EDITOR_FRAGMENT).replace(android.R.id.content, editorFragment).commit();
    }

    @Override
    public void editorPickWeekdays(ArrayList<Integer> currentWeekdays) {
        WeekdayPickerDialogFragment.newInstance(currentWeekdays).show(getFragmentManager(), "t");
    }

    @Override
    public void editorPickTime(int currentHour, int currentMinute) {
        TimePickerDialogFragment.newInstance(0, currentHour, currentMinute).show(getFragmentManager(), "tag");
    }

    @Override
    public void onEditFinished() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onTimeSet(int id, int hour, int minute) {
        if(editorFragment != null)
            editorFragment.onTimePicked(hour, minute);
    }

    @Override
    public void onWeekdayPicked(ArrayList<Integer> weekdays) {
        if(editorFragment != null)
            editorFragment.onWeekdaysPicked(weekdays);
    }
}
