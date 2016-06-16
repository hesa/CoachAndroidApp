package com.sandklef.coachapp.Session;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.TextView;

import com.sandklef.coachapp.Auth.Authenticator;
import com.sandklef.coachapp.activities.ActivitySwitcher;
import com.sandklef.coachapp.json.JsonAccessException;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.report.ReportUser;
import com.sandklef.coachapp.storage.ConnectionStatusListener;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.LocalStorageSync;
import com.sandklef.coachapp.storage.Storage;
import com.sandklef.coachapp.storage.StorageNoClubException;
import com.sandklef.coachapp.storage.StorageSync;
import com.sandklef.coachapp.storage.StorageSyncListener;
import com.sandklef.coachapp.storage.StorageUpdateListener;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import coachassistant.sandklef.com.coachapp.R;

/**
 * Created by hesa on 2016-05-10.
 */
public class CoachAppSession  implements ConnectionStatusListener, StorageSyncListener {

    private final String url = "https://app.tranarappen.se/api/";

    private static CoachAppSession instance;

    private final static String LOG_TAG = CoachAppSession.class.getSimpleName();

    private final static int COACHAPP_SESSION_STATUS_OK = 0;
    private final static int COACHAPP_SESSION_STATUS_NO_NETWORK = 1;
    private final static int COACHAPP_SESSION_STATUS_INVALID_TOKEN = 2;

    private Context   context;
    private Activity  currentActivity;
    private Menu      currentTopMenu;
    private MenuItem  currentTopMenuItem;
    private int       serverConnectionStatus;

    /**
     *
     */
    private static AlertDialog  dialog;
    private static ProgressDialog progress;
    private static boolean dialogInUse;
    private static int     dialogUser;

    private boolean syncMode;

    private CoachAppSession() {
        ;
    }

    public static CoachAppSession getInstance() {
        if (instance==null) {
            instance = new CoachAppSession();
        }
        return instance;
    }

    public void init(Context inContext) {
        Log.d(LOG_TAG, "init, context: " + inContext);
        context = inContext;
        LocalStorage.newInstance(context);
        LocalStorage.getInstance().setServerUrl(url);
        LocalStorageSync.newInstance(context);
        serverConnectionStatus = JsonAccessException.OK;

//        LocalStorage.getInstance().setLatestUserToken("");
        Storage.newInstance(context);
        Log.d(LOG_TAG, "init, context: " + context);
    }

    public Context getContext() {
        return context;
    }

    public void startUp(String email, String token) {

        Log.d(LOG_TAG, "startUp: Email: " + email + " Token: " + token);

//        Storage.getInstance().setClubUuid(club.getUuid());
        //      LocalStorage.getInstance().setCurrentClub(club.getUuid());

        if (email != null) {
            LocalStorage.getInstance().setLatestUserEmail(email);
        }
        if (token != null) {
            LocalStorage.getInstance().setLatestUserToken(token);
        }

        Log.d(LOG_TAG, "Stored: " + email + "|" + token);
    }


    public boolean getSyncMode() {
        return syncMode;
    }

    public void setSyncMode() {
        syncMode = true;
    }

    public void unsetSyncMode() {
        Log.d(LOG_TAG, " ------------------- UNSET SYNC MODE");
        syncMode = false;
        closeDialog();
        //progress=null;
    }


    public void setupActivity(Activity actiivity) {
        Log.d(LOG_TAG, "setupActivity()     activity name: " + actiivity.getClass().getName());
        currentActivity = actiivity;

        if (LocalStorage.getInstance()==null) {
            ActivitySwitcher.startLoginActivity(actiivity);
        }

        if (LocalStorage.getInstance()!=null) {
            Log.d(LOG_TAG, "setupActivity() club: " + LocalStorage.getInstance().getCurrentClub());
        }
        context = actiivity;
        //        Storage.getInstance().setClubUuid(LocalStorage.getInstance().getCurrentClub());
    }


    public AlertDialog.Builder buildAlertDialog(int titleId, int textId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setMessage(context.getResources().getString(textId));
        builder.setTitle(context.getResources().getString(titleId));
        return builder;
    }


    public String getString(int id){
        return getContext().getResources().getString(id);
    }

    public void handleInvalidToken() {
        Log.d(LOG_TAG, "Dialog time?" + " activity: " + currentActivity);

        AlertDialog.Builder builder = buildAlertDialog(R.string.token_invalid, R.string.login_again_to_get_token);
        builder.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivitySwitcher.startLoginActivity(currentActivity);
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public void verifyToken(String token) {
        new TokenVerifier().execute(token);
    }

    @Override
    public void syncFinished(StorageSync.StorageSyncBundle bundle) {
        Log.d(LOG_TAG, "SYNC FINI ------------------------");
        Log.d(LOG_TAG, "SYNC FINI ------------------------ bundle: " + bundle.getErrorCode());
        closeDialog();
    }

    public class TokenVerifier extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String token = params[0];
            int tokenStatus = COACHAPP_SESSION_STATUS_OK;
            int validToken = Authenticator.getInstance().verifyToken(token);

            if (validToken == Authenticator.NETWORK_ERROR) {
                LocalStorage.getInstance().setConnectionStatus(COACHAPP_SESSION_STATUS_NO_NETWORK);
            } else if (validToken == Authenticator.ACCESS_ERROR) {
                LocalStorage.getInstance().setConnectionStatus(COACHAPP_SESSION_STATUS_INVALID_TOKEN);
            } else {
                LocalStorage.getInstance().setConnectionStatus(COACHAPP_SESSION_STATUS_OK);
            }
            return null;
        }
    }

    public void setSyncDialog(int tot, String title) {
        Log.d(LOG_TAG, "setSyncDialog()" + progress);
       // closeDialog();
        getSyncDialog();
        Log.d(LOG_TAG, "setSyncDialog()" + progress);
        progress.setMax(tot);
        progress.setTitle(title);
    }

    public Dialog getSyncDialog() {
        Log.d(LOG_TAG, "get SyncDialog()" + progress);
        if (progress == null) {
            progress = new ProgressDialog(getContext());
            progress.setIndeterminate(false);
//            progress.setCanceledOnTouchOutside(true);
            progress.setCancelable(false);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    unsetSyncMode();
                }
            });
            progress.create();
            progress.show();
        }
        Log.d(LOG_TAG, "get SyncDialog()" + progress + " " + progress.getProgress());
        return progress;
    }

    public Dialog getSyncDialogA(){
        Log.d(LOG_TAG, "get SyncDialog()");
        if (dialog==null) {
            AlertDialog.Builder builder = CoachAppSession.getInstance().buildAlertDialog(R.string.sync_title, R.string.sync_in_progress);
            builder.setNegativeButton(CoachAppSession.getInstance().getContext().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(LOG_TAG, "User pressed cancel, will quit sync");
                    CoachAppSession.getInstance().unsetSyncMode();
                    // User cancelled the dialog
                }
            });
            dialog = builder.create();
            Log.d(LOG_TAG, "Creating dialog: " + dialog);
            setSyncMode();
            dialog.setCancelable(false);
            dialog.show();
            dialogInUse=true;
        } else {
            if(dialogInUse) {
                Log.d(LOG_TAG, "Dialog already in use");
            }
        }
        return dialog;
    }

    public void addDialoguser() {
        dialogUser++;
        Log.d(LOG_TAG, "addDialoguser()   SyncDiag users: " + dialogUser + "  what the fuck????");
    }

    public void closeDialog(){
        Log.d(LOG_TAG, "closeDialog" + progress);

        if (dialogUser>0) {
            dialogUser--;
        }
        Log.d(LOG_TAG, "close SyncDialog()   users: " + dialogUser);
        Log.d(LOG_TAG, "Closing dialog: " + dialog);
        if (dialogUser==0) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
            if (progress != null) {
                progress.dismiss();
               progress = null;
            }
        }
        dialogInUse=false;
    }

    public void setDialogText(String s) {
        Log.d(LOG_TAG, "setSyncDialogText()" + progress);

        progress.setProgress(progress.getProgress()+1);
        progress.show(getContext(), "Syncing", s);

        /*
        TextView tv = new TextView(currentActivity);
        tv.setText(s);
        if (dialog!=null) {
            dialog.setMessage(s);
        } else {
            Log.d(LOG_TAG, "setDialogText  from onProgressUpdate dialog null, text: " + s);
        }
        */
    }

    public void setDialogInfo(int cnt, String text) {
        Log.d(LOG_TAG, "setSyncDialogInfo()" + progress);
        getSyncDialog();
        if (progress != null) {
            progress.setProgress(cnt);
/*            if (!progress.isShowing()) {
                progress.show();
            }
*/

//            Log.d(LOG_TAG, "setDialogInfo: " + cnt + " " + progress.getMax());

        /*
        TextView tv = new TextView(currentActivity);
        tv.setText(s);
        if (dialog!=null) {
            dialog.setMessage(s);
        } else {
            Log.d(LOG_TAG, "setDialogText  from onProgressUpdate dialog null, text: " + s);
        }
        */
        }
    }

    public void updateFromServer(
            Activity activity,
            StorageUpdateListener sl,
            ConnectionStatusListener cl) {

        Log.d(LOG_TAG, "updateFromServer  serverConnectionStatus:" + serverConnectionStatus);
        ReportUser.log("Refreshing data", "Getting inormation about training phases, teams and members from server.");

        if (serverConnectionStatus == JsonAccessException.ACCESS_ERROR ) {
            ReportUser.warning(activity,
                    context.getResources().getString(R.string.user_credentials_faulty),
                    "User credentials were wrong so we've skipped update from server");
            CoachAppSession.getInstance().handleInvalidToken();
            return;
        }

        if (isOnline() ) {
            if (serverConnectionStatus == JsonAccessException.NETWORK_ERROR ) {
                ReportUser.inform(activity,
                        context.getResources().getString(R.string.network_down_server_up));
                Log.d(LOG_TAG, "Network is up, but server seems to be down");
            } else {
                Log.d(LOG_TAG, "Update from server, sync mode?: " + CoachAppSession.getInstance().getSyncMode());
                if (!CoachAppSession.getInstance().getSyncMode()) {
                    ReportUser.inform(activity,
                            context.getResources().getString(R.string.getting_data_from_server));
                }
                Log.d(LOG_TAG, "Initiate update from server");
                Storage.getInstance().update(activity, sl, cl);
            }
        } else {
            Log.d(LOG_TAG, "No network, will not sync");
        }
    }

    public void syncServer(
            Activity activity,
            StorageUpdateListener sl,
            ConnectionStatusListener cl) {

        Log.d(LOG_TAG, "syncServer  serverConnectionStatus:" + serverConnectionStatus);

        if (serverConnectionStatus == JsonAccessException.ACCESS_ERROR ) {
            ReportUser.warning(activity,
                    context.getResources().getString(R.string.error_incorrect_password),
                    "User credentials were wrong so we've skipped update from server");
            return;

        }

        if (isWifi() ) {
            if (serverConnectionStatus == JsonAccessException.NETWORK_ERROR ) {
                ReportUser.inform(activity,
                        context.getResources().getString(R.string.network_down_server_up));
                Log.d(LOG_TAG, "Network is up, but server seems to be down");
            } else {
                ReportUser.inform(activity,
                        context.getResources().getString(R.string.sync_started));

                LocalStorageSync.getInstance().syncLocalStorage();
            }
        } else {
            Log.d(LOG_TAG, "No network, will not sync");
        }
    }

/*
    public void updateConnectionStatusTopMenu(
            Activity activity,
            MenuItem item) {
        updateConnectionStatusTopMenu(activity, item, serverConnectionStatus);
    }
*/


    private void updateConnectionStatusImpl(int status) {
        Log.d(LOG_TAG, "updateConnectionStatusImpl");
        MenuItem item  =  currentTopMenuItem;
        serverConnectionStatus = status;
        boolean wifiConnected = isWifi();

        if (item != null) {
            Log.d(LOG_TAG, "updateConnectionStatusImpl not null");
            if (!isSyncAllowed()) {
                item.setIcon(R.drawable.ic_sync_off_black_24dp);
                return;
            }

            // We have wifi or allowed to use gprs/3g..
            if (status == JsonAccessException.OK) {
                item.setIcon(R.drawable.ic_sync_black_24dp);
            } else if (status == JsonAccessException.ACCESS_ERROR) {
                item.setIcon(R.drawable.ic_sync_alert_black_24dp);
                CoachAppSession.getInstance().handleInvalidToken();
            } else if (status == JsonAccessException.NETWORK_ERROR) {
                item.setIcon(R.drawable.ic_sync_off_black_24dp);
            }
        } else {
            com.sandklef.coachapp.misc.Log.d(LOG_TAG, "onConnectionStatusUpdate  could not set status since menu item NULL");
        }

    }

    public void updateConnectionStatusTopMenu(int status) {
        updateConnectionStatusImpl(status);
    }

    public void updateConnectionStatusMemberMenu(int status, int menuId) {

        updateConnectionStatusImpl(status);
    }


    public void setupActivity(Activity actiivity, Menu topMenu, int id) {
        currentActivity     = actiivity;
        currentTopMenu      = topMenu;
        currentTopMenuItem  = topMenu.findItem(id);

        Log.d(LOG_TAG, "setting up activity: menu: " + topMenu);

        updateConnectionStatusTopMenu(serverConnectionStatus);
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }


    @Override
    public void onConnectionStatusUpdate(int status) {
        com.sandklef.coachapp.misc.Log.d(LOG_TAG, "onConnectionStatusUpdate  status: " + status);
        serverConnectionStatus = status;

        Log.d(LOG_TAG, " onConnectionStatusUpdate()");
        if (getCurrentActivity().getClass()==com.sandklef.coachapp.activities.MemberActivity.class)  {
            Log.d(LOG_TAG, " onConnectionStatusUpdate()   MemberActivity");
        } else {
            Log.d(LOG_TAG, " onConnectionStatusUpdate()   NOT MemberActivity");
        }

        updateConnectionStatusTopMenu(status);
    }


    private NetworkInfo activeNetwork;
    private ConnectivityManager cm ;

    private void networkCommonCheck() {
        cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public boolean isWifi() {
//        if (isEmulator()) { return true;}

        networkCommonCheck();

        if (activeNetwork==null) {
            return false;
        }

        /*
        Log.d(LOG_TAG, "iwWifi() " + isOnline());
        Log.d(LOG_TAG, "iwWifi() " + activeNetwork.getType());
        Log.d(LOG_TAG, "iwWifi() " + ConnectivityManager.TYPE_WIFI);
        Log.d(LOG_TAG, "iwWifi() " + (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI));
*/
        return isOnline() && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public boolean isOnline() {
        networkCommonCheck();
        if (activeNetwork==null) {
            return false;
        }

        return  activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public void goBackToActivity(){
        Log.d(LOG_TAG, " -- switch from "+ currentActivity.getClass().getName());
        if (currentActivity.getClass()==com.sandklef.coachapp.activities.MemberActivity.class) {
            Log.d(LOG_TAG, " -- switch from Member");
            ActivitySwitcher.startTrainingPhaseActivity(currentActivity);
        } else if (currentActivity.getClass()==com.sandklef.coachapp.activities.TrainingPhasesActivity.class) {
            Log.d(LOG_TAG, " -- switch from TrainingPhase");
            ActivitySwitcher.startTeamActivity(currentActivity);
        }
    }

    public boolean isSyncAllowed() {
        boolean wifi       = isWifi();
        boolean syncNoWifi = (!LocalStorage.getInstance().getSyncOnWifiOnly());
        boolean online     =  isOnline();
        boolean allowSync  = ( wifi || (syncNoWifi && online)) && (serverConnectionStatus == JsonAccessException.OK) ;

        Log.d(LOG_TAG, " has wifi:     " + wifi);
        Log.d(LOG_TAG, " sync no wifi: " + syncNoWifi);
        Log.d(LOG_TAG, " online:       " + online);
        Log.d(LOG_TAG, " Allow:        " + allowSync);
        return allowSync;
    }

    public boolean handleTopMenu(MenuItem item, StorageUpdateListener l) {
        com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  onOptionsItemSelected: " + item.getItemId());
        // Handle item selection

        if (LocalStorage.getInstance()==null) {
            ActivitySwitcher.startTrainingActivity(currentActivity);
        }

        switch (item.getItemId()) {
            case R.id.menu_log_file:
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  log");
                ActivitySwitcher.startLogMessageActivity(currentActivity);
                return true;
            case R.id.menu_media_manager:
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  media");
                ActivitySwitcher.startLocalMediaManager(currentActivity);
                return true;
            case R.id.menu_club_info:
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  club");
                ActivitySwitcher.startClubInfoActivity(currentActivity);
                return true;
            case R.id.menu_refresh:
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  refresh -- menu");
                CoachAppSession.getInstance().updateFromServer(currentActivity, l, CoachAppSession.getInstance());
                return true;
            case R.id.menu_destroy_token:
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  destroy token");
                LocalStorage.getInstance().setLatestUserToken("");
                return true;
            case R.id.topsync:
                syncAll();
                return true;
            case R.id.menu_settings:
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  goto settings");
                ActivitySwitcher.startSettingsActivity(currentActivity);
                return true;
            default:
                goBackToActivity();

                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  default handled");
                return true;
        }
    }


    public void syncAll() {
        com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  sync");
        if (isSyncAllowed()) {
            Log.d(LOG_TAG, "Dialog time?" + " activity: " + currentActivity);
            Log.d(LOG_TAG, "----> all sync methods will be called");


            //TODO: REFRESH DATA FROM SERVER AS WELL AS SYNC FILES
//                    CoachAppSession.getInstance().updateFromServer(currentActivity, l, CoachAppSession.getInstance());
            try {
                new StorageSync(this).execute();
                // SYNC HERE
                CoachAppSession.getInstance().getSyncDialog();

                Log.d(LOG_TAG, "<---- all sync methods called");
            } catch (StorageNoClubException e) {
                e.printStackTrace();
                // FIXME: 2016-06-08
            }
        } else {
            ReportUser.warning(context,
                    context.getResources().getString(R.string.no_network_sync),
                    "To limit bandwidth useage when on mobile network, you can only sync with wifi");
        }
    }


    public String newFileName() {
        DateFormat df = new SimpleDateFormat(ActivitySwitcher.VIDEO_FILE_DATE_FORMAT);
        Date today = Calendar.getInstance().getTime();
        String mediaDate = df.format(today);

        String newFileName = LocalStorage.getInstance().getNewMediaDir() + "/" +
                mediaDate + ActivitySwitcher.VIDEO_FILE_TYPE_SUFFIX;

        File newFile = new File(newFileName);
        String dirName = newFile.getParent();
        File dir = new File(dirName);

        com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  Dir:  " + dir.getPath());
        boolean created = dir.mkdirs();

        com.sandklef.coachapp.misc.Log.d(LOG_TAG, "RECORD TO NEW FILE: " + newFile);

        return newFileName;
    }

    public int orientationDegrees(int orientation) {
        int degrees = 0 ;
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                degrees = 0;
                break;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                degrees = 180;
                break;
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                degrees = 90;
                break;
            default:
                degrees = 180;
                break;
        }

        Log.d(LOG_TAG, " Screen orientation: " + orientation);
        Log.d(LOG_TAG, " * landscape: " + ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Log.d(LOG_TAG, " * rev land:  " + ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        Log.d(LOG_TAG, " * portrait:  " + ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d(LOG_TAG, " Degress:     " + degrees);

        return degrees ;
    }

    public int orientationDegrees() {
        return  orientationDegrees(getScreenOrientation());
    }

    public int getScreenOrientation() {
        /*
*     Not my (c)
 *   http://stackoverflow.com/questions/10380989/how-do-i-get-the-current-orientation-activityinfo-screen-orientation-of-an-a
     */
        int rotation = currentActivity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    Log.e(LOG_TAG, "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    Log.e(LOG_TAG, "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

}
