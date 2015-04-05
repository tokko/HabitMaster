package com.tokko.habits;

import com.tokko.habitgroups.HabitgroupActivity;
import com.tokko.habitgroups.HabitgroupEditor;
import com.tokko.habitgroups.HabitgroupListFragment;
import com.tokko.provider.HabitProvider;

public class HabitActivity extends HabitgroupActivity {

    @Override
    protected HabitgroupEditor getEditorFragment(long id) {
        return HabitEditor.newInstance(id);
    }

    @Override
    protected HabitgroupListFragment getListFragment() {
        return HabitgroupListFragment.newInstance(HabitProvider.URI_HABITS);
    }
}
