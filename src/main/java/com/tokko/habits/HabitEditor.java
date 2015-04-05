package com.tokko.habits;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.tokko.R;
import com.tokko.habitgroups.HabitgroupEditor;
import com.tokko.provider.HabitProvider;

public class HabitEditor extends HabitgroupEditor {

    public static HabitEditor newInstance(long id){
        Bundle b = new Bundle();
        b.putLong(EXTRA_ID, id);
        HabitEditor f = new HabitEditor();
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
