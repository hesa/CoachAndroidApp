package com.sandklef.coachapp.activities;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sandklef.coachapp.json.JsonAccess;
import com.sandklef.coachapp.json.JsonAccessException;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Club;
import com.sandklef.coachapp.model.Team;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.LocalStorageSync;
import com.sandklef.coachapp.storage.Storage;
import com.sandklef.coachapp.storage.StorageNoClubException;

import java.util.ArrayList;

import coachassistant.sandklef.com.coachapp.R;

public class TeamsActivity extends AppCompatActivity implements AbsListView.OnItemClickListener {


//    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    private ListView mListView;
    private ArrayAdapter mAdapter;

    private final static String LOG_TAG = TeamsActivity.class.getSimpleName();
//    private Club currentClub;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);

        ActivitySwitcher.printDb("TeamsActivity");

        try {

            Log.d(LOG_TAG, "onCreate()  storage:" + Storage.getInstance().getTeams().size() + " teams");

            mAdapter = new ArrayAdapter<Team>(this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    Storage.getInstance().getTeams());
            Log.d(LOG_TAG, "onCreate()  adapter:" + mAdapter);

            updateFromServer();

            Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
//        myToolbar.setTitle(getString(R.string.team_list_header));
            getSupportActionBar().setTitle(getResources().getString(R.string.team_list_header));


/*        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        */
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }

    }

    public void updateFromServer() {
        Log.d(LOG_TAG, "Initiate update from server");
        Storage.getInstance().update(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);


        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(LOG_TAG, "onStart()");

        // Set the adapter
        mListView = (ListView) findViewById(R.id.team_list);

        Log.d(LOG_TAG, "onStart() listview: " + mListView);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Team t = Storage.getInstance().getTeams().get((int) id);
            Log.d(LOG_TAG, " team clicked: " + t.getUuid() + "  " + t);

            LocalStorage.getInstance().setCurrentTeam(t.getUuid());
            ActivitySwitcher.startTrainingPhaseActivity(this);
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
    }



    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "  onOptionsItemSelected: " + item.getItemId());
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_log_file:
                Log.d(LOG_TAG, "  log");
                ActivitySwitcher.startLogMessageActivity(this);
                return true;
            case R.id.menu_media_manager:
                Log.d(LOG_TAG, "  media");
                ActivitySwitcher.startLocalMediaManager(this);
                return true;
            case R.id.menu_club_info:
                Log.d(LOG_TAG, "  club");
                ActivitySwitcher.startClubInfoActivity(this);
                return true;
            case R.id.menu_refresh:
                Log.d(LOG_TAG, "  refresh");
                mListView.setAdapter(null);
                mAdapter.clear();
                try {
                    mAdapter = new ArrayAdapter<Team>(this,
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            Storage.getInstance().getTeams());
                    mListView.setAdapter(mAdapter);
                } catch (StorageNoClubException e) {
                    e.printStackTrace();
                }


                return true;

            default:
                Log.d(LOG_TAG, "  doin nada");
                return true;
        }
    }

}