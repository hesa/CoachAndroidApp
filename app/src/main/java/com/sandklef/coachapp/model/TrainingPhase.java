package com.sandklef.coachapp.model;

import java.util.ArrayList;

public class TrainingPhase extends CoachAppBase {

    private String videoUuid;

    public TrainingPhase(String uuid,
                         String name,
                         String clubUuid,
                         String videoUuid) {
        super(uuid, name, clubUuid);
        this.videoUuid = videoUuid;
    }

    public String getVideoUuid() {
        return videoUuid;
    }

}
