package com.sandklef.coachapp.filters;


import com.sandklef.coachapp.model.CoachAppBase;

public class NameBaseFilter implements BaseFilter {

    private final static String LOG_TAG = NameBaseFilter.class.getSimpleName();
    private String name;
    private NameBaseFilter() {
        name=null;
    }

    static public NameBaseFilter newBaseFilterName(String needle) {
        NameBaseFilter bf = new NameBaseFilter();
        bf.name=needle;
        return bf;
    }

/*    public List<Base> apply(List<Base> list) {
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
*/

    @Override
    public boolean check(CoachAppBase b) {
        return b.toString().contains(this.name);
    }
}
