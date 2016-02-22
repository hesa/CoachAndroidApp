package com.sandklef.coachapp.filters;

import com.sandklef.coachapp.model.Base;
import com.sandklef.coachapp.model.Media;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesa on 2016-02-21.
 */
public class BaseFilterEngine {

    public static List<Base> apply(List<Base> list, BaseFilter bf) {
        List filteredList = new ArrayList<Base>();
        for (Base b : list) {
            if (bf.check(b)) {
                filteredList.add(b);
            }

        }
        return filteredList;
    }
}
