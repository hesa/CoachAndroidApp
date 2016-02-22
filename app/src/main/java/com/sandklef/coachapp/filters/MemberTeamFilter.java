package com.sandklef.coachapp.filters;


import com.sandklef.coachapp.model.Member;

public class MemberTeamFilter implements MemberFilter {

    private final static String LOG_TAG = MemberTeamFilter.class.getSimpleName();

    private String teamUuid;

    public MemberTeamFilter() {
        teamUuid=null;
    }

    static public MemberTeamFilter newMemberTeamFilter(String uuid) {
        MemberTeamFilter bf = new MemberTeamFilter();
        bf.teamUuid=uuid;
        return bf;
    }


    @Override
    public boolean check(Member m) {
        return (teamUuid.equals(m.getTeamUuid()));
    }

}
