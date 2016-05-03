package com.sandklef.coachapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sandklef.coachapp.fragments.TrainingPhasesFragment;
import com.sandklef.coachapp.fragments.VideoCapture;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.misc.ViewSetter;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.model.TrainingPhase;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import coachassistant.sandklef.com.coachapp.R;

public class MemberActivity extends ActionBarActivity implements AbsListView.OnItemClickListener {

    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    private AbsListView mListView;
    private ListAdapter mAdapter;
    private final static String LOG_TAG = MemberActivity.class.getSimpleName();

    private List<Member> teamMembers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate()");

        setContentView(R.layout.activity_member);


        String teamUUid = LocalStorage.getInstance().getCurrentTeam();
        teamMembers = Storage.getInstance().getMembersTeam(LocalStorage.getInstance().getCurrentTeam());

        mAdapter = new ArrayAdapter<Member>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                teamMembers);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(getResources().getString(R.string.member_list_title));

        ViewSetter.setViewText(this,
                R.id.team_text,
                "Team: " + Storage.getInstance().getTeam(LocalStorage.getInstance().getCurrentTeam()));

        ViewSetter.setViewText(this,
                R.id.trainingphase_text,
                "Trainingphase: " +
                        Storage.getInstance().getTrainingPhase(LocalStorage.getInstance().getCurrentTrainingPhase()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected(): " + item);

        switch (item.getItemId()) {

            default:
                finish();
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        // Set the adapter
        mListView = (AbsListView) findViewById(R.id.member_list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Member m = teamMembers.get((int) id);
        Log.d(LOG_TAG, " member clicked: " + m.getUuid() + "  " + m);

        LocalStorage.getInstance().setCurrentMember(m.getUuid());

        ActivitySwitcher.startRecording(this);


/*

        LocalStorage.getInstance().setCurrentMember(m.getUuid());
        showVideo();

        recordVideo();
 */
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log_media_menu, menu);
        return true;
    }
*/

    private void saveMedia(Uri uri) {
        String club = LocalStorage.getInstance().getCurrentClub();
        String team = LocalStorage.getInstance().getCurrentTeam();
        String member = LocalStorage.getInstance().getCurrentMember();
        String tp = LocalStorage.getInstance().getCurrentTrainingPhase();
        // TODO: get member name instaed of UUID
        Storage.getInstance().log("Recorded " + member.toString());
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

    public void recordVideo(View v) {
        Log.d(LOG_TAG, "recordVideo()");

        View videoView = findViewById(R.id.videoView);
        Log.d(LOG_TAG, "VideoView: " + videoView);

//        VideoCapture vc = (VideoCapture) videoView;
        VideoCapture.getInstance().startRecord();


    }


    public void watchInstructionalVideo(View v) {
        String tpUuid = LocalStorage.getInstance().getCurrentTrainingPhase();
        List<Media> tpMedia = Storage.getInstance().getMediaTrainingPhase(tpUuid);
        Media mediaToWatch = null;

        // TODO: decide a strategy for choosing between (possibly) meany tp media
        for (Media m : tpMedia) {
            Log.d(LOG_TAG, " * choosing tp media: " + m.fileName());
            // this means we're choosing the last one
            mediaToWatch = m;
        }

        Log.d(LOG_TAG, "watch video: .... tp: " + tpUuid);
        if (mediaToWatch != null) {
            Log.d(LOG_TAG, "watch video: " + mediaToWatch.getUuid());
        } else {
            Log.d(LOG_TAG, "watch video: .... null");
        }

        if (mediaToWatch != null) {
            Log.d(LOG_TAG, "   video item: " + mediaToWatch.fileName() + ", " + mediaToWatch.getUuid() + ", " + mediaToWatch.getClubUuid() + ", ");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(mediaToWatch.fileName())), "video/*");
            startActivity(intent);
        }
    }


}
