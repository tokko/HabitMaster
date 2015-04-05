package com.tokko.config;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.tokko.R;
import com.tokko.provider.HabitProvider;

public class HabitEditorFragment extends HabitgroupEditorFragment {

    public static HabitEditorFragment newInstance(long id){
        Bundle b = new Bundle();
        b.putLong(EXTRA_ID, id);
        HabitEditorFragment f = new HabitEditorFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((Button)view.findViewById(R.id.habitgroupedit_pickTime)).setText("Set duration");
        view.findViewById(R.id.habitgroupedit_pickWeekday).setVisibility(View.GONE);
    }

    @Override
    protected Uri getUri() {
        return HabitProvider.URI_HABITS;
    }
}
