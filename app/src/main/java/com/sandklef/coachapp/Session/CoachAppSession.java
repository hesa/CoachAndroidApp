package com.sandklef.coachapp.Session;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;

import com.sandklef.coachapp.Auth.Authenticator;
import com.sandklef.coachapp.activities.ActivitySwitcher;
import com.sandklef.coachapp.json.JsonAccessException;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.report.ReportUser;
import com.sandklef.coachapp.storage.ConnectionStatusListener;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.LocalStorageSync;
import com.sandklef.coachapp.storage.Storage;
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
public class CoachAppSession  implements ConnectionStatusListener {

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
    private static Dialog  dialog;
    private static boolean dialogInUse;

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


    public void setupActivity(Activity actiivity) {
        Log.d(LOG_TAG, "setupActivity()");
        currentActivity = actiivity;
        if (LocalStorage.getInstance()!=null) {
            Log.d(LOG_TAG, "setupActivity() club: " + LocalStorage.getInstance().getCurrentClub());

        }
        //        Storage.getInstance().setClubUuid(LocalStorage.getInstance().getCurrentClub());
    }


    public AlertDialog.Builder buildAlertDialog(int titleId, int textId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setMessage(context.getResources().getString(textId));
        builder.setTitle(context.getResources().getString(titleId));
        return builder;
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

    public Dialog getSyncDialog(){
        Log.d(LOG_TAG, "getAlertDialog()");
        if (dialog==null) {
            AlertDialog.Builder builder = CoachAppSession.getInstance().buildAlertDialog(R.string.sync_title, R.string.sync_in_progress);
            builder.setNegativeButton(CoachAppSession.getInstance().getContext().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            dialog = builder.create();
            Log.d(LOG_TAG, "Creating dialog: " + dialog);
            dialog.show();
            dialogInUse=true;
        } else {
            if(dialogInUse) {
                Log.d(LOG_TAG, "Dialog already in use");
            }
        }
        return dialog;
    }



    public void closeDialog(){
        Log.d(LOG_TAG, "Closing dialog: " + dialog);
        if (dialog!=null) {
            dialog.dismiss();
            dialog=null;
        }
        dialogInUse=false;
    }

    public void updateFromServer(
            Activity activity,
            StorageUpdateListener sl,
            ConnectionStatusListener cl) {

        Log.d(LOG_TAG, "updateFromServer  serverConnectionStatus:" + serverConnectionStatus);

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
                ReportUser.inform(activity,
                        context.getResources().getString(R.string.getting_data_from_server));
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
                    context.getResources().getString(R.string.getting_data_from_server),
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
            if (!isWifi()) {
                item.setIcon(R.drawable.ic_sync_off_black_24dp);
                return;
            }

            // We have wifi
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

    public boolean isWifi() {
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

    public boolean isSyncAllowed() {
        return isWifi() && serverConnectionStatus == JsonAccessException.OK;
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
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  sync");
                if (CoachAppSession.getInstance().isWifi()) {
                    Log.d(LOG_TAG, "Dialog time?" + " activity: " + currentActivity);
                    Log.d(LOG_TAG, "all sync methods will be called");

                    CoachAppSession.getInstance().getSyncDialog();
                    LocalStorageSync.getInstance().syncLocalStorage();
                    Storage.getInstance().downloadTrainingPhaseFiles();

                    Log.d(LOG_TAG, "all sync methods called");
                } else {
                    ReportUser.warning(context,
                            context.getResources().getString(R.string.no_network_sync),
                            "To limit bandwidth useage when on mobile network, you can only sync with wifi");
                }
                return true;
            default:
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  default");
                currentActivity.finish();
                com.sandklef.coachapp.misc.Log.d(LOG_TAG, "  default handled");
                return true;
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



}
