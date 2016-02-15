package com.sandklef.coachapp.filters;


import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Base;
import com.sandklef.coachapp.model.Media;

import java.util.ArrayList;
import java.util.List;

public class MediaFilter {

    private final static String LOG_TAG = MediaFilter.class.getSimpleName();

    private String name;
    private int    status;

    public MediaFilter() {
        name=null;
        status=-1;
    }

    static public MediaFilter newMediaFilterName(String needle) {
        MediaFilter bf = new MediaFilter();
        bf.name=needle;
        return bf;
    }

    static public MediaFilter newMediaFilterStatus(int status) {
        Log.d(LOG_TAG, " newMediaFilterStatus (" + status + ")");
        MediaFilter bf = new MediaFilter();
        bf.status=status;
        return bf;
    }

    public List<Media> apply(List<Media> list) {
        List filteredList = new ArrayList<Media>();
        for (Media b: list){

            // Treat unset as include

            if ( (name==null || b.toString().contains(name)) &&
                    ( (status==-1) || (b.getStatus()==status) )){
                Log.d(LOG_TAG, " adding to filtered Media list: " + b);
                Log.d(LOG_TAG, "    name:     " + b.toString()+ " " + name);
                Log.d(LOG_TAG, "    status:   " + b.getStatus()+ " " + status);
                filteredList.add(b);
            } else {
                Log.d(LOG_TAG, " NOT adding to filtered Media list: " + b);
            }
        }
        return filteredList;
    }
}
