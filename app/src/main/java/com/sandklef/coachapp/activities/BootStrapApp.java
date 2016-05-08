package com.sandklef.coachapp.activities;

import android.content.Context;
import android.util.Log;

import com.sandklef.coachapp.model.Club;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.LocalStorageSync;
import com.sandklef.coachapp.storage.Storage;

/**
 * Created by hesa on 2016-04-28.
 */
public class BootStrapApp {

    private static final String url = "https://app.tranarappen.se/api/";
    private static Context context;

    private final static String LOG_TAG = BootStrapApp.class.getSimpleName();

    public static void init(Context inContext) {
        Log.d(LOG_TAG, "init, context: " + inContext);
        context = inContext;
        LocalStorage.newInstance(context);
        LocalStorage.getInstance().setServerUrl(url);
        LocalStorageSync.newInstance(context);

        LocalStorage.getInstance().resetSessionToken();
        Storage.newInstance(context);
        Log.d(LOG_TAG, "init, context: " + context);
    }

    public static void initClub(Club club) {

        //      LocalStorage.getInstance().setServerUrl("http://129.16.219.77:3000/0.0.0/");
//        LocalStorage.getInstance().setServerUrl("http://192.168.1.111:3000/0.0.0/");

        Storage.getInstance().setClubUuid(club.getUuid());
        LocalStorage.getInstance().setCurrentClub(club.getUuid());
    }

}
