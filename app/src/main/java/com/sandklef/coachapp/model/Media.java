package com.sandklef.coachapp.model;

import android.net.Uri;
import android.provider.MediaStore;

import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.storage.LocalStorage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hesa on 2016-02-09.
 */
public class Media extends Base {

    /*    public enum MediaStatus {*/
    public final static int MEDIA_STATUS_UNDEFINED = 0;
    public final static int MEDIA_STATUS_NEW = 1;
    public final static int MEDIA_STATUS_UPLOADED = 2;
    public final static int MEDIA_STATUS_AVAILABLE = 3;
    public final static int MEDIA_STATUS_UPLOAD_FAILED = 4;
    public final static int MEDIA_STATUS_DOWNLOADED = 5;
    public final static int MEDIA_STATUS_DELETABLE = 6;
    public final static int MEDIA_STATUS_FROMSERVER = 7;
  /*  } */

    private String file;
    private int status;
    private long date;
    private String teamUuid;
    private String trainingPhaseUuid;
    private String memberUuid;

    private final static String LOG_TAG = Media.class.getSimpleName();

    public Media(String uuid,
                 String name,
                 String clubUuid,
                 String file,
                 int status,
                 long date,
                 String teamUuid,
                 String trainingPhaseUuid,
                 String memberUuid) {
        super(uuid, name, clubUuid);

        Log.d(LOG_TAG, "Constructing ned Media from file: " + file);
        this.file = file;
        this.status = status;
        this.date = date;
        this.teamUuid = teamUuid;
        this.trainingPhaseUuid = trainingPhaseUuid;
        this.memberUuid = memberUuid;
    }


    public Media(File f) {
        super(null, null, LocalStorage.getInstance().getCurrentClub());
        file = f.getAbsolutePath();
        status = MEDIA_STATUS_UNDEFINED;
    }

    public void setStatus(int s) {
        status = s;
    }

    public String fileName() {
        return file;
    }

    public int getStatus() {
        return status;
    }

    public long getDate() {
        return date;
    }

    public String getTeam() {
        return teamUuid;
    }

    public String getTrainingPhase() {
        return trainingPhaseUuid;
    }

    public String getMember() {
        return memberUuid;
    }

    public String toString() {
/*        return super.toString() +
                " " + getDate()
                + " " + getTeam()
                + " " + getTrainingPhase()
                + " " + getDate()
                + " " + getStatus();
*/
        DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Date date = new Date(getDate());
        return df.format(date) ;//+ super.toString() + "-" + file  ;
    }

}
