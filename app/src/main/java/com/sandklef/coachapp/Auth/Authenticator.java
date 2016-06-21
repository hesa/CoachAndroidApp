package com.sandklef.coachapp.Auth;

import com.sandklef.coachapp.Session.CoachAppSession;
import com.sandklef.coachapp.activities.ActivitySwitcher;
import com.sandklef.coachapp.json.JsonAccess;
import com.sandklef.coachapp.json.JsonAccessException;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Club;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.Storage;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hesa on 2016-05-05.
 */
public class Authenticator {

    private final static String LOG_TAG = Authenticator.class.getSimpleName();

    public final static int NETWORK_ERROR = 1;  // netowrk down, http problem
    public final static int ACCESS_ERROR  = 2;  // server responds "not ok"

    private static Authenticator instance;

    private Authenticator() {;}

    public static Authenticator getInstance() {
        if (instance==null) {
            instance = new Authenticator();
        }
        return instance;
    }

    // TODO: really, this is not good ;)
    /*public boolean checkToken(String token) {
        return true;
    }*/

    public int verifyToken(String token) {
        Log.d(LOG_TAG, "verifyToken()");
        try {

            CoachAppSession.getInstance().setClubs(null);
            JsonAccess jsa = new JsonAccess();
            List<Club> clubs = jsa.getClubs(token);
            CoachAppSession.getInstance().setClubs(clubs);
            Log.d(LOG_TAG, "Clubs: " + Arrays.toString(clubs.toArray()));

            List<String> clubsStrings = new ArrayList<String>();
            for (Club c : clubs) {
                Log.d(LOG_TAG, "Club: " + c.getName() + " " + c.getUuid());
                clubsStrings.add(c.getUuid());
            }

            Club primaryClub = clubs.get(0);

            LocalStorage.getInstance().setCurrentClub(primaryClub.getClubUuid());
            LocalStorage.getInstance().setCurrentClubName(primaryClub.getName());



            Log.d(LOG_TAG, "Club set for use in app: " + LocalStorage.getInstance().getCurrentClub());

            ActivitySwitcher.printDb("Authenticator");
            Log.d(LOG_TAG, "verifyToken()  return 0");
            return 0; // SUCCESSS
        } catch (JsonAccessException e) {

            if ( e.getMode() == JsonAccessException.ACCESS_ERROR) {
                Log.d(LOG_TAG, "verifyToken()  return ACCESS_ERROR");
                return ACCESS_ERROR;
            } else if ( e.getMode() == JsonAccessException.NETWORK_ERROR) {
                Log.d(LOG_TAG, "verifyToken()  return NETWORK_ERROR");
                return NETWORK_ERROR;
            }
        }
        Log.d(LOG_TAG, "verifyToken()  return -1");
        return -1;
    }




}
