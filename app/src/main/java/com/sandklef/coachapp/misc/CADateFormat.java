package com.sandklef.coachapp.misc;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hesa on 2016-02-21.
 */
public class CADateFormat {

    public static SimpleDateFormat sdf;
    static {
        sdf = new SimpleDateFormat("yyyy-mm-dd hh:MM:ss");
    }

//    public static String DATE_FORMAT = "yyyyMMdd-HHmmss";

    public static Date getDate(long time) {
        return new Date(time);
    }

    public static String getDateString(Date d) {
//        java.text.DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(d);
    }

    public static String getDateString(long time) {
//        java.text.DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return getDateString(getDate(time));
    }


}
