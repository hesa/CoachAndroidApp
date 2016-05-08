package com.sandklef.coachapp.model;

import java.util.ArrayList;

public class Member extends CoachAppBase {

    private String teamUuid;

    public Member(String uuid,
                  String name,
                  String clubUuid,
                  String teamUuid) {
        super(uuid, name, clubUuid);
        this.teamUuid = teamUuid;
    }

    public String getTeamUuid() {
        return teamUuid;
    }



    
}
