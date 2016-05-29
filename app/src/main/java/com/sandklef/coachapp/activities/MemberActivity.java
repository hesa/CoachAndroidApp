package com.sandklef.coachapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sandklef.coachapp.Session.CoachAppSession;
import com.sandklef.coachapp.fragments.TrainingPhasesFragment;
import com.sandklef.coachapp.fragments.VideoCapture;
import com.sandklef.coachapp.json.JsonAccess;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.misc.ViewSetter;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.model.Team;
import com.sandklef.coachapp.model.TrainingPhase;
import com.sandklef.coachapp.report.ReportUser;
import com.sandklef.coachapp.storage.ConnectionStatusListener;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.LocalStorageSync;
import com.sandklef.coachapp.storage.Storage;
import com.sandklef.coachapp.storage.StorageNoClubException;
import com.sandklef.coachapp.storage.StorageUpdateListener;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import coachassistant.sandklef.com.coachapp.R;

public class MemberActivity extends ActionBarActivity
        implements AbsListView.OnItemClickListener, StorageUpdateListener, View.OnLongClickListener {

    private GridView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    private AbsListView mListView;
    private ListAdapter mAdapter;
    private final static String LOG_TAG = MemberActivity.class.getSimpleName();

    private List<Member> teamMembers;

    private Media instructionalVideo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate()");

        setContentView(R.layout.activity_member);

        CoachAppSession.getInstance().setupActivity(this);


        String teamUUid = LocalStorage.getInstance().getCurrentTeam();
        teamMembers = Storage.getInstance().getMembersTeam(LocalStorage.getInstance().getCurrentTeam());

        Log.d(LOG_TAG, "Members: ");
        for (Member m: teamMembers) {
            Log.d(LOG_TAG, " * " + m);
        }


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
                getResources().getString(R.string.team_column) + Storage.getInstance().getTeam(LocalStorage.getInstance().getCurrentTeam()));

        ViewSetter.setViewText(this,
                R.id.trainingphase_text,
                getResources().getString(R.string.trainingphase_column) +
                        Storage.getInstance().getTrainingPhase(LocalStorage.getInstance().getCurrentTrainingPhase()));
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

/*
    private void findInstructionalVideo(Menu menu) {
        String tpUuid    = LocalStorage.getInstance().getCurrentTrainingPhase();
        Log.d(LOG_TAG, "findInstructionalVideo() tp:    " + tpUuid);
        Media media= Storage.getInstance().getInstructionalMedia(tpUuid);
        MenuItem item = (MenuItem) menu.findItem(R.id.instructionalAction);

        if (media!=null) {
            item.setIcon(R.drawable.ic_play_arrow_black_24dp);
            Log.d(LOG_TAG, "findInstructionalVideo() media: " + media.getUuid());

        } else {
            item.setIcon(R.drawable.ic_videocam_black_24dp);
            Log.d(LOG_TAG, "findInstructionalVideo() media: null");
        }
        instructionalVideo = media;
    }
*/
    private Uri newFileUri() {
        File f = new File(CoachAppSession.getInstance().newFileName());
        return  Uri.fromFile(f);
    }

    /*

    public void recordVideo2(View v) {
        Log.d(LOG_TAG, "recordVideo()");

        View videoView = findViewById(R.id.videoView);
        Log.d(LOG_TAG, "VideoView: " + videoView);


        String file = CoachAppSession.getInstance().newFileName();
//        VideoCapture vc = (VideoCapture) videoView;
        VideoCapture.getInstance().startRecordTP(file);

        saveMedia(Uri.fromFile(new File(file)));
    }
    */

    public void recordVideo(View v) {
        Log.d(LOG_TAG, "recordVideo()");
        ActivitySwitcher.startMediaRecorderActivity(this);


    }


    public void recordInstructionalVideo(View v) {
        Log.d(LOG_TAG, "recordInstructionalVideo()");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        String file = CoachAppSession.getInstance().newFileName();

        Log.d(LOG_TAG, "  file: " + file);
        Uri uri = Uri.fromFile(new File(file));


        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
/*
        intent.putExtra("android.intent.extra.durationLimit", 5);
        intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);

        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
  */
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
        startActivityForResult(intent, VideoCapture.VIDEO_CAPTURE);
       // saveinstructionMedia(Uri.fromFile(new File(file)));
    }


/*
    public void recordInstructionalVideo_OLD(View v) {
        Log.d(LOG_TAG, "recordInstructionalVideo()");
        View videoView = findViewById(R.id.videoView);
        String file = CoachAppSession.getInstance().newFileName();
        VideoCapture.getInstance().startRecordInstructional(file);
        saveinstructionMedia(Uri.fromFile(new File(file)));
    }
*/

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Member m = teamMembers.get((int) id);
        Log.d(LOG_TAG, " member clicked: " + m.getUuid() + "  " + m);

        LocalStorage.getInstance().setCurrentMember(m.getUuid());

//        ActivitySwitcher.startRecording(this);
        String file = CoachAppSession.getInstance().newFileName();


        try {
//        VideoCapture vc = (VideoCapture) videoView;
// BRING BACK?            VideoCapture.getInstance().startRecordTP(file);
            ActivitySwitcher.startMediaRecorderActivity(this);
        } catch (java.lang.RuntimeException e) {
            Log.d(LOG_TAG, "Recording failed....");
            new File(file).delete();
            ReportUser.log("Video recording failed", e.getMessage());
            return;
        }
       saveMedia(Uri.fromFile(new File(file)));


/*

        LocalStorage.getInstance().setCurrentMember(m.getUuid());
        showVideo();

        recordVideo();
 */
    }


    private void saveMediaImpl(Uri uri, String member) {
        String club = LocalStorage.getInstance().getCurrentClub();
        String team = LocalStorage.getInstance().getCurrentTeam();
        String tp = LocalStorage.getInstance().getCurrentTrainingPhase();

        String teamName = Storage.getInstance().getTeam(LocalStorage.getInstance().getCurrentTeam()).getName();
        String tpName   = Storage.getInstance().getTrainingPhase(LocalStorage.getInstance().getCurrentTrainingPhase()).getName();

        if (member!=null) {
            // TODO: get member name instaed of UUID
            Storage.getInstance().log("Video recorded " + member, "Recorded video (" + member.toString() + " | " + tpName +" | " + teamName + ")" );
        }
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


    private void saveMedia(Uri uri) {
        Log.d(LOG_TAG, "SaveMedia()");
        String member = LocalStorage.getInstance().getCurrentMember();
        saveMediaImpl(uri, member);
    }

    private void saveinstructionMedia(Uri uri) {
        saveMediaImpl(uri, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.member_menu, menu);
     //   findInstructionalVideo(menu);

        CoachAppSession.getInstance().setupActivity(this, menu, R.id.member_sync);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "  onOptionsItemSelected: " + item.getItemId());

        switch (item.getItemId()){
            case R.id.instructionalPlay:
                Log.d(LOG_TAG, "handle instrucionalitem");
                handleInstructionalVideo(null);
                break;
            case R.id.instructionalRecord:
                Log.d(LOG_TAG, "handle instrucionalitem");
                recordInstructionalVideo(null);
                break;
            case R.id.member_sync:
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  sync");
                LocalStorageSync.getInstance().syncLocalStorage();
                Storage.getInstance().downloadTrainingPhaseFiles();
                break;
            default:
                Log.d(LOG_TAG, "default, go back");
                CoachAppSession.getInstance().getCurrentActivity().finish();
                break;
        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "Video callback: " + requestCode + " " + resultCode + " " + data);
        if (requestCode == VideoCapture.VIDEO_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(LOG_TAG, "Video saved to: " +
                        data.getData());
                Log.d(LOG_TAG, "Calling SaveMedia media object...");
                saveMedia(data.getData());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(LOG_TAG, "Video recording cancelled.");
            } else {
                Log.d(LOG_TAG, "Failed to record video");
            }
        }
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


    @Override
    public void onStorageUpdate() {
        Log.d(LOG_TAG, "onStorageUpdate()");

    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(LOG_TAG, "onLongClick()");
        //recordInstructionalVideo(v);
        return true;
    }


    private Media getInstructionalMedia() {
        String tpUuid    = LocalStorage.getInstance().getCurrentTrainingPhase();
        TrainingPhase tp = Storage.getInstance().getTrainingPhase(tpUuid);
        Log.d(LOG_TAG, "findInstructionalVideo() tp:    " + tpUuid);
        Log.d(LOG_TAG, "findInstructionalVideo() video: " + tp.getVideoUuid());

        Media media= Storage.getInstance().getInstructionalMedia(tpUuid);
        return media;
    }


    public void handleInstructionalVideo(View v) {
        Log.d(LOG_TAG, "handleInstructionalVideo()");
        Media m = getInstructionalMedia();
        if (m==null || m.fileName()==null) {
            Log.d(LOG_TAG, "Nothing to show");
            ReportUser.inform(this, "Video not download. Press sync button");
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Log.d(LOG_TAG, "Will show: " + m.fileName());
            intent.setDataAndType(Uri.fromFile(new File(m.fileName())), "video/*");
            startActivity(intent);
        }
    }


}
