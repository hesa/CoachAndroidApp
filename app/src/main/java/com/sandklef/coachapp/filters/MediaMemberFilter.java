package com.sandklef.coachapp.filters;


import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Media;

public class MediaMemberFilter implements MediaFilter {

    private final static String LOG_TAG = MediaMemberFilter.class.getSimpleName();

    private String uuid;

    public MediaMemberFilter() {
        uuid=null;
    }

    static public MediaMemberFilter newMediaMemberFilter(String uuid) {
        MediaMemberFilter bf = new MediaMemberFilter();
        bf.uuid=uuid;
        return bf;
    }


    @Override
    public boolean check(Media m) {
        Log.d(LOG_TAG, "check media:   member: " + m.getMember());
        if (m.getMember()==null && uuid==null) {
            return true;
        } else if (uuid==null) {
            return false;
        }
        return (uuid.equals(m.getMember()));
    }

}
