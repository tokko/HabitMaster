package com.tokko.config;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.tokko.R;
import com.tokko.slidingtabs.SlidingTabLayout;

public class ConfigActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);

        HabitViewPagerAdapter adapter = new HabitViewPagerAdapter(getSupportFragmentManager(), new CharSequence[]{"Habitgroups", "Habits"}, new HabitMasterListFragment.HabitMasterListFragmentCallbacks[]{new HabitGroupCallbacks(), new HabitCallbacks()});

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        SlidingTabLayout tabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        tabLayout.setDistributeEvenly(true);
        tabLayout.setViewPager(pager);
    }


    private class HabitGroupCallbacks implements HabitMasterListFragment.HabitMasterListFragmentCallbacks{

        @Override
        public void onListItemEdit(long id) {
            startActivity(EditorActivity.getEditGroupIntent(ConfigActivity.this, id));
        }

        @Override
        public void onAddListItem() {
            startActivity(EditorActivity.getAddGroupIntent(ConfigActivity.this));
        }
    }

    private class HabitCallbacks implements HabitMasterListFragment.HabitMasterListFragmentCallbacks{

        @Override
        public void onListItemEdit(long id) {
            startActivity(EditorActivity.getEditHabitIntent(ConfigActivity.this, id));
        }

        @Override
        public void onAddListItem() {
            startActivity(EditorActivity.getAddHabitIntent(ConfigActivity.this));
        }
    }

}
