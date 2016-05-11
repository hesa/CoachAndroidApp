package com.sandklef.coachapp.Session;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sandklef.coachapp.Auth.Authenticator;
import com.sandklef.coachapp.model.Club;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.LocalStorageSync;
import com.sandklef.coachapp.storage.Storage;

/**
 * Created by hesa on 2016-05-10.
 */
public class CoachAppSession {

    private static final String url = "https://app.tranarappen.se/api/";
    private static Context context;

    private final static String LOG_TAG = CoachAppSession.class.getSimpleName();

    private final static int COACHAPP_SESSION_STATUS_OK            = 0;
    private final static int COACHAPP_SESSION_STATUS_NO_NETWORK    = 1;
    private final static int COACHAPP_SESSION_STATUS_INVALID_TOKEN = 2;


    public static void init(Context inContext) {
        Log.d(LOG_TAG, "init, context: " + inContext);
        context = inContext;
        LocalStorage.newInstance(context);
        LocalStorage.getInstance().setServerUrl(url);
        LocalStorageSync.newInstance(context);

//        LocalStorage.getInstance().setLatestUserToken("");
        Storage.newInstance(context);
        Log.d(LOG_TAG, "init, context: " + context);
    }

    public static void startUp(String email, String token){
//        Storage.getInstance().setClubUuid(club.getUuid());
        //      LocalStorage.getInstance().setCurrentClub(club.getUuid());

        if (email!=null) {
            LocalStorage.getInstance().setLatestUserEmail(email);
        }
        if (token!=null) {
            LocalStorage.getInstance().setLatestUserToken(token);
        }

        Log.d(LOG_TAG, "Stored: " + email + "|" + token);
    }


    public static void setupActivity() {
        Log.d(LOG_TAG, "setupActivity()");
        Log.d(LOG_TAG, "setupActivity() club: " + LocalStorage.getInstance().getCurrentClub());

        Storage.getInstance().setClubUuid(LocalStorage.getInstance().getCurrentClub());
    }


    public static void verifyToken(String token) {
        new TokenVerifier().execute(token);
    }

    public static class TokenVerifier extends AsyncTask<String, Void, Void> {

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

}
