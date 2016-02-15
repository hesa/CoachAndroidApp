package com.sandklef.coachapp.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sandklef.coachapp.filters.BaseFilter;
import com.sandklef.coachapp.filters.MediaFilter;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Base;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.storage.Storage;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import coachassistant.sandklef.com.coachapp.R;


public class LocalMediaManager extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final static String LOG_TAG = Member.class.getSimpleName();

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;
    //private MediaListAdapter mAdapter;
    private Toolbar toolbar;
    private List<Media> media;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_media_manager);

        Storage.newInstance(this);

        media = Storage.getInstance().getMedia();

        mAdapter = new ArrayAdapter<Media>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, media);


        //        public MediaListAdapter(Context context, int textViewResourceId, List<Media> media)
/*
        mAdapter = new MediaListAdapter(getApplicationContext(),
                R.layout.media_list, media);
*/
        // Set the adapter
        mListView = (AbsListView) findViewById(R.id.local_media_list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        registerForContextMenu(mListView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getTitle());
        }
    }

    private static final String CONTEXT_MENU_EDIT    = "Edit";
    private static final String CONTEXT_MENU_WATCH   = "Watch";
    private static final String CONTEXT_MENU_DELETE  = "Delete";
    private static final String CONTEXT_MENU_UPLOAD  = "Upload";

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        String selectedWord = ((TextView) info.targetView).getText().toString();
        long selectedWordId = info.id;

        Log.d(LOG_TAG, "  Add context for v: " + v);
        Log.d(LOG_TAG, "   * " + v.getId());
        Log.d(LOG_TAG, "   * " + selectedWordId);
        menu.setHeaderTitle("Select");
        menu.add(0, v.getId(), 0, CONTEXT_MENU_EDIT);
        menu.add(0, v.getId(), 0, CONTEXT_MENU_DELETE);
        menu.add(0, v.getId(), 0, CONTEXT_MENU_WATCH);
//        menu.add(0, v.getId(), 0, CONTEXT_MENU_UPLOAD);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle()==CONTEXT_MENU_EDIT){
            Toast.makeText(getApplicationContext(), "Choice: " + CONTEXT_MENU_EDIT, Toast.LENGTH_LONG).show();
        } else if(item.getTitle()==CONTEXT_MENU_DELETE){
            Toast.makeText(getApplicationContext(), "Choice: "  + CONTEXT_MENU_DELETE, Toast.LENGTH_LONG).show();
        } else if(item.getTitle()==CONTEXT_MENU_WATCH){
            Toast.makeText(getApplicationContext(), "Choice: "  + CONTEXT_MENU_WATCH, Toast.LENGTH_LONG).show();
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int position = acmi.position;
            String file = media.get(position).fileName();

            Log.d(LOG_TAG, "   video item: " + media.get(position).fileName() + ", " + media.get(position).getUuid() + ", " + media.get(position).getClubUuid() + ", ");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(media.get(position).fileName())), "video/*");
            startActivity(intent);
        }else{
            return false;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Media m = media.get((int) id);
            Log.d(LOG_TAG, " media clicked: " + m.getUuid() + "  " + m);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.local_media_menu, menu);
        return true;
    }

    private void showTrainingMode() {
        Intent intent = new Intent(this, com.sandklef.coachapp.activities.TopActivity.class);
        startActivity(intent);
    }

    private void filteredUpdatedList(MediaFilter mf) {
        Log.d(LOG_TAG, "  filter " + mf + "on deletable media " + media.size());
        media = mf .apply(Storage.getInstance().getMedia());
        Log.d(LOG_TAG, "  filter " + mf + "on deletable media " + media.size());
        ArrayAdapter<Media> ma = ((ArrayAdapter) mAdapter);
        ma.clear();
        ma.addAll(media);
        ma.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "  onOptionsItemSelected");
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_training:
                showTrainingMode();
                return true;
            case R.id.menu_deletable_media:
                filteredUpdatedList(MediaFilter.newMediaFilterStatus(Media.MEDIA_STATUS_DELETABLE));
                return true;
            case R.id.menu_all_media:
                filteredUpdatedList(new MediaFilter());
                return true;
            case R.id.menu_new_media:
                filteredUpdatedList(MediaFilter.newMediaFilterStatus(Media.MEDIA_STATUS_NEW));
                return true;
            default:
                Log.d(LOG_TAG, "  doin nada");
                return true;
        }
    }


    private class MediaListAdapter extends ArrayAdapter {

        private Context mContext;
        private int id;
        private List<Media> media;

        public MediaListAdapter(Context context, int textViewResourceId, List<Media> media)
        {
            super(context, textViewResourceId, media );
            mContext = context;
            id = textViewResourceId;
            this.media = media ;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent)
        {
            View mView = v ;

            if(mView == null){
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = vi.inflate(id, null);
            }

            TextView text       = (TextView) mView.findViewById(R.id.media_list_text);

            if( media.size() > position && media.get(position) != null ) {
                text.setText(media.get(position).toString() );
            }
            return mView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}

