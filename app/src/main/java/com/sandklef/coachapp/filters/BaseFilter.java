package com.sandklef.coachapp.filters;


import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Base;

import java.util.ArrayList;
import java.util.List;

public class BaseFilter {

    private final static String LOG_TAG = BaseFilter.class.getSimpleName();

    private String name;

    public BaseFilter() {
        name=null;
    }

    static public BaseFilter  newBaseFilterName(String needle) {
        BaseFilter bf = new BaseFilter();
        bf.name=needle;
        return bf;
    }

    public List<Base> apply(List<Base> list) {
        List filteredList = new ArrayList<Base>();
        for (Base b: list){
            Log.d(LOG_TAG, " adding? " + name +  " compared with: " + b);
            if (b.toString().contains(name)) {
                Log.d(LOG_TAG, " adding to filtered base list: " + b);
                filteredList.add(b);
            } else {
                Log.d(LOG_TAG, " NOT adding to filtered base list: " + b);
            }
        }
        return filteredList;
    }
}
