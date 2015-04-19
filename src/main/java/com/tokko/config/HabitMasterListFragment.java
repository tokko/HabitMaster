package com.tokko.config;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.tokko.R;
import com.tokko.provider.HabitProvider;

public class HabitMasterListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String EXTRA_URI = "extra_uri";
    private Uri uri;
    private CursorAdapter adapter;
    private HabitMasterListFragmentCallbacks host;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            uri = Uri.parse(savedInstanceState.getString(EXTRA_URI, ""));
        else if(getArguments() != null)
            uri = Uri.parse(getArguments().getString(EXTRA_URI, ""));
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.editor_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.list_add:
                host.onAddListItem();
                return true;
           case R.id.clear:
               getActivity().getContentResolver().call(HabitProvider.URI_HABIT_GROUPS, "clear", null, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static HabitMasterListFragment newInstance(Uri uri){
        Bundle b = new Bundle();
        b.putString(EXTRA_URI, uri.toString());
        HabitMasterListFragment f = new HabitMasterListFragment();
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

    public HabitMasterListFragment setCallbacks(HabitMasterListFragmentCallbacks callbacks){
        host = callbacks;
        return this;
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
        host.onListItemEdit(id);
    }

    public interface HabitMasterListFragmentCallbacks {

        void onListItemEdit(long id);

        void onAddListItem();
    }

}
