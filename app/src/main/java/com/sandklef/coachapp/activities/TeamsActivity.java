package com.sandklef.coachapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.sandklef.coachapp.Session.CoachAppSession;
import com.sandklef.coachapp.json.JsonAccessException;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Team;
import com.sandklef.coachapp.storage.ConnectionStatusListener;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.Storage;
import com.sandklef.coachapp.storage.StorageNoClubException;
import com.sandklef.coachapp.storage.StorageUpdateListener;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;

import coachassistant.sandklef.com.coachapp.R;

public class TeamsActivity
        extends AppCompatActivity
        implements AbsListView.OnItemClickListener, StorageUpdateListener {


    //    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    private ListView mListView;
    private ArrayAdapter mAdapter;

    private final static String LOG_TAG = TeamsActivity.class.getSimpleName();

    private int backPressCounter;

    //    private Club currentClub;

    @Override
    public void onBackPressed() {
        if (backPressCounter>0) {
            Log.d(LOG_TAG, "onBackPressed(), will finish");
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);

        } else {
            Log.d(LOG_TAG, "onBackPressed(), ignoring back press");
        }
        backPressCounter++;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);

        if (CoachAppSession.getInstance() == null) {
            ActivitySwitcher.startLoginActivity(this);
        }
        CoachAppSession.getInstance().setupActivity(this);

        //        Log.d(LOG_TAG, "video length: " + LocalStorage.getInstance().getVideoRecordingTime());
//        ActivitySwitcher.printDb("TeamsActivity");

        Log.d(LOG_TAG, "orientation: " + CoachAppSession.getInstance().getScreenOrientation());

        try {

            Log.d(LOG_TAG, "onCreate()  storage:" + Storage.getInstance().getTeams().size() + " teams");

            mAdapter = new ArrayAdapter<Team>(this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    Storage.getInstance().getTeams());
            Log.d(LOG_TAG, "onCreate()  adapter:" + mAdapter);

//            CoachAppSession.getInstance().updateFromServer(this, this, CoachAppSession.getInstance());

            Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
//        myToolbar.setTitle(getString(R.string.team_list_header));
            getSupportActionBar().setTitle(getResources().getString(R.string.team_list_header));
//            getSupportActionBar().setIcon(android.R.drawable.arrow_up_float);


/*        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        */


        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
        backPressCounter=0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
        if (CoachAppSession.getInstance() == null) {
            ActivitySwitcher.startLoginActivity(this);
        }
        backPressCounter=0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);

        Log.d(LOG_TAG, " find menu: " + menu);

        CoachAppSession.getInstance().setupActivity(this, menu, R.id.topsync);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        Log.d(LOG_TAG, "onStart()");
        Log.d(LOG_TAG, "onStart() " + LocalStorage.getInstance().getCurrentClub());
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
            Log.d(LOG_TAG, " team clicked: " + id);
            Team t = Storage.getInstance().getTeams().get((int) id);
            Log.d(LOG_TAG, " team clicked: " + t.getUuid() + "  " + t);

            LocalStorage.getInstance().setCurrentTeam(t.getUuid());
            ActivitySwitcher.startTrainingPhaseActivity(this);
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        return CoachAppSession.getInstance().handleTopMenu(item, this);
    }


    @Override
    public void onStorageUpdate() {
        Log.d(LOG_TAG, "onStorageUpdate()");
        try {
            List<Team> teams = Storage.getInstance().getTeams();
            Log.d(LOG_TAG, "refresh()  teams: " + teams.size());
            for (Team t : Storage.getInstance().getTeams()) {
                Log.d(LOG_TAG, " * " + t.getName() + " " + t.getUuid());
            }

            mListView.setAdapter(null);
            mAdapter.clear();
            mAdapter = new ArrayAdapter<Team>(this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    teams);
            mListView.setAdapter(mAdapter);
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
