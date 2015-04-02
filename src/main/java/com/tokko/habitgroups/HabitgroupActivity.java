package com.tokko.habitgroups;

import android.app.Activity;
import android.os.Bundle;

public class HabitgroupActivity extends Activity implements HabitgroupListFragment.HabitGroupListFragmentHost{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new HabitgroupListFragment()).commit();
    }

    @Override
    public void editHabitGroup(long id) {

    }

    @Override
    public void addHabitGroup() {

    }
}
