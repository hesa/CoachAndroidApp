package com.sandklef.coachapp.json;

import com.sandklef.coachapp.http.HttpAccessException;
import com.sandklef.coachapp.model.*;
import com.sandklef.coachapp.misc.*;
import com.sandklef.coachapp.storage.*;
import com.sandklef.coachapp.http.HttpAccess;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;


public class JsonAccess  {


    private final static String LOG_TAG = JsonAccess.class.getSimpleName();



    private Context context;
    private HttpAccess httpAccess;

    private String jsonString;

    public JsonAccess(String name, Context context) throws JsonAccessException {
        if (name == null || context == null) {
            throw new JsonAccessException(
                    "NULL pointer passed to constructor (" + name + ", " + context + ")");
        }
        this.context = context;
        try {
            this.httpAccess =
                    new HttpAccess(LocalStorage.getInstance().getServerUrl(), name);
        } catch (HttpAccessException e) {
            throw new JsonAccessException("Could not access Http", e);
        }
    }

    public class CompositeBundle {
        public List<Member> members;
        public List<Team> teams;
        public List<Media> media;
        public List<TrainingPhase> tps;
    }


    public CompositeBundle update() {
        CompositeBundle bundle = new CompositeBundle();
            try {
                /*
                 *
                 *  Get data from server
                 *
                 */
                Log.d(LOG_TAG, "Read entire coach server");
                jsonString = readEntireCoachServer();
                JSONObject json = new JSONObject(jsonString);

                Log.d(LOG_TAG, " json: " + jsonString);
                /*
                 *
                 *  Extract the data
                 *
                 */

                bundle.members = extractMembers(json);
                Log.d(LOG_TAG, "Members:  " + bundle.members.size() + "   " + bundle.members);

                bundle.teams = extractTeams(json);
                Log.d(LOG_TAG, "Teams:  " + bundle.teams.size() + "   " + bundle.teams);

                bundle.media = extractVideos(json);
                Log.d(LOG_TAG, "Media:  " + bundle.media.size() + "   " + bundle.media);

                bundle.tps = extractTrainingPhases(json);
                Log.d(LOG_TAG, "TrainingPhase:  " + bundle.tps.size() + "   " + bundle.tps);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        return bundle;
    }


    public String readEntireCoachServer() throws JsonAccessException {
        try {
            return httpAccess.readEntireCoachServer();
        } catch (HttpAccessException e) {
            throw new JsonAccessException("Failed reading composite view", e);
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
                TrainingPhase m = new TrainingPhase(uuid, name, clubUuid);
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

    public List<Media> extractVideos(JSONObject json) {
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
                Log.d(LOG_TAG, "  date:  " + new Date(date).getTime());

                Media m = new Media(uuid, "", club,
                        null, jsonStatusToMediaStatus(status),
                        new Date(date).getTime(),
                        team, tp, member);
                media.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return media;
    }


    public void downloadVideo(String file, String videoUuid) throws JsonAccessException {
        try {
            httpAccess.downloadVideo(file, videoUuid);
        } catch (HttpAccessException e) {
            throw new JsonAccessException("Failed downloading video: " + videoUuid, e);
        }
    }

    public void uploadTrainingPhaseVideo(Media m) throws JsonAccessException {

        Log.d(LOG_TAG, "   upload file: " + m.fileName());
        Log.d(LOG_TAG, "      uuid    : " + m.getUuid());
        try {
            httpAccess.uploadTrainingPhaseVideo(m.getUuid(), m.fileName());
        } catch (HttpAccessException e) {
            throw new JsonAccessException("Failed to access http", e);
        }
    }

    public String createVideoOnServer(Media m) throws JsonAccessException {
        Log.d(LOG_TAG, "createVideoOnServer()");
        String trainingPhaseUuid = m.getTrainingPhase();
        if (trainingPhaseUuid.length() < 3) {
            throw new JsonAccessException("No TrainingPhase id");
        }
        String jsonData = "{ \"trainingPhaseUuid\": \"" + trainingPhaseUuid + "\" }";
        String header = "application/json";

        try {
            String jsonString = httpAccess.createVideo(jsonData, header);
            Log.d(LOG_TAG, jsonString);
            JSONObject jo = new JSONObject(jsonString);
            String uuid = jo.getString("uuid");
            String state = jo.getString("status");
            Log.d(LOG_TAG, "created (id):    " + uuid);
            Log.d(LOG_TAG, "created (state): " + state);
//            if (state.equals("empty")) {
            return uuid;
        } catch (JSONException e) {
            throw new JsonAccessException("Failed requesting new uuid", e);
        } catch (HttpAccessException e) {
            throw new JsonAccessException("Failed requesting new uuid", e);
        }
    }



}