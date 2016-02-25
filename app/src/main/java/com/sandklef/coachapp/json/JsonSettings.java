package com.sandklef.coachapp.json;

/**
 * Created by hesa on 2016-02-25.
 */
public class JsonSettings {

    // SERVER TYPE TAGS
    public static final String TRAINING_PHASES_TAG = "trainingPhases";
    public static final String VIDEOS_TAG = "videos";
    public static final String TEAMS_TAG = "teams";
    public static final String MEMBERS_TAG = "members";

    // SERVER JSON TAGS
    public static final String UUID_TAG = "uuid";
    public static final String NAME_TAG = "name";
    public static final String CLUB_TAG = "clubUuid";
    public static final String TEAM_TAG = "teamUuid";
    public static final String TRAININGPHASE_TAG = "trainingPhaseUuid";
    public static final String MEMBER_TAG = "memberUuid";
    public static final String STATUS_TAG = "status";
    public static final String CREATED_TAG = "created";

    // SERVER VIDEO TAGS
    public static final String SERVER_VIDEO_EMPTY_TAG = "empty";
    public static final String SERVER_VIDEO_PROCESSING_TAG = "processing";
    public static final String SERVER_VIDEO_COMPLETE_TAG = "complete";
    public static final String SERVER_VIDEO_FAILURE_TAG = "failure";

    // TODO: Move this somewhere else
    public static final String SERVER_VIDEO_SUFFIX = ".webm";

}
