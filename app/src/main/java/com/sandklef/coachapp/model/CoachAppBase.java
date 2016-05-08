package com.sandklef.coachapp.model;

import java.util.ArrayList;
import com.sandklef.coachapp.storage.*;

public class CoachAppBase {

    private String uuid;
    private String name;
    private String clubUuid;


    public CoachAppBase(String uuid,
                        String name,
                        String clubUuid) {
        this.uuid     = uuid;
        this.clubUuid = clubUuid;
        this.name     = name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getClubUuid() {
        return clubUuid;
    }

    public String toString() {
        return name;
    }

    public String toString2() {
        return "<" + uuid + ">, " + name + " [ " + clubUuid + "]";
    }

}
