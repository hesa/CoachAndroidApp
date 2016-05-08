package com.sandklef.coachapp.storage;

import android.content.Context;
import android.os.AsyncTask;

import com.sandklef.coachapp.json.JsonAccess;
import com.sandklef.coachapp.json.JsonAccessException;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Club;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.model.Team;
import com.sandklef.coachapp.model.TrainingPhase;

import java.util.List;


import com.sandklef.coachapp.json.JsonAccess;
import com.sandklef.coachapp.json.JsonAccessException;
import com.sandklef.coachapp.json.JsonSettings;
import com.sandklef.coachapp.report.ReportUser;

/**
 * Created by hesa on 2016-02-25.
 */
public class StorageRemoteWorker extends AsyncTask<StorageRemoteWorker.AsyncBundle, Void, StorageRemoteWorker.AsyncBundle> {

    private final static String LOG_TAG = StorageRemoteWorker.class.getSimpleName();

    private String clubUuid;
//    private Context c;
    private JsonAccess ja;

    public StorageRemoteWorker(String clubUuid /*, Context c*/) throws StorageException {
        try {
  //          this.c = c;
            this.clubUuid = clubUuid;

            ja = new JsonAccess(clubUuid);
        } catch (JsonAccessException e) {
            throw new StorageException("Failed to create JsonAccess instance", e);
        }
    }

    @Override
    protected AsyncBundle doInBackground(AsyncBundle... bundles) {
        AsyncBundle cbundle = bundles[0];
        SimpleAsyncBundle bundle = cbundle.getSimpleAsyncBundle();
        int mode = cbundle.getMode();
        Log.d(LOG_TAG, "doInBackground  mode: " + mode);

        if (mode == Storage.MODE_COMPOSITE) {
            Log.d(LOG_TAG, " fetching JSON data in background");
            try {
                JsonAccess.CompositeBundle cb = ja.update();

                Storage.getInstance().updateDB(cb.members,
                        cb.teams,
                        cb.media,
                        cb.tps);
            } catch (JsonAccessException e) {
                Log.d(LOG_TAG, "Failed getting Json data from server" + e.getMessage());
// TODO: Fix ... make it possible to report to user
//                ReportUser.warning(c, "Failed getting data from server");
            }

        } else if (mode == Storage.MODE_CREATE) {
            try {
                String uuid = ja.createVideoOnServer(bundle.getMedia());
                Log.d(LOG_TAG, " creation seems to have work, uuid: " + uuid + "  and media: " + bundle.getMedia());
                return new AsyncBundle(mode, new SimpleAsyncBundle(0, bundle.getMedia(), uuid));
            } catch (JsonAccessException e) {
                // TODO: store errors in log?
                Log.e(LOG_TAG, " Could not create video on server" + e.getMessage());
                return null;
            }
        } else if (mode == Storage.MODE_UPLOAD) {
            try {
                ja.uploadTrainingPhaseVideo(bundle.getMedia());
                Log.d(LOG_TAG, " upload seems to have work with media: " + bundle.getMedia());
                return new AsyncBundle(mode, new SimpleAsyncBundle(0, bundle.getMedia()));
            } catch (JsonAccessException e) {
                Log.e(LOG_TAG, " Failed uploading video to server: " + e.getMessage());
                Storage.getInstance().log("Failed downloading video from server");
            }
            // TODO: store errors in log?
            Log.e(LOG_TAG, " Finished uploading video to server");
            return null;
        } else if (mode == Storage.MODE_DOWNLOAD) {
            Media m = bundle.getMedia();
            String videoUuid = m.getUuid();
            String file = LocalStorage.getInstance().getDownloadMediaDir() + "/" + videoUuid + JsonSettings.SERVER_VIDEO_SUFFIX;
            try {
                ja.downloadVideo(file, videoUuid);
                LocalStorage.getInstance().replaceLocalWithDownloaded(m, file);
            } catch (JsonAccessException e) {
                // TODO: store errors in log?
                Log.e(LOG_TAG, " Failed downloading video from server; " + e.getMessage());
                e.printStackTrace();
                Storage.getInstance().log("Failed downloading video from server");
                return null;
            }
            Log.d(LOG_TAG, " Finished downloading video from server ");
            return null;
        }
//        return new SimpleAsyncBundle(mode, 0, bundle.getMedia());
        return null;
    }

    protected void onPostExecute(AsyncBundle bundle) {
        Log.d(LOG_TAG, " onPostExecute " + bundle);

        if (bundle != null) {
            int mode = bundle.getMode();
            CompositeAsyncBundle cam = bundle.getCompositeAsyncBundle();
            SimpleAsyncBundle sam = bundle.getSimpleAsyncBundle();
            /*
             *
             *  Store in DB
             *
             */

            if (mode == Storage.MODE_COMPOSITE) {
                Storage.getInstance().updateDB(cam.getMembers(),
                        cam.getTeams(),
                        cam.getMedia(),
                        cam.getTrainingPhases());
            } else if (mode == Storage.MODE_CREATE) {
                Log.d(LOG_TAG, " onPostExecute mode create");
                String uuid = sam.getUuid();
                Media m = sam.getMedia();
                Log.d(LOG_TAG, "Newly created video on server: " + uuid);
                Storage.getInstance().updateMediaStateCreated(m, uuid);
            } else if (mode == Storage.MODE_UPLOAD) {
                Log.d(LOG_TAG, " onPostExecute mode upload");
                Media m = sam.getMedia();
                boolean res = Storage.getInstance().updateMediaState(m, Media.MEDIA_STATUS_UPLOADED);
            } else if (mode == Storage.MODE_DOWNLOAD) {
                Log.d(LOG_TAG, " onPostExecute mode download");
                Media m = sam.getMedia();
                boolean res = Storage.getInstance().updateMediaState(m, Media.MEDIA_STATUS_DOWNLOADED);



            }


        }
        // TODO: throw exception
    }

    /*    @Override
        protected void onPostExecute(SimpleAsyncBundle bundle) {
            Log.d(LOG_TAG, "onPostExecute");
        }
    */

    public static class SimpleAsyncBundle {
        private int status;
        private Media media;
        private String uuid;

        public SimpleAsyncBundle(int status, Media m) {
            this.status = status;
            this.media = m;
        }

        public SimpleAsyncBundle(int status, Media m, String uuid) {
            this.status = status;
            this.media = m;
            this.uuid = uuid;
        }

        public int getStatus() {
            return status;
        }

        public Media getMedia() {
            return media;
        }

        public String getUuid() {
            return uuid;
        }
    }

    public static class CompositeAsyncBundle {
        private List<Member> members;
        private List<Team> teams;
        private List<Media> media;
        private List<TrainingPhase> tps;


        public CompositeAsyncBundle(List<Member> members,
                                    List<Team> teams,
                                    List<Media> media,
                                    List<TrainingPhase> tps) {
            this.members = members;
            this.teams = teams;
            this.media = media;
            this.tps = tps;
        }

        public List<Member> getMembers() {
            return members;
        }

        public List<Media> getMedia() {
            return media;
        }

        public List<TrainingPhase> getTrainingPhases() {
            return tps;
        }

        public List<Team> getTeams() {
            return teams;
        }

    }

    public static class AsyncBundle {
        private int mode;
        private SimpleAsyncBundle sam;
        private CompositeAsyncBundle cam;


        public AsyncBundle(int mode,
                           List<Member> members,
                           List<Team> teams,
                           List<Media> media,
                           List<TrainingPhase> tps) {
            this.cam = new CompositeAsyncBundle(members, teams, media, tps);
            this.mode = mode;
            this.sam = null;
        }

        public AsyncBundle(int mode,
                           SimpleAsyncBundle sam) {
            this.mode = mode;
            this.cam = null;
            this.sam = sam;
        }

        public AsyncBundle(int mode) {
            this.mode = mode;
            this.cam = null;
            this.sam = null;
        }

        public int getMode() {
            return mode;
        }

        public CompositeAsyncBundle getCompositeAsyncBundle() {
            return cam;
        }

        public SimpleAsyncBundle getSimpleAsyncBundle() {
            return sam;
        }
    }


}
