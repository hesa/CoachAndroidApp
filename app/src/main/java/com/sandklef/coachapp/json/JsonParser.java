package com.sandklef.coachapp.json;

import com.sandklef.coachapp.model.*;
import com.sandklef.coachapp.misc.*;
import com.sandklef.coachapp.storage.*;
import com.sandklef.coachapp.http.HttpAccess;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.os.AsyncTask;


public class JsonParser extends AsyncTask<Void, Void, JsonParser.AsyncBundle> {

    // SERVER TYPE TAGS
    private static final String TRAINING_PHASES_TAG = "trainingPhases";
    private static final String VIDEOS_TAG = "videos";
    private static final String TEAMS_TAG = "teams";
    private static final String MEMBERS_TAG = "members";

    // SERVER JSON TAGS
    private static final String UUID_TAG = "uuid";
    private static final String NAME_TAG = "name";
    private static final String CLUB_TAG = "clubUuid";
    private static final String TEAM_TAG = "teamUuid";
    private static final String TRAININGPHASE_TAG = "trainingPhaseUuid";
    private static final String MEMBER_TAG = "memberUuid";
    private static final String STATUS_TAG = "status";
    private static final String CREATED_TAG = "created";

    // SERVER VIDEO TAGS
    private static final String SERVER_VIDEO_EMPTY_TAG = "empty";
    private static final String SERVER_VIDEO_PROCESSING_TAG = "processing";
    private static final String SERVER_VIDEO_COMPLETE_TAG = "complete";
    private static final String SERVER_VIDEO_FAILURE_TAG = "failure";

    private final static String LOG_TAG = JsonParser.class.getSimpleName();


    private String clubName;
    private Context context;
    private HttpAccess httpAccess;

    private String jsonString;

    public JsonParser(String name, Context context) {
        this.clubName = name;
        this.context = context;
        this.httpAccess =
                new HttpAccess(LocalStorage.getInstance().getServerUrl(), name);
    }

    public AsyncBundle update() {
        AsyncBundle bundle = null;
        if (clubName != null) {

            List<Member> members;
            List<Team> teams;
            List<Media> media;
            List<TrainingPhase> tps;
            try {
                /*
                 *
                 *  Get data from server
                 *
                 */
                jsonString = readEntireCoachServer();
                JSONObject json = new JSONObject(jsonString);

                Log.d(LOG_TAG, " json: " + jsonString);
                /*
                 *
                 *  Extract the data
                 *
                 */

                members = extractMembers(json);
                Log.d(LOG_TAG, "Members:  " + members.size() + "   " + members);


                teams = extractTeams(json);
                Log.d(LOG_TAG, "Teams:  " + teams.size() + "   " + teams);

                media = extractVideos(json);
                Log.d(LOG_TAG, "Media:  " + media.size() + "   " + media);

                tps = extractTrainingPhases(json);
                Log.d(LOG_TAG, "TrainingPhase:  " + tps.size() + "   " + tps);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            bundle = new AsyncBundle(members, teams, media, tps);
        }
        return bundle;
    }


    public String readEntireCoachServer() {
        return httpAccess.readEntireCoachServer();
    }

    public static void printElement(JSONArray jArray) {
        for (int i = 0; i < jArray.length(); i++) {
            try {
                JSONObject jo = jArray.getJSONObject(i);
                String uuid = jo.getString(UUID_TAG);
                String name = jo.getString(NAME_TAG);
                System.out.println(name + " " + uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Member> extractMembers(JSONObject json) {
        List<Member> members = new ArrayList<Member>();
        try {
            JSONArray memberArray = json.getJSONArray(JsonParser.MEMBERS_TAG);

            for (int i = 0; i < memberArray.length(); i++) {
                JSONObject jo = memberArray.getJSONObject(i);
                String uuid = jo.getString(UUID_TAG);
                String name = jo.getString(NAME_TAG);
                String clubUuid = jo.getString(CLUB_TAG);
                String teamUuid = jo.getString(TEAM_TAG);
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
            JSONArray memberArray = json.getJSONArray(JsonParser.TEAMS_TAG);

            for (int i = 0; i < memberArray.length(); i++) {
                JSONObject jo = memberArray.getJSONObject(i);
                String uuid = jo.getString(UUID_TAG);
                String name = jo.getString(NAME_TAG);
                String clubUuid = jo.getString(CLUB_TAG);
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
            JSONArray tpArray = json.getJSONArray(JsonParser.TRAINING_PHASES_TAG);

            for (int i = 0; i < tpArray.length(); i++) {
                JSONObject jo = tpArray.getJSONObject(i);
                String uuid = jo.getString(UUID_TAG);
                String name = jo.getString(NAME_TAG);
                String clubUuid = jo.getString(CLUB_TAG);
                TrainingPhase m = new TrainingPhase(uuid, name, clubUuid);
                tps.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tps;
    }


    private int jsonStatusToMediaStatus(String status) {
        switch (status) {
            case SERVER_VIDEO_EMPTY_TAG:
                return Media.MEDIA_STATUS_UNDEFINED;
            case SERVER_VIDEO_PROCESSING_TAG:
                return Media.MEDIA_STATUS_UPLOADED;
            case SERVER_VIDEO_COMPLETE_TAG:
                return Media.MEDIA_STATUS_AVAILABLE;
            case SERVER_VIDEO_FAILURE_TAG:
                return Media.MEDIA_STATUS_UPLOAD_FAILED;
        }
        return Media.MEDIA_STATUS_UPLOAD_FAILED;
    }

    public List<Media> extractVideos(JSONObject json) {
        List<Media> media = new ArrayList<Media>();

        try {
            JSONArray videoArray = json.getJSONArray(JsonParser.VIDEOS_TAG);

            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject jo = videoArray.getJSONObject(i);
                String status = jo.getString(STATUS_TAG);
                String uuid = jo.getString(UUID_TAG);
                String tp = jo.getString(TRAININGPHASE_TAG);
                String club = jo.getString(CLUB_TAG);
                String member = null;
                try {
                    member = jo.getString(MEMBER_TAG);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "JsonException while prasing member in video. Assuming instructional");
                }
                String team = null;
                String date = null;

                // HESA HESA HESA - THERE BE DRAGONS HERE
                try {
                    team = jo.getString(TEAM_TAG);
                    date = jo.getString(CREATED_TAG);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "JsonException while prasing member in video. CONTINUING SINCE WE*re testing");
                }

                /*
                public Media(String uuid,
                 String name,
                 String clubUuid,
                 String file,
                 MediaStatus status,
                 long   date,
                 String teamUuid,
                 String trainingPhaseUuid,
                 String memberUuid) {
                 */
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

/*
    public String createTrainingPhaseVideo(String tpName) {
        BufferedReader rd;
        JSONObject holder = new JSONObject();
        //        StringBuilder  builder = new StringBuilder();
        try {
            holder.put("trainingPhaseUuid", tpName);
            StringEntity se = new StringEntity(holder.toString());

            String jsonString = httpAccess.sendHttpPost(se,
                    "application/json",
                    "/videos");
            System.out.println("SE: " +
                    holder.toString());
            System.out.println("RESPONSE: " +
                    jsonString);
            JSONObject json = new JSONObject(jsonString);
            String uuid = json.getString(UUID_TAG);
            return uuid;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
*/


    public boolean downloadVideo(String file, String videoUuid) {
        return httpAccess.downloadVideo(file, videoUuid);
    }


    @Override
    protected AsyncBundle doInBackground(Void... voids) {
        Log.d(LOG_TAG, " fetching JSON data in background");
        return update();
    }

    protected void onPostExecute(AsyncBundle bundle) {
        Log.d(LOG_TAG, " storing new data (from JSON) in background: " + bundle);
            /*
             *
             *  Store in DB
             *
             */
        if (bundle != null) {
            Storage.getInstance().updateDB(bundle.getMembers(),
                    bundle.getTeams(),
                    bundle.getMedia(),
                    bundle.getTrainingPhases());
        }
    }

    protected static class AsyncBundle {
        private List<Member> members;
        private List<Team> teams;
        private List<Media> media;
        private List<TrainingPhase> tps;

        public AsyncBundle(List<Member> members,
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


}