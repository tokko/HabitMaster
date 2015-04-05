package com.tokko.habits;

import com.tokko.config.HabitEditorFragment;
import com.tokko.config.ConfigActivity;
import com.tokko.config.HabitgroupEditorFragment;
import com.tokko.config.HabitgroupListFragment;
import com.tokko.provider.HabitProvider;

public class HabitActivity extends ConfigActivity {

    @Override
    protected HabitgroupEditorFragment getEditorFragment(long id) {
        return HabitEditorFragment.newInstance(id);
    }

    @Override
    protected HabitgroupListFragment getListFragment() {
        return HabitgroupListFragment.newInstance(HabitProvider.URI_HABITS);
    }
}
