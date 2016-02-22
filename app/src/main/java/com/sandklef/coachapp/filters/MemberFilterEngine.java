package com.sandklef.coachapp.filters;

import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesa on 2016-02-21.
 */
public class MemberFilterEngine {

    public static List<Member> apply(List<Member> list, MemberFilter bf) {
        List filteredList = new ArrayList<Member>();
        for (Member b : list) {
            if (bf.check(b)) {
                filteredList.add(b);
            }

        }
        return filteredList;
    }
}
