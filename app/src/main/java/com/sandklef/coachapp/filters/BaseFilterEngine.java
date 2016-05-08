package com.sandklef.coachapp.filters;

import com.sandklef.coachapp.model.CoachAppBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesa on 2016-02-21.
 */
public class BaseFilterEngine {

    public static List<CoachAppBase> apply(List<CoachAppBase> list, BaseFilter bf) {
        List filteredList = new ArrayList<CoachAppBase>();
        for (CoachAppBase b : list) {
            if (bf.check(b)) {
                filteredList.add(b);
            }

        }
        return filteredList;
    }
}
