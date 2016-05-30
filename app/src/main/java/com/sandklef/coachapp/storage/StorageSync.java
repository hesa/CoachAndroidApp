package com.sandklef.coachapp.storage;

import android.os.AsyncTask;

import com.sandklef.coachapp.Session.CoachAppSession;
import com.sandklef.coachapp.misc.Log;

import java.util.Random;

/**
 * Created by hesa on 2016-05-30.
 */
public class StorageSync extends AsyncTask<Void, Void, StorageSync.StorageSyncBundle> {

    private static int OK           = 0;
    private static int NOT_DONE     = 1;
    private static int INTERRUPTED  = 2;
    private static int STORAGE_ERR  = 4;
    private static int CLUB_NOT_SET = 3;

    private final static String LOG_TAG = StorageSync.class.getSimpleName();
    private StorageSyncListener listener;

    public StorageSync(StorageSyncListener l) {
        listener = l;
    }

    @Override
    protected void onPostExecute(StorageSyncBundle  bundle) {
        if (listener!=null) {
            listener.syncFinished(bundle);
        }
    }

    @Override
    protected StorageSyncBundle doInBackground(Void... params) {


        if (CoachAppSession.getInstance().isSyncAllowed()) {
            try {

                int localMediaCount = 0;
                int tpMediaCount = 0;

                try {
                    localMediaCount = Storage.getInstance().getMedia().size();
                } catch (StorageNoClubException e) {
                    return new StorageSyncBundle(CLUB_NOT_SET);
                }

                // ENTER SYNC MODE
                CoachAppSession.getInstance().setSyncMode();

                // Upload local media
                LocalStorageSync.getInstance().syncLocalMediaSync();

/*
                // TODO: Remove --------------------------------------
                int nr = (new Random().nextInt(5) + 2) * 2;
                while (nr > 0) {
                    if (!CoachAppSession.getInstance().getSyncMode()) {
                        Log.d(LOG_TAG, "Uh oh Sync interrupted");
                        return new StorageSyncBundle(INTERRUPTED);
                    }
                    Log.d("StorageSync", "   sleep... " + nr + " left");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block

                        e.printStackTrace();
                    }
                    nr--;
                }
                // TODO: EO Remove --------------------------------
*/
                // Download TrainingPhase videos
                Storage.getInstance().downloadTrainingPhaseFiles();

                // LEAVE SYNC MODE
                CoachAppSession.getInstance().unsetSyncMode();

            } catch (StorageException e) {
                return new StorageSyncBundle(STORAGE_ERR);
            }

        }
        return new StorageSyncBundle(OK);
    }




    public class StorageSyncBundle {


        private String  msg;
        private int     fileToUpload;
        private int     filesUploaded;
        private int     fileToDownload;
        private int     filesDownloaded;
        private int     errorCode;

        public StorageSyncBundle(
                String  msg,
                int     fileToUpload,
                int     filesUploaded,
                int     fileToDownload,
                int     filesDownloaded,
                int     errorCode) {
            this.msg              = msg;
            this.fileToUpload     = fileToUpload;
            this.filesUploaded    = filesUploaded;
            this.fileToDownload   = fileToDownload;
            this.filesDownloaded  = filesDownloaded;
            this.errorCode        = errorCode;
        }

        public StorageSyncBundle(int     errorCode) {
            this.errorCode        = errorCode;
        }

        public String  getMsg()             {return msg;};
        public int     getFileToUpload()    {return fileToUpload;};;
        public int     getFilesUploaded()   {return filesUploaded;};
        public int     getFileToDownload()  {return fileToDownload;};
        public int     getFilesDownloaded() {return filesDownloaded;};
        public int     getErrorCode()       {return errorCode;};
        public boolean getSuccess()         {return filesDownloaded==fileToDownload && filesUploaded==fileToDownload; };
    }

}
