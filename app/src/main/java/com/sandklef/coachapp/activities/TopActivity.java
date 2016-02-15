package com.sandklef.coachapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;

import com.sandklef.coachapp.fragments.Camera2Fragment;
import com.sandklef.coachapp.fragments.MemberFragment;
import com.sandklef.coachapp.fragments.SimpleVideoFragment;
import com.sandklef.coachapp.fragments.TeamFragment;
import com.sandklef.coachapp.fragments.TopFragment;
import com.sandklef.coachapp.fragments.TrainingPhasesFragment;
import com.sandklef.coachapp.fragments.UserFragment;
import com.sandklef.coachapp.fragments.VideoCapture;
import com.sandklef.coachapp.json.JsonParser;
import com.sandklef.coachapp.misc.Log;
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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Storage.newInstance(getApplicationContext());
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
        Storage.newInstance(getApplicationContext());
        LocalStorage.newInstance(getApplicationContext());

        Log.d(LOG_TAG, "CurrentMember:  " + LocalStorage.getInstance().getCurrentMember());
        Club c = new Club("e0b7098f-b7e1-4fe4-89bb-22c4d83f1141", "IK Nord");

        LocalStorage.getInstance().setCurrentClub(c.getUuid());

        JsonParser jsp = new JsonParser(c.getUuid(), getApplicationContext());
        jsp.update();


        List<Media> media = Storage.getInstance().getMedia();
        Log.d(LOG_TAG, "Media list: " + media.size());
        for (Media m: media) {
            Log.d(LOG_TAG, " * media: " + m);
        }

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
        switch (item.getItemId()) {
            case R.id.menu_media_manager:
                showMediamangerMode();
                return true;
            default:
                Log.d(LOG_TAG, "  doin nada");
                return true;
        }
    }

}



