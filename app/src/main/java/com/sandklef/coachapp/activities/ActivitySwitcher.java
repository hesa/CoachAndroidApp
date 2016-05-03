package com.sandklef.coachapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.sandklef.coachapp.fragments.VideoCapture;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.storage.LocalStorage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hesa on 2016-02-27.
 */
public class ActivitySwitcher {

    private final static String LOG_TAG = ActivitySwitcher.class.getSimpleName();

    // TODO: MOVE somewhere
    private final static String VIDEO_FILE_DATE_FORMAT = "yyyyMMdd-HHmmss";
    private final static String VIDEO_FILE_TYPE_SUFFIX = ".mp4";
    private final static int VIDEO_FILE_DEFAULT_TIME = 5000;


    private static void startActivityImpl(Activity a, Class c) {
        Intent intent = new Intent(a, c);
        a.startActivity(intent);
    }

    private static void startActivityImpl(Context con, Class c) {
        Intent intent = new Intent(con, c);
        con.startActivity(intent);
    }

    // TODO: remove this method. should not be needed
    public static void startTrainingActivity(Activity a) {
        startActivityImpl(a, com.sandklef.coachapp.activities.TeamsActivity.class);
    }

    public static void startTeamActivity(Activity a) {
        startActivityImpl(a, com.sandklef.coachapp.activities.TeamsActivity.class);
    }

    public static void startTeamActivity(Context c) {
        Intent intent = new Intent(c, com.sandklef.coachapp.activities.TeamsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        c.startActivity(intent);
    }


    public static void startTrainingPhaseActivity(Activity a) {
        startActivityImpl(a, com.sandklef.coachapp.activities.TrainingPhasesActivity.class);
    }

    public static void startMemberActivity(Activity a) {
        startActivityImpl(a, com.sandklef.coachapp.activities.MemberActivity.class);
    }

    public static void startLocalMediaManager(Activity a) {
        startActivityImpl(a, com.sandklef.coachapp.activities.LocalMediaManager.class);
    }

    public static void startLogMessageActivity(Activity a) {
        startActivityImpl(a, com.sandklef.coachapp.activities.LogActivity.class);
    }

    public static void startClubInfoActivity(Activity a) {
        startActivityImpl(a, com.sandklef.coachapp.activities.ClubInfoActivity.class);
    }

    /*    public static void startVideoRecordActivity(Activity a) {
            startActivityImpl(a, com.sandklef.coachapp.activities.CameraActivity.class);
        }
    */
/*    public static boolean startRecording(Activity a) {
        startVideoRecordActivity(a);
        return true;
    }
*/
    public static boolean startRecording(Activity a) {

        DateFormat df = new SimpleDateFormat(VIDEO_FILE_DATE_FORMAT);
        Date today = Calendar.getInstance().getTime();
        String mediaDate = df.format(today);

        String newFileName = LocalStorage.getInstance().getNewMediaDir() + "/" +
                mediaDate + VIDEO_FILE_TYPE_SUFFIX;

        File newFile = new File(newFileName);
        String dirName = newFile.getParent();
        File dir = new File(dirName);

        Log.d(LOG_TAG, "  Dir:  " + dir.getPath());
        boolean created = dir.mkdirs();

        Log.d(LOG_TAG, "RECORD TO NEW FILE: " + newFile);

        Uri uri = Uri.fromFile(newFile);

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        Log.d(LOG_TAG, "  file: " + newFile.getParent() + " " + newFile + " " + uri);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("android.intent.extra.durationLimit", 5);
        intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
        // start the image capture Intent
        //context.startActivity(intent);
        a.startActivityForResult(intent, VideoCapture.VIDEO_CAPTURE);
        return true;
    }

}
