package com.sandklef.coachapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
/*
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;

import com.sandklef.coachapp.fragments.Camera2Fragment;
*/
import com.sandklef.coachapp.filters.MediaFilter;
import com.sandklef.coachapp.filters.MediaFilterEngine;
import com.sandklef.coachapp.filters.MediaStatusNameFilter;
import com.sandklef.coachapp.fragments.MemberFragment;
import com.sandklef.coachapp.fragments.SimpleVideoFragment;
import com.sandklef.coachapp.fragments.TeamFragment;
import com.sandklef.coachapp.fragments.TopFragment;
import com.sandklef.coachapp.fragments.TrainingPhasesFragment;
//import com.sandklef.coachapp.fragments.UserFragment;
import com.sandklef.coachapp.fragments.VideoCapture;
import com.sandklef.coachapp.json.JsonParser;
import com.sandklef.coachapp.misc.Log;
//import com.sandklef.coachapp.model.Club;
import com.sandklef.coachapp.model.Club;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.model.Team;
import com.sandklef.coachapp.model.TrainingPhase;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.Storage;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/*
import android.view.View;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
*/

import coachassistant.sandklef.com.coachapp.R;


public class TopActivity extends AppCompatActivity implements
        TopFragment.OnFragmentInteractionListener,
        TeamFragment.TeamFragmentListener,
        MemberFragment.MemberInteractionListener,
        TrainingPhasesFragment.TrainingPhasesFragmentListener,
        SimpleVideoFragment.OnSimpleVideoListener {

    private final static String LOG_TAG = TopActivity.class.getSimpleName();
    private static Storage storage;
    private TopFragment topFragment;
    private Toolbar     toolbar;
    private Club        currentClub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TEMP Settings  TODO: Make this flexible,
        // Should be received via the bundle instead
//        Club c11 = new Club("e0b7098f-b7e1-4fe4-89bb-22c4d83f1141", "IK Nord");
        currentClub = new Club("c04b2bdd-9fef-4123-b4cb-e122081e1868", "AHK");
//        currentClub = new Club("e0b7098f-b7e1-4fe4-89bb-22c4d83f1141", "IK Nord");


        Storage.newInstance(currentClub.getUuid(), getApplicationContext());
        LocalStorage.newInstance(getApplicationContext());
        LocalStorage.getInstance().setServerUrl("http://172.17.42.1:3000/0.0.0/");
        LocalStorage.getInstance().setCurrentClub(currentClub.getUuid());


        // TEST
        Log.d(LOG_TAG, "Media new:");
        for (Media media: MediaFilterEngine.apply(Storage.getInstance().getMedia(), MediaStatusNameFilter.newMediaFilterStatus(Media.MEDIA_STATUS_NEW))){
            Log.d(LOG_TAG, " * " + media.toString());
        }
        Log.d(LOG_TAG, "Media created:");
        for (Media media: MediaFilterEngine.apply(Storage.getInstance().getMedia(), MediaStatusNameFilter.newMediaFilterStatus(Media.MEDIA_STATUS_CREATED))){
            Log.d(LOG_TAG, " * " + media.toString());
        }
        Log.d(LOG_TAG, "Media uploaded:");
        for (Media media: MediaFilterEngine.apply(Storage.getInstance().getMedia(), MediaStatusNameFilter.newMediaFilterStatus(Media.MEDIA_STATUS_UPLOADED))){
            Log.d(LOG_TAG, " * " + media.toString());
        }
        Log.d(LOG_TAG, "Media downloaded:");
        for (Media media: MediaFilterEngine.apply(Storage.getInstance().getMedia(), MediaStatusNameFilter.newMediaFilterStatus(Media.MEDIA_STATUS_DOWNLOADED))){
            Log.d(LOG_TAG, " * " + media.toString());
        }


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String tag = "com.sandklef.coachapp.fragments.TopFragment";
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);

        if (fragment == null) {
            fragment = Fragment.instantiate(this, tag);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(android.R.id.content, fragment, tag);
            ft.commit();
        }

        topFragment = (TopFragment) fragment;

        updateFromServer();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // TODO: make better ... simply do it
        Log.d(LOG_TAG, "Setting club info");
        Log.d(LOG_TAG, "Setting club info: " + currentClub);
        Log.d(LOG_TAG, "Setting club info: " + currentClub.getUuid());
        Log.d(LOG_TAG, "Setting club info: " + currentClub.getName());

    //    ((TextView)findViewById(R.id.club_name)).setText(currentClub.getName());
    }



    public void updateFromServer() {
        (new JsonParser(LocalStorage.getInstance().getCurrentClub(), getApplicationContext())).execute();
    }

    @Override
    public void onBackPressed() {
        int fragmentIndex = topFragment.getCurrentBottomFragmentIndex();
        Log.d(LOG_TAG, "onBackPressed() " + fragmentIndex);

        if (fragmentIndex != 0) {
            fragmentIndex--;
            topFragment.setBottomFragmentIndex(fragmentIndex);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }


    public void onFragmentInteraction(Uri uri) {
        Log.d(LOG_TAG, "click ...kinda 1");
    }

    public void onTeamFragmentInteraction(Team t) {
        Log.d(LOG_TAG, "onTrainingphasesFragmentInteraction " + t);
        if (topFragment != null) {
            topFragment.onTeamFragmentInteraction(t);
        }
    }

    public void onTrainingphasesFragmentInteraction(TrainingPhase tp) {
        Log.d(LOG_TAG, "onTrainingphasesFragmentInteraction " + tp);
        if (topFragment != null) {
            topFragment.onTrainingphasesFragmentInteraction(tp);
        }
    }

    public void onMemberInteraction(Member m) {
        Log.d(LOG_TAG, " onMemberInteraction " + m);
        if (topFragment != null) {
            topFragment.onMemberInteraction(m);
        }
    }

    public void onMediaInteraction(long id) {
        Log.d(LOG_TAG, " onMediaInteraction " + id);
    }

    public void onSimpleVideoInteraction(Uri uri) {
        Log.d(LOG_TAG, " onSimpleVideoInteraction() " + uri);
    }


    private void saveMedia(Uri uri) {
        String club = LocalStorage.getInstance().getCurrentClub();
        String team = LocalStorage.getInstance().getCurrentTeam();
        String member = LocalStorage.getInstance().getCurrentMember();
        String tp = LocalStorage.getInstance().getCurrentTrainingPhase();

        /*DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Date today = Calendar.getInstance().getTime();
        String mediaDate = df.format(today);
        */

        /*
public Media(String uuid,
                 String name,
                 String clubUuid,
                 String file,
                 MediaStatus status,
                 long   date,
                 String teamUuid,
                 String trainingPhaseUuid,
                 String memberUuid) {
         */

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

    private void showMediamangerMode() {
        Intent intent = new Intent(this, com.sandklef.coachapp.activities.LocalMediaManager.class);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Log.d(LOG_TAG, "onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.menu_media_manager:
                showMediamangerMode();
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
//                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                Log.d(LOG_TAG, "  doin nada");
                return true;
        }
    }

}



