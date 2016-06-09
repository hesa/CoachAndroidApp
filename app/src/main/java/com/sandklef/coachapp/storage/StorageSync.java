package com.sandklef.coachapp.storage;

import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.sandklef.coachapp.Session.CoachAppSession;
import com.sandklef.coachapp.json.JsonAccessException;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.TrainingPhase;

import java.util.List;
import java.util.Random;

import coachassistant.sandklef.com.coachapp.R;

/**
 * Created by hesa on 2016-05-30.
 */
public class StorageSync extends AsyncTask<Void, StorageSync.StorageSyncBundle, StorageSync.StorageSyncBundle> {

    private static int OK           = 0;
    private static int NOT_DONE     = 1;
    private static int INTERRUPTED  = 2;
    private static int STORAGE_ERR  = 4;
    private static int CLUB_NOT_SET = 3;

    private static int STORAGE_SYNC_DOWNLOAD = 0;
    private static int STORAGE_SYNC_UPLOAD   = 1;

    private ProgressBar progressBar;

    private final static String LOG_TAG = StorageSync.class.getSimpleName();
    private StorageSyncListener listener;

    List<TrainingPhase> trainingPhases;
    List<Media>         mediaToUpload;

    private int uploadedTPs;
    private int downloadedInstructions;

    public StorageSync(StorageSyncListener l) throws StorageNoClubException {
        listener       = l;
        trainingPhases = Storage.getInstance().getTrainingPhases();
        mediaToUpload  = Storage.getInstance().getLocalMedia();

        CoachAppSession.getInstance().setSyncDialog(trainingPhases.size()+mediaToUpload.size(),
                CoachAppSession.getInstance().getContext().getString(R.string.sync_in_progress));
    }

    @Override
    protected void onPostExecute(StorageSyncBundle  bundle) {
        Log.d(LOG_TAG, " ------------------------------------------------------------------ onPostExecute");
        if (listener!=null) {
            listener.syncFinished(bundle);
        }
    }


    public void syncAllMedia() throws StorageException {

        downloadedInstructions = 0;

        uploadedTPs = syncLocalMediaSync();

        Log.d(LOG_TAG, "Local files synced: " + uploadedTPs);

        // Download TrainingPhase videos
        downloadTrainingPhaseFiles();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Storage.getInstance().reReadMedia();
    }


    private void updateProgress(int mode, int downloadCnt, int uploadCnt) {
                        /*
                        public StorageSyncBundle(
                int     mode,
                int     fileToUpload,
                int     filesUploaded,
                int     fileToDownload,
                int     filesDownloaded,
                int     errorCode) {
                 */
        StorageSync.StorageSyncBundle bundle = new StorageSync.StorageSyncBundle(
                mode,
                trainingPhases.size(),
                downloadCnt,
                mediaToUpload.size(),
                uploadCnt,
                OK);

       // onProgressUpdate(bundle);


        publishProgress(bundle);
    }

    public void downloadTrainingPhaseFiles() {
        Log.d(LOG_TAG, "download tp videos");
//        StorageRemoteWorker srw = null;


        if (trainingPhases == null) {
            Log.d(LOG_TAG, "No data downlaoded.... Missing information about your club and team(s). Refresh");
            return;
        }
        int cnt = 0;
        Log.d(LOG_TAG, "download tp videos: " + trainingPhases.size());

        for (TrainingPhase tp : trainingPhases) {

            cnt++;
            if (!CoachAppSession.getInstance().getSyncMode()) {
                Log.d(LOG_TAG, "Uh oh download tp interrupted");
                return;
            }

            updateProgress(STORAGE_SYNC_DOWNLOAD, cnt, uploadedTPs);



            Log.d(LOG_TAG, "Syncing (dload) file: " + cnt + " of " + trainingPhases.size() + " files");

            Media m = Storage.getInstance().getInstructionalMedia(tp.getUuid());
            if (m == null || m.fileName() == null) {
                cnt++;
                Log.d(LOG_TAG, "Syncing (dload) file: " + cnt + " of " + trainingPhases.size() + " files");
                Storage.getInstance().downloadMediaFromServer(CoachAppSession.getInstance().getContext(), m);
            } else {
                Log.d(LOG_TAG, "No need to download: " + tp);
                Log.d(LOG_TAG, "No need to download: " + m.fileName());
            }
        }
    }


    public int syncLocalMediaSync() throws StorageException {
        int cnt=0;
        try {
            Storage.getInstance().reReadMedia();
            Log.d(LOG_TAG, "syncLocalMediaSync");
            for (Media m: mediaToUpload) {
                Log.d(LOG_TAG, "Syncing file: " + ++cnt + " of " + Storage.getInstance().getMedia().size() + " files");
//                publishProgress ("Syncing. Uploading local video " + cnt + " of " + Storage.getInstance().getMedia().size());

                updateProgress(STORAGE_SYNC_UPLOAD, 0, cnt);
                if (CoachAppSession.getInstance().getSyncMode()) {
                    LocalStorageSync.getInstance().handleMediaSync(m);
                } else {
                    Log.d(LOG_TAG, "Sync mode not set, discarding media: " + m);
                }

            }
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }



        return cnt;
    }


    @Override
    protected StorageSyncBundle doInBackground(Void... params) {


        if (!CoachAppSession.getInstance().isSyncAllowed()) {
            return new StorageSyncBundle(OK);
        }
        try {

            int localMediaCount = 0;
            int tpMediaCount = 0;

            try {
                localMediaCount = Storage.getInstance().getLocalMedia().size();
            } catch (StorageNoClubException e) {
                return new StorageSyncBundle(CLUB_NOT_SET);
            }

            // Set sync mode
            CoachAppSession.getInstance().setSyncMode();

            // Upload local media
            syncAllMedia();

            // LEAVE SYNC MODE
            CoachAppSession.getInstance().unsetSyncMode();


        } catch (StorageException e) {
            return new StorageSyncBundle(STORAGE_ERR);
        }
        return new StorageSyncBundle(OK);
    }


    protected void onProgressUpdate(StorageSync.StorageSyncBundle ... progress) {
        StorageSync.StorageSyncBundle bundle = progress[0];

        Log.d(LOG_TAG, "Bundle: " + bundle + "  => " + bundle.getFilesDownloaded()+bundle.getFilesUploaded());

        String info = "";
        if (bundle.getMode() == STORAGE_SYNC_DOWNLOAD) {
            info += "Downloading " + bundle.getFilesDownloaded() + " / " + bundle.getFileToDownload() +  "\n";
        }else {
            info = "Uploading " + bundle.getFilesUploaded() + " / " + bundle.getFileToUpload() + "\n";
        }

        Log.d(LOG_TAG, "onProgressUpdate() " + info);

        CoachAppSession.getInstance().setDialogInfo(bundle.getFilesDownloaded()+bundle.getFilesUploaded(),
                CoachAppSession.getInstance().getContext().getString(R.string.sync_in_progress));


    }

    public class StorageSyncBundle {


        private int     mode;
        private int     fileToUpload;
        private int     filesUploaded;
        private int     fileToDownload;
        private int     filesDownloaded;
        private int     errorCode;

        public StorageSyncBundle(
                int     mode,
                int     fileToUpload,
                int     filesUploaded,
                int     fileToDownload,
                int     filesDownloaded,
                int     errorCode) {
            this.mode             = mode;
            this.fileToUpload     = fileToUpload;
            this.filesUploaded    = filesUploaded;
            this.fileToDownload   = fileToDownload;
            this.filesDownloaded  = filesDownloaded;
            this.errorCode        = errorCode;
        }

        public StorageSyncBundle(int     errorCode) {
            this.errorCode        = errorCode;
        }

        public int     getMode()            {return mode;}
        public int     getFileToUpload()    {return fileToUpload;}
        public int     getFilesUploaded()   {return filesUploaded;}
        public int     getFileToDownload()  {return fileToDownload;}
        public int     getFilesDownloaded() {return filesDownloaded;}
        public int     getErrorCode()       {return errorCode;}
        public boolean getSuccess()         {return filesDownloaded==fileToDownload && filesUploaded==fileToDownload; }

        public String toString() {
            return "mode: " + mode + " " + filesUploaded+ "/" + fileToUpload + "  " + filesDownloaded + "/" + fileToDownload ;
        }

    }



}