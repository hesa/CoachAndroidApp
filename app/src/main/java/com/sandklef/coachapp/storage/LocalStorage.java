package com.sandklef.coachapp.storage;

import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.*;
import com.sandklef.coachapp.report.ReportUser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.security.acl.LastOwnerException;

import coachassistant.sandklef.com.coachapp.R;

public class LocalStorage {

    private final static String LOG_TAG = LocalStorage.class.getSimpleName();

    private static LocalStorage localStore;
    private final static String sharedPrefname = "session";
    private SharedPreferences settings;

    private static final String SESSION_KEY               = "session-key";
    private static final String CURRENT_CLUB_KEY          = "current-club";
    private static final String CURRENT_TEAM_KEY          = "current-team";
    private static final String CURRENT_TRAININGPHASE_KEY = "current-trainingphase";
    private static final String CURRENT_MEMBER_KEY        = "current-member";

    private String urlBase;

    private Context c;

/*
    public static final String[] imageExtensions = {".png", ".jpg"};
    public static final String[] videoExtensions = {".mp4", ".ogg"};

    public static String COACHAPP_IMAGE_SUFFIX = ".png";
    public static String COACHAPP_VIDEO_SUFFIX = ".mp4";
*/

    /*
    public final static String TEAM_TAG          = "team";
    public final static String TEAMS_TAG         = "teams";
    public final static String TRAININGPHASE_TAG = "trainingphase";
    public final static String TRAINING_TAG      = "training";
*/


    private LocalStorage(Context c) {
        settings = c.getSharedPreferences(sharedPrefname, 0);
        this.c = c;

        // Create dirs in advance
        getNewMediaDir();
        getDeletableMediaDir();
        getDownloadMediaDir();
    }

    public static LocalStorage newInstance(Context c) {
        if (localStore == null) {
            localStore = new LocalStorage(c);
        }
        return localStore;
    }

    public void setKeyValueString(String key, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getServerUrl() {
        return urlBase;
        /*+ "/clubs/" +

                LocalStorage.getInstance().getCurrentClub();
                */
    }

    public void setServerUrl(String url) {
        urlBase = url;
    }

    public String getKeyValueString(String key) {
        return settings.getString(key, "");
    }

    public void setKeyValueInt(String key, int value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getKeyValueInt(String key) {
        return settings.getInt(key, 0);
    }

    public static LocalStorage getInstance() {
        return localStore;
    }

    public void storeSessionId(String sessId) {
        setKeyValueString(SESSION_KEY, sessId);
    }

    public String getSessionId() {
        return getKeyValueString(SESSION_KEY);
    }


    public void setCurrentMember(String uuid) {
        setKeyValueString(CURRENT_MEMBER_KEY, uuid);
    }

    public String getCurrentMember() {
        return getKeyValueString(CURRENT_MEMBER_KEY);
    }

    public void setCurrentTeam(String uuid) {
        setKeyValueString(CURRENT_TEAM_KEY, uuid);
    }

    public String getCurrentTeam() {
        return getKeyValueString(CURRENT_TEAM_KEY);
    }

    public void setCurrentTrainingPhase(String uuid) {
        setKeyValueString(CURRENT_TRAININGPHASE_KEY, uuid);
    }

    public String getCurrentTrainingPhase() {
        return getKeyValueString(CURRENT_TRAININGPHASE_KEY);
    }


    public void setCurrentClub(String uuid) {
        Log.d(LOG_TAG, "Set current club uuid: " + uuid);
        setKeyValueString(CURRENT_CLUB_KEY, uuid);
    }

    public String getCurrentClub() {
        Log.d(LOG_TAG, "Get current club uuid: " + getKeyValueString(CURRENT_CLUB_KEY));
        return getKeyValueString(CURRENT_CLUB_KEY);
    }

    public String getAppDir() {
// try {
        String dir = Environment.getExternalStorageDirectory().toString() +  "/com.sandklef.coachapp/" + getCurrentClub() + "/";
  /*      } catch (IOException e) {
            Log.w(LOG_TAG, "Failed getting default path");
            ReportUser.warning(c, "Could not get default internal file storage directory\nThis app might not work. Please report this as a bug");
            return null;
        }*/
        Log.d(LOG_TAG, "  getAppDir() => " + dir);
        return dir;
    }

    private String getMediaDirImpl(String extra) {
        if (extra == null) { return null; }
        File f = new File(getAppDir() + extra);
        if (! f.exists()) {
            f.mkdirs();
        }
        return f.getAbsolutePath();
    }

    public String getNewMediaDir() {
        return getMediaDirImpl(c.getString(R.string.NEW_MEDIA_DIR));
    }

    public String getDownloadMediaDir() {
        return getMediaDirImpl(c.getString(R.string.DOWNLOAD_MEDIA_DIR));
    }

    public String getDeletableMediaDir() {
        return getMediaDirImpl(c.getString(R.string.DELETABLE_MEDIA_DIR));
    }

    public boolean replaceLocalWithDownloaded(Media m, String newFileName) {
        File oldFile = new File(m.fileName());
        if (Storage.getInstance().updateMediaReplaceDownloadedFile(m, newFileName)) {
            String replaceFileName = m.fileName().replace(c.getString(R.string.NEW_MEDIA_DIR),
                    c.getString(R.string.DELETABLE_MEDIA_DIR));
            File newFile =
                    new File(replaceFileName);
            // Create deletable dir first
            Log.d(LOG_TAG, "Move " + oldFile.getAbsolutePath() + " => " + newFile.getAbsolutePath());
            return oldFile.renameTo(newFile);
        }
        return false;
    }

}
