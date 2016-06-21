package com.sandklef.coachapp.misc;

import android.app.Activity;
import android.widget.TextView;

import coachassistant.sandklef.com.coachapp.R;

/**
 * Created by hesa on 2016-03-08.
 */
public class ViewSetter {
    private final static String LOG_TAG = ViewSetter.class.getSimpleName();

    public static void setViewText(Activity activity, int id, String text) {
        Log.d(LOG_TAG, "Setting text \"" + text + "\" on " + id);
        TextView tv = (TextView) activity.findViewById(id);
        if (tv != null) {
            tv.setText(text);
        }
    }
}
