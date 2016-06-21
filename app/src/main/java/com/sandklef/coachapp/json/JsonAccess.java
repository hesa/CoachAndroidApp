package com.sandklef.coachapp.json;

import com.sandklef.coachapp.Session.CoachAppSession;
import com.sandklef.coachapp.http.HttpAccessException;
import com.sandklef.coachapp.model.*;
import com.sandklef.coachapp.misc.*;
import com.sandklef.coachapp.report.ReportUser;
import com.sandklef.coachapp.storage.*;
import com.sandklef.coachapp.http.HttpAccess;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.os.AsyncTask;

import coachassistant.sandklef.com.coachapp.R;


public class JsonAccess  {


    private final static String LOG_TAG = JsonAccess.class.getSimpleName();



    //    private Context context;
    private HttpAccess httpAccess;

    private String jsonString;

    /*
    public JsonAccess(String club ) throws JsonAccessException {
        if (club == null ) {
            throw new JsonAccessException(
                    "NULL pointer passed to constructor (" + club + ")");
        }
//        this.context = context;
        try {
            this.httpAccess =
                    new HttpAccess(LocalStorage.getInstance().getServerUrl(), club);
        } catch (HttpAccessException e) {
            throw new JsonAccessException("Could not access Http", e);
        }
    }
*/

    public JsonAccess() throws JsonAccessException {
        try {
            this.httpAccess =
                    new HttpAccess(LocalStorage.getInstance().getServerUrl());
        } catch (HttpAccessException e) {
            throw new JsonAccessException("Could not access Http", e, JsonAccessException.NETWORK_ERROR);
        }
    }

    public class CompositeBundle {
        public List<Member> members;
        public List<Team> teams;
//        public List<Media> media;
        public List<TrainingPhase> tps;
    }


    public  List<Club> getClubs(String token) throws JsonAccessException  {
        Log.d(LOG_TAG, "getClubs()");
        List<Club> clubs = new ArrayList<Club>();
        try {
            String jsonData = httpAccess.getClubs(token);

            Log.d(LOG_TAG, "data: " + jsonData);
            JSONObject json = new JSONObject(jsonData);

//            String clubJson = json.getJSONArray(JsonSettings.ITEMS_TAG);
  //          Log.d(LOG_TAG, " clubs (json):  " + clubJson);

            JSONArray clubsArray = json.getJSONArray(JsonSettings.ITEMS_TAG);
            Log.d(LOG_TAG, " clubs (array): " + clubsArray);

            for (int i = 0; i < clubsArray.length(); i++) {
                JSONObject jo = clubsArray.getJSONObject(i);
                String uuid = jo.getString(JsonSettings.UUID_TAG);
                String name = jo.getString(JsonSettings.NAME_TAG);
                Log.d(LOG_TAG, " * " + uuid + " - " + name);
                clubs.add(new Club(uuid, name));
            }
        } catch (JSONException e) {
            throw new JsonAccessException("Failed parsing JSON", e, JsonAccessException.ACCESS_ERROR);
        } catch (HttpAccessException e) {
            throw new JsonAccessException(e, e.getMode());
        }
        return clubs;
    }

    public CompositeBundle update(String clubUri) throws JsonAccessException {
        CompositeBundle bundle = new CompositeBundle();
        try {
                /*
                 *
                 *  Get data from server
                 *
                 */
            Log.d(LOG_TAG, "Read entire coach server");
            jsonString = readEntireCoachServer(clubUri);
            JSONObject json = new JSONObject(jsonString);

            Log.d(LOG_TAG, " json: " + jsonString);
                /*
                 *
                 *  Extract the data
                 *
                 */

            bundle.members = extractMembers(json);
            Log.d(LOG_TAG, "Members:  " + bundle.members.size() + "   " + bundle.members);
            for (Member m: bundle.members) {
                Log.d(LOG_TAG, " * " + m);
            }

            bundle.teams = extractTeams(json);
            Log.d(LOG_TAG, "Teams:  " + bundle.teams.size() + "   " + bundle.teams);

  /*          bundle.media = extractVideos(json);
            Log.d(LOG_TAG, "Media:  " + bundle.media.size() + "   " + bundle.media);
*/
            bundle.tps = extractTrainingPhases(json);
            Log.d(LOG_TAG, "TrainingPhase:  " + bundle.tps.size() + "   " + bundle.tps);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JsonAccessException("Failed receiving data from server", e, JsonAccessException.ACCESS_ERROR);
        }
        return bundle;
    }


    public String readEntireCoachServer(String clubUri) throws JsonAccessException {
        try {
            Log.d(LOG_TAG, "readEntireCoachServer(...):  token:" + LocalStorage.getInstance().getLatestUserToken());
            return httpAccess.readEntireCoachServer(LocalStorage.getInstance().getLatestUserToken(), clubUri);
        } catch (HttpAccessException e) {
            e.printStackTrace();
            throw new JsonAccessException("Failed reading composite view", e, e.getMode());
        }
    }


    public String getToken(String user, String password)  throws JsonAccessException {
        try {
            // 'Content-Type: application/json'
            String header   = "application/json";
            String data     = "{ \"user\": \"" + user + "\", \"password\": \"" + password + "\" }";
            String jsonData = httpAccess.getToken(header, data);

            Log.d(LOG_TAG, "Token: " + jsonData);
            JSONObject json = new JSONObject(jsonData);
            String token    = json.getString(JsonSettings.TOKEN_TAG);
            return new JSONObject(token).getString(JsonSettings.TOKEN_TAG);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JsonAccessException("Failed getting token: ", e, JsonAccessException.ACCESS_ERROR);
        }
    }

    public static void printElement(JSONArray jArray) {
        for (int i = 0; i < jArray.length(); i++) {
            try {
                JSONObject jo = jArray.getJSONObject(i);
                String uuid = jo.getString(JsonSettings.UUID_TAG);
                String name = jo.getString(JsonSettings.NAME_TAG);
                System.out.println(name + " " + uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Member> extractMembers(JSONObject json) {
        List<Member> members = new ArrayList<Member>();
        try {
            JSONArray memberArray = json.getJSONArray(JsonSettings.MEMBERS_TAG);

            for (int i = 0; i < memberArray.length(); i++) {
                JSONObject jo = memberArray.getJSONObject(i);
                String uuid = jo.getString(JsonSettings.UUID_TAG);
                String name = jo.getString(JsonSettings.NAME_TAG);
                String clubUuid = jo.getString(JsonSettings.CLUB_TAG);
                String teamUuid = jo.getString(JsonSettings.TEAM_TAG);
                Member m = new Member(uuid, name, clubUuid, teamUuid);
                members.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return members;
    }

    public List<Team> extractTeams(JSONObject json) {
        List<Team> teams = new ArrayList<Team>();
        try {
            JSONArray memberArray = json.getJSONArray(JsonSettings.TEAMS_TAG);

            for (int i = 0; i < memberArray.length(); i++) {
                JSONObject jo = memberArray.getJSONObject(i);
                String uuid = jo.getString(JsonSettings.UUID_TAG);
                String name = jo.getString(JsonSettings.NAME_TAG);
                String clubUuid = jo.getString(JsonSettings.CLUB_TAG);
                Team m = new Team(uuid, name, clubUuid);
                teams.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return teams;
    }


    public List<TrainingPhase> extractTrainingPhases(JSONObject json) {
        List<TrainingPhase> tps = new ArrayList<TrainingPhase>();
        try {
            JSONArray tpArray = json.getJSONArray(JsonSettings.TRAINING_PHASES_TAG);

            for (int i = 0; i < tpArray.length(); i++) {
                JSONObject jo = tpArray.getJSONObject(i);
                String uuid = jo.getString(JsonSettings.UUID_TAG);
                String name = jo.getString(JsonSettings.NAME_TAG);
                String clubUuid = jo.getString(JsonSettings.CLUB_TAG);
                String videoUuid="";
                if (!jo.isNull(JsonSettings.VIDEO_TAG)) {
                    videoUuid = jo.getString(JsonSettings.VIDEO_TAG);
                }
                TrainingPhase m = new TrainingPhase(uuid, name, clubUuid, videoUuid);
                tps.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tps;
    }


    private int jsonStatusToMediaStatus(String status) {
        Log.d(LOG_TAG, "jsonStatusToMediaStatus ( " + status + ")");
        switch (status) {
            case JsonSettings.SERVER_VIDEO_EMPTY_TAG:
                return Media.MEDIA_STATUS_UNDEFINED;
            case JsonSettings.SERVER_VIDEO_PROCESSING_TAG:
                return Media.MEDIA_STATUS_UPLOADED;
            case JsonSettings.SERVER_VIDEO_COMPLETE_TAG:
                return Media.MEDIA_STATUS_AVAILABLE;
            case JsonSettings.SERVER_VIDEO_FAILURE_TAG:
                return Media.MEDIA_STATUS_UPLOAD_FAILED;
        }
        return Media.MEDIA_STATUS_UPLOAD_FAILED;
    }

/*    public List<Media> extractVideos(JSONObject json) {
        List<Media> media = new ArrayList<Media>();

        try {
            JSONArray videoArray = json.getJSONArray(JsonSettings.VIDEOS_TAG);

            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject jo = videoArray.getJSONObject(i);
                String status = jo.getString(JsonSettings.STATUS_TAG);
                String uuid = jo.getString(JsonSettings.UUID_TAG);
                String tp = jo.getString(JsonSettings.TRAININGPHASE_TAG);
                String club = jo.getString(JsonSettings.CLUB_TAG);
                String member = null;
                try {
                    member = jo.getString(JsonSettings.MEMBER_TAG);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "JsonException while parsing member in video. Assuming instructional");
                }
                String team = null;
                String date = null;

                // HESA HESA HESA - THERE BE DRAGONS HERE
                try {
                    date = jo.getString(JsonSettings.CREATED_TAG);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "JsonException while parsing date in video. CONTINUING SINCE WE*re testing");
                }
                // HESA HESA HESA - THERE BE DRAGONS HERE
                try {
                    team = jo.getString(JsonSettings.TEAM_TAG);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "JsonException while parsing team in video. CONTINUING SINCE WE*re testing");
                }


                Log.d(LOG_TAG, "Creating media from: " + uuid + " " + club + " " + status + " " + jsonStatusToMediaStatus(status) + " " + team + " " + tp + " " + member);
                Log.d(LOG_TAG, "  media: " + media.size());

                Log.d(LOG_TAG, "  date:  '" + date + "'");

//                "2016-02-25T23:16:24.74447Z"
                date = date.replaceFirst("(\\d\\d[\\.,]\\d{3})\\d+", "$1");

                Date d = new Date(date);
                Log.d(LOG_TAG, "  date:  " + d);
                Log.d(LOG_TAG, "  date:  " + d.getTime());

                Media m = new Media(uuid, "", club,
                        null, jsonStatusToMediaStatus(status),
                        d.getTime(),
                        team, tp, member);
                media.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return media;
    }
*/

    public void downloadVideo(String clubUri, String file, String videoUuid) throws JsonAccessException {
        try {
            httpAccess.downloadVideo(clubUri, file, videoUuid);
        } catch (HttpAccessException e) {
            e.printStackTrace();
            throw new JsonAccessException("Failed downloading video: " + videoUuid, e.getMode());
        }
    }

    public void uploadTrainingPhaseVideo(String clubUri, Media m) throws JsonAccessException {

        Log.d(LOG_TAG, "   upload file: " + m.fileName());
        Log.d(LOG_TAG, "      uuid    : " + m.getUuid());
        try {
            httpAccess.uploadTrainingPhaseVideo(clubUri, m.getUuid(), m.fileName());
        } catch (IOException e) {
            Log.d(LOG_TAG, "Missing file ... deleting Media from db");
            ReportUser.Log(
                    CoachAppSession.getInstance().getCurrentActivity().getString(R.string.missing_file),
                    "Missing file: " + m.fileName());
            Storage.getInstance().removeMediaFromDb(m);
        } catch (HttpAccessException e) {

            if (e.getMode()==HttpAccessException.CONFLICT_ERROR) {
                Log.d(LOG_TAG, "Conflict uploading file ... deleting Media from db");
                ReportUser.Log(
                        CoachAppSession.getInstance().getCurrentActivity().getString(R.string.conflicting_file),
                        "file conflict, most likely already uploaded: " + m.fileName() + ". You can discard this");
                Storage.getInstance().removeMediaFromDb(m);
            } else {
                e.printStackTrace();
                Log.d(LOG_TAG, "  exception: " + e.getMessage());
                throw new JsonAccessException("Failed to access http", e, e.getMode());
            }
        }
    }




    public String createVideoOnServer(String clubUri, Media m) throws JsonAccessException {
        Log.d(LOG_TAG, "createVideoOnServer()");
        String trainingPhaseUuid = m.getTrainingPhase();
        if (trainingPhaseUuid.length() < 3) {
            throw new JsonAccessException("No TrainingPhase id", JsonAccessException.ACCESS_ERROR);
        }

        Log.d(LOG_TAG, "uploading ... pt II");

        String dateString = CADateFormat.getDateStringForServer(m.getDate());
        String jsonData = "{ \"" + JsonSettings.TRAININGPHASE_TAG + "\": \"" + trainingPhaseUuid + "\" , " +
                "\"" + JsonSettings.RECORDED_DATE_TAG + "\": \"" + dateString + "\"" ;
        if ( m.getMember()!=null ) {
            if (!m.getMember().equals("")) {
                jsonData = jsonData + " , \"" + JsonSettings.MEMBER_TAG + "\": \"" + m.getMember() + "\"";
            }
        }
        jsonData = jsonData + "}";

        Log.d(LOG_TAG, "DEBUG upload: " + jsonData);


        String header = "application/json";

        try {
            String jsonString = httpAccess.createVideo(LocalStorage.getInstance().getLatestUserToken(), clubUri, jsonData, header);
            Log.d(LOG_TAG, jsonString);
            JSONObject jo = new JSONObject(jsonString);
            String uuid = jo.getString("uuid");
            String state = jo.getString("status");
            Log.d(LOG_TAG, "created (id):    " + uuid);
            Log.d(LOG_TAG, "created (state): " + state);
//            if (state.equals("empty")) {
            return uuid;
        } catch (JSONException e) {
            throw new JsonAccessException("Failed requesting new uuid", e, JsonAccessException.ACCESS_ERROR);
        } catch (HttpAccessException e) {
            throw new JsonAccessException("Failed requesting new uuid", e.getMode());
        }
    }



}