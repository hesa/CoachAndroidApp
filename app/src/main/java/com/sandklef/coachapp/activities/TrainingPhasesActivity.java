package com.sandklef.coachapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sandklef.coachapp.Session.CoachAppSession;
import com.sandklef.coachapp.fragments.VideoCapture;
import com.sandklef.coachapp.json.JsonSettings;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Team;
import com.sandklef.coachapp.model.TrainingPhase;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.Storage;
import com.sandklef.coachapp.storage.StorageNoClubException;

import java.io.File;
import java.util.ArrayList;

import coachassistant.sandklef.com.coachapp.R;

public class TrainingPhasesActivity extends ActionBarActivity implements AbsListView.OnItemClickListener{

    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    private AbsListView mListView;
    private ListAdapter mAdapter;
    private final static String LOG_TAG = TrainingPhasesActivity.class.getSimpleName();

    private String currentTPId = null;

    @Override
    public void onBackPressed(){
        Log.d(LOG_TAG, "onBackPressed()");
        ActivitySwitcher.startTeamActivity(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_phases);

        Log.d(LOG_TAG, "onCreate()");

        if (CoachAppSession.getInstance()==null) {
            ActivitySwitcher.startLoginActivity(this);
        }
        CoachAppSession.getInstance().setupActivity(this);

        Log.d(LOG_TAG, "onCreate() storage:" + Storage.getInstance());


        try {
            mAdapter = new ArrayAdapter<TrainingPhase>(this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    Storage.getInstance().getTrainingPhases() );
            Log.d(LOG_TAG, "onCreate()  adapter:" + mAdapter);

            Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
            ActionBar ab = getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.trainingphase_list_title));

            LocalStorage.getInstance().setCurrentMember(null);
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
        if (CoachAppSession.getInstance()==null) {
            ActivitySwitcher.startLoginActivity(this);
        }
        CoachAppSession.getInstance().setupActivity(this);
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
    protected void onStart(){
        super.onStart();

        Log.d(LOG_TAG, "onStart()");

        // Set the adapter
        mListView = (AbsListView) findViewById(R.id.tp_list);
        Log.d(LOG_TAG, "onStart() listview: " + mListView);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        registerForContextMenu(mListView);
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.d(LOG_TAG, "  onCreateContextMenu()");

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        String word = ((TextView) info.targetView).getText().toString();
        long id = info.id;

        try {
            menu.setHeaderTitle("Select");
            currentTPId = Storage.getInstance().getTrainingPhases().get((int) id).getUuid();
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }

//        menu.add(0, v.getId(), 0, "Create instruction video");
        menu.add(0, v.getId(), 0, "Show Trainingphase information");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return CoachAppSession.getInstance().handleTopMenu(item, null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            TrainingPhase tp = Storage.getInstance().getTrainingPhases().get((int)id);
            Log.d(LOG_TAG, " training phase clicked: " + tp.getUuid() + "  " + tp);

            LocalStorage.getInstance().setCurrentTrainingPhase(tp.getUuid());
            ActivitySwitcher.startMemberActivity(this);
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = acmi.position;

//        String fileName = LocalStorage.getInstance().getNewMediaDir() + "/tp-" + currentTPId + JsonSettings.SERVER_VIDEO_SUFFIX;

        TrainingPhase tp = Storage.getInstance().getTrainingPhase(currentTPId);

        Log.d(LOG_TAG, "TP:  " + tp);
        Log.d(LOG_TAG, "TP:  " + tp.getName());
        Log.d(LOG_TAG, "TP:  " + tp.getVideoUuid());


/*
        LocalStorage.getInstance().setCurrentTrainingPhase(currentTPId);

        Media m = Media.newInstructionVideo(fileName, currentTPId);
        currentTPId = null;
        Uri uri = Uri.fromFile(new File(fileName));

        if (m!=null) {
            Log.d(LOG_TAG, "   instruction video item: " + fileName);
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            Log.d(LOG_TAG, "  file: " + fileName + " uri: " + uri);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("android.intent.extra.durationLimit", 5);
            intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
            // start the image capture Intent
            //context.startActivity(intent);
//            activity.startActivityForResult(intent, com.sandklef.coachapp.fragments.VideoCapture.VIDEO_CAPTURE);
            startActivityForResult(intent, VideoCapture.VIDEO_CAPTURE);
//            activity.startActivityForResult(intent, com.sandklef.coachapp.fragments.VideoCapture.VIDEO_CAPTURE);
        }
        Log.d(LOG_TAG, "  new instruction video wanted creation: " + fileName);
*/
        return true;
    }

    private void saveMedia(Uri uri) {
        Log.d(LOG_TAG, "saveMedia()");
        String club   = LocalStorage.getInstance().getCurrentClub();
        String team   = LocalStorage.getInstance().getCurrentTeam();
        String member = null;
        String tp     = LocalStorage.getInstance().getCurrentTrainingPhase();

        Log.d(LOG_TAG, "savemedia(" + uri + ")");
        Log.d(LOG_TAG, "savemedia: " + Storage.getInstance().getTrainingPhase(tp));

        // TODO: get member name instaed of UUID
        Storage.getInstance().log("Video (instr) recorded", "Recorded instructional video for" + Storage.getInstance().getTrainingPhase(tp).getName());
        Log.d(LOG_TAG, "Recorded instructional video for" + Storage.getInstance().getTrainingPhase(tp).getName());
        Media m = new Media(null,
                "",
                club,
                uri.getPath(),
                Media.MEDIA_STATUS_NEW,
                System.currentTimeMillis(),
                team,
                tp,
                member);

        Log.d(LOG_TAG, "Calling storage to store Media.  File: " + uri.getPath());
        Storage.getInstance().saveMedia(m);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "Video callback: " + requestCode + " " + resultCode + " " + data);

        if (requestCode == VideoCapture.VIDEO_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(LOG_TAG, "Video saved to: " +
                        data.getData());
                Log.d(LOG_TAG, "Saving media object...");
                saveMedia(data.getData());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(LOG_TAG, "Video recording cancelled.");
            } else {
                Log.d(LOG_TAG, "Failed to record video");
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d(LOG_TAG, "onConfigurationChanged   orientation change");

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            CoachAppSession.getInstance().unsetSyncModeSoft();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            CoachAppSession.getInstance().unsetSyncModeSoft();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        CoachAppSession.getInstance().unsetSyncMode();
        Log.d(LOG_TAG, "onStop()");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");

    }

}
