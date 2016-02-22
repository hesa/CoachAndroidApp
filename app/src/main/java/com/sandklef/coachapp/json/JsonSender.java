package com.sandklef.coachapp.json;

import android.content.Context;
import android.os.AsyncTask;

import com.sandklef.coachapp.http.HttpAccess;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by hesa on 2016-02-15.
 */
public class JsonSender extends AsyncTask<JsonSender.AsyncBundle, Void, JsonSender.AsyncBundle> {


    private final static String LOG_TAG = JsonSender.class.getSimpleName();

    public static final int MODE_CREATE   = 0;
    public static final int MODE_UPLOAD   = 1;
    public static final int MODE_DOWNLOAD = 2;

    public static final String SERVER_VIDEO_SUFFIX = ".webm";


    private String clubName;
    private Context context;
    private HttpAccess httpAccess;

    private String jsonString;

    public JsonSender(String name, Context context) {
        this.clubName = name;
        this.context = context;
        this.httpAccess =
                new HttpAccess(LocalStorage.getInstance().getServerUrl(),name);
    }

    /*    public AsyncBundle uploadVideoToServer(Media m) throws JsonSenderException {
            Log.d(LOG_TAG, "uploadVideoToServer()  m: " + m);
            return null;
        }
    */
    public void uploadTrainingPhaseVideo(Media m) {

        Log.d(LOG_TAG, "   upload file: " + m.fileName());
        Log.d(LOG_TAG, "      uuid    : " + m.getUuid());
        boolean success = httpAccess.uploadTrainingPhaseVideo(m.getUuid(), m.fileName());

        if (success) {
            boolean res = Storage.getInstance().updateMediaState(m, Media.MEDIA_STATUS_UPLOADED);
            if (!res) {
                // TODO: how to hande this properly
                Log.d(LOG_TAG, "Failed setting status to upload.... ");
            }
        }
        return;
    }

    public String createVideoOnServer(Media m) throws JsonSenderException {
        String trainingPhaseUuid = m.getTrainingPhase();
        if (trainingPhaseUuid.length() < 3) {
            throw new JsonSenderException("No TrainingPhase id");
        }
        String jsonData = "{ \"trainingPhaseUuid\": \"" + trainingPhaseUuid + "\" }";
        String header = "application/json";

        String jsonString = httpAccess.postTrainingPhase(jsonData, header);
        Log.d(LOG_TAG, jsonString);
        try {
            JSONObject jo = new JSONObject(jsonString);
            String uuid = jo.getString("uuid");
            String state = jo.getString("status");
            Log.d(LOG_TAG, "created (id):    " + uuid);
            Log.d(LOG_TAG, "created (state): " + state);
//            if (state.equals("empty")) {
            Log.d(LOG_TAG, "Newly created video on server: " + uuid);
            Storage.getInstance().updateMediaStateCreated(m, uuid);


  /*          } else {
                return null;
            }*/
        } catch (JSONException e) {
            Log.d(LOG_TAG, " failed converting http response to json..");
        }

        return null;
    }

    @Override
    protected AsyncBundle doInBackground(AsyncBundle... bundles) {
        AsyncBundle bundle = bundles[0];
        int mode = bundle.getMode();
        Log.d(LOG_TAG, "doInBackground  mode: " + mode);

        if (mode == MODE_CREATE) {
            try {
                createVideoOnServer(bundle.getMedia());
            } catch (JsonSenderException e) {
                // TODO: store errors in log?
                Log.d(LOG_TAG, " Could not create video on server");
                return null;
            }
        } else if (mode == MODE_UPLOAD) {
            uploadTrainingPhaseVideo(bundle.getMedia());
            // TODO: store errors in log?
            Log.d(LOG_TAG, " Finished uploading video to server");
            return null;
        } else if (mode == MODE_DOWNLOAD) {
            Media m          = bundle.getMedia();
            String videoUuid = m.getUuid();
            String file      = LocalStorage.getInstance().getDownloadMediaDir() + "/" + videoUuid + SERVER_VIDEO_SUFFIX;
            boolean result   = httpAccess.downloadVideo(file, videoUuid);
            if (result) {
                result =    LocalStorage.getInstance().replaceLocalWithDownloaded(m, file);
            }
            // TODO: store errors in log?
            Log.d(LOG_TAG, " Finished downloading video to server: " + result);
            return null;
        }
        return new AsyncBundle(mode, 0, bundle.getMedia());
    }



    @Override
    protected void onPostExecute(JsonSender.AsyncBundle bundle) {
        Log.d(LOG_TAG, "onPostExecute");
    }

    public static class AsyncBundle {
        private int mode;
        private int status;
        private Media media;

        public AsyncBundle(int mode, int status, Media m) {
            this.mode = mode;
            this.status = status;
            this.media = m;
        }

        public int getMode() {
            return mode;
        }

        public int getStatus() {
            return status;
        }

        public Media getMedia() {
            return media;
        }
    }


}
