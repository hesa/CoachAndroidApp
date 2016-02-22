package com.sandklef.coachapp.filters;

import com.sandklef.coachapp.model.Base;
import com.sandklef.coachapp.model.Media;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesa on 2016-02-21.
 */
public class MediaFilterEngine {

    public static List<Media> apply(List<Media> list, MediaFilter bf) {
        List filteredList = new ArrayList<Media>();
        for (Media b : list) {
            if (bf.check(b)) {
                filteredList.add(b);
            }

        }
        return filteredList;
    }
}
