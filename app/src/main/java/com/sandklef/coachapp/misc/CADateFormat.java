package com.sandklef.coachapp.misc;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hesa on 2016-02-21.
 */
public class CADateFormat {

    public static String DATE_FORMAT = "yyyyMMdd-HHmmss";

    public static Date getDate(long time) {
        return new Date(time);
    }

    public static String getDateString(long time) {
        java.text.DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(getDate(time));
    }

}
