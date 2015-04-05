package com.tokko.config;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.tokko.dialogs.TimePickerDialogFragment;
import com.tokko.dialogs.WeekdayPickerDialogFragment;

import java.util.ArrayList;

public class EditorActivity extends FragmentActivity implements HabitgroupEditorFragment.HabitGroupEditorHost, TimePickerDialogFragment.TimePickerDialogCallbacks, WeekdayPickerDialogFragment.WeekdayPickerCallbacks {
    private static final String TAG_EDITOR_FRAGMENT = "editor_fragment";
    private static final String EXTRA_ID = "extra_id";
    private static final String ACTION_EDIT_GROUP = "ACTION_EDIT_GROUP";
    private static final String ACTION_EDIT_HABIT = "ACTION_EDIT_HABIT";

    private HabitgroupEditorFragment editorFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            editorFragment = (HabitgroupEditorFragment) getSupportFragmentManager().getFragment(savedInstanceState, TAG_EDITOR_FRAGMENT);
        else if(getIntent().getExtras() != null)
            if(getIntent().getAction().equals(ACTION_EDIT_GROUP))
                editorFragment = HabitgroupEditorFragment.newInstance(getIntent().getLongExtra(EXTRA_ID, -1));
            else if(getIntent().getAction().equals(ACTION_EDIT_HABIT))
                editorFragment = HabitEditorFragment.newInstance(getIntent().getLongExtra(EXTRA_ID, -1));
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, editorFragment).commit();
    }

    private static Intent getStartIntent(Context context, String action, long id){
        return new Intent(context, EditorActivity.class).setAction(action).putExtra(EXTRA_ID, id);
    }

    public static Intent getEditGroupIntent(Context context, long id){
        return getStartIntent(context, ACTION_EDIT_GROUP, id);
    }

    public static Intent getEditHabitIntent(Context context, long id){
        return getStartIntent(context, ACTION_EDIT_HABIT, id);
    }

    public static Intent getAddGroupIntent(Context context){
        return getEditGroupIntent(context, -1);
    }

    public static Intent getAddHabitIntent(Context context){
        return getEditHabitIntent(context, -1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(editorFragment != null)
            getSupportFragmentManager().putFragment(outState, TAG_EDITOR_FRAGMENT, editorFragment);
    }
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void editorPickWeekdays(ArrayList<Integer> currentWeekdays) {
        WeekdayPickerDialogFragment.newInstance(currentWeekdays).show(getSupportFragmentManager(), "t");
    }

    @Override
    public void editorPickTime(int currentHour, int currentMinute) {
        TimePickerDialogFragment.newInstance(0, currentHour, currentMinute).show(getSupportFragmentManager(), "tag");
    }

    @Override
    public void onEditFinished() {
        onBackPressed();
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
