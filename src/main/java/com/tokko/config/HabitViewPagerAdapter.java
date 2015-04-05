package com.tokko.config;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tokko.provider.HabitProvider;

public class HabitViewPagerAdapter extends FragmentStatePagerAdapter {


    private final CharSequence[] titles;
    private HabitMasterListFragment.HabitMasterListFragmentCallbacks[] callbacks;

    public HabitViewPagerAdapter(FragmentManager fm, CharSequence[] titles, HabitMasterListFragment.HabitMasterListFragmentCallbacks[] habitMasterListFragmentCallbackses) {
        super(fm);
        this.titles = titles;
        this.callbacks = habitMasterListFragmentCallbackses;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return HabitMasterListFragment.newInstance(HabitProvider.URI_HABIT_GROUPS).setCallbacks(callbacks[0]);
            case 1:
                return HabitMasterListFragment.newInstance(HabitProvider.URI_HABITS).setCallbacks(callbacks[1]);
            default:
                throw new IllegalArgumentException("No fragment at index: " + position);
        }
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return titles.length;
    }
}
