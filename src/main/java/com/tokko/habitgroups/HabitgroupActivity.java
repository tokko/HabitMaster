package com.tokko.habitgroups;

import android.app.Activity;
import android.os.Bundle;

import com.tokko.dialogs.TimePickerDialogFragment;


public class HabitgroupActivity extends Activity implements HabitgroupListFragment.HabitGroupListFragmentHost, HabitgroupEditor.HabitGroupEditorHost, TimePickerDialogFragment.TimePickerDialogCallbacks{
    private static final String TAG_EDITOR_FRAGMENT = "editor_fragment";
    private HabitgroupEditor editorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new HabitgroupListFragment()).commit();
        if(savedInstanceState != null){
            editorFragment = (HabitgroupEditor) getFragmentManager().getFragment(savedInstanceState, TAG_EDITOR_FRAGMENT);
            if(editorFragment != null)
                getFragmentManager().beginTransaction().replace(android.R.id.content, editorFragment).commit();
        }
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

    private void showEditorFragment(long id){
        editorFragment = HabitgroupEditor.newInstance(id);
        getFragmentManager().beginTransaction().addToBackStack(TAG_EDITOR_FRAGMENT).replace(android.R.id.content, editorFragment).commit();
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
}
