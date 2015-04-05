package com.tokko.habitgroups;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.tokko.provider.HabitProvider;

public class HabitgroupListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String EXTRA_URI = "extra_uri";
    private Uri uri;
    private CursorAdapter adapter;
    private HabitGroupListFragmentHost host;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            uri = Uri.parse(savedInstanceState.getString(EXTRA_URI, ""));
        else if(getArguments() != null)
            uri = Uri.parse(getArguments().getString(EXTRA_URI, ""));
    }

    public static HabitgroupListFragment newInstance(Uri uri){
        Bundle b = new Bundle();
        b.putString(EXTRA_URI, uri.toString());
        HabitgroupListFragment f = new HabitgroupListFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, new String[]{HabitProvider.TITLE}, new int[]{android.R.id.text1}, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_URI, uri.toString());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = new CursorLoader(getActivity());
        cl.setUri(uri);
        return cl;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            host  = (HabitGroupListFragmentHost) activity;
        }
        catch (ClassCastException ignored){
            throw new IllegalStateException("Parent activity must inherit proper interface");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setListAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        host.editHabitGroup(id);
    }

    public interface HabitGroupListFragmentHost{

        public void editHabitGroup(long id);

        public void addHabitGroup();
    }

}
