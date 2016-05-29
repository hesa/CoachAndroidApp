package com.sandklef.coachapp.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.sandklef.coachapp.misc.CADateFormat;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Club;
import com.sandklef.coachapp.model.LogMessage;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.storage.Storage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import coachassistant.sandklef.com.coachapp.R;

public class LogActivity extends ActionBarActivity implements AbsListView.OnItemClickListener {

    private final static String LOG_TAG = LogMessage.class.getSimpleName();

    private ListAdapter      mAdapter;
    private AbsListView      mListView;
    private List<LogMessage> logs;
    private Club             currentClub;
    private Toolbar          toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
//        logStrings = new ArrayList<>();

        //List<LogMessage> logs;
//        Storage.getInstance().log("onCreate in LogMessage");

        logs = Storage.getInstance().getLogMessages();
/*        String lastDayString = null;


        for (LogMessage l: logs) {
            Date ld = l.getDate();
            String dayString = CADateFormat.getDayString(ld);

            Log.d(LOG_TAG, " d: " + ld);

            if (lastDayString==null || (!lastDayString.equals(dayString))) {
                logStrings.add(dayString);
            }
            logStrings.add(l.toString());

            lastDayString = dayString;
        }
*/

        mAdapter = new ArrayAdapter<LogMessage>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, logs);


        //        public MediaListAdapter(Context context, int textViewResourceId, List<Media> media)
/*
        mAdapter = new MediaListAdapter(getApplicationContext(),
                R.layout.media_list, media);
*/
        // Set the adapter
        mListView = (AbsListView) findViewById(R.id.local_log_list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        registerForContextMenu(mListView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getTitle());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log_media_menu, menu);

        mListView.setOnItemClickListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "  onOptionsItemSelected: " + item.getItemId());
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_training_mode:
                ActivitySwitcher.startTrainingActivity(this);
                return true;
            case R.id.log_menu_localmedia:
                ActivitySwitcher.startLocalMediaManager(this);
            default:
                Log.d(LOG_TAG, "  doin nada");
                return true;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOG_TAG, " log clicked");
        Log.d(LOG_TAG, " detail:" + logs.get((int)id).getDetail());
    }
}
