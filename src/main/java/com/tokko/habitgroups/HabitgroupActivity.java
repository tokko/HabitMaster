package com.tokko.habitgroups;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.PersistableBundle;


public class HabitgroupActivity extends Activity implements HabitgroupListFragment.HabitGroupListFragmentHost, HabitgroupEditor.HabitGroupEditorHost{
    private static final String TAG_EDITOR_FRAGMENT = "editor_fragment";
    private Fragment editorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new HabitgroupListFragment()).commit();
        if(savedInstanceState != null){
            editorFragment = getFragmentManager().getFragment(savedInstanceState, TAG_EDITOR_FRAGMENT);
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
    public void onEditFinished() {
        getFragmentManager().popBackStack();
    }
}
