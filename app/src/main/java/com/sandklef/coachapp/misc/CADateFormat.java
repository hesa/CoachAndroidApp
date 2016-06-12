package com.sandklef.coachapp.misc;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hesa on 2016-02-21.
 */
public class CADateFormat {

    public static SimpleDateFormat serverDf;
    public static SimpleDateFormat sdf;
    public static SimpleDateFormat sdfDay;
    public static SimpleDateFormat sdfTime;

    static {
        sdf      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdfDay   = new SimpleDateFormat("yyyy-MM-dd");
        sdfTime  = new SimpleDateFormat("hh:mm:ss");
    }

//    public static String DATE_FORMAT = "yyyyMMdd-HHmmss";

    public static Date getDate(long time) {
        return new Date(time);
    }

    public static String getDateStringForServer(long time) {
//        Goal: 2016-06-11T23:29:12.935412Z
//
        Date d     = new Date(time);
        String ds   = getDayString(d);
        String ts   = getTimeString(d);
        return ds + "T" + ts + ".000000Z";
    }

    public static String getDateString(Date d) {
//        java.text.DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(d);
    }

    public static String getDayString(Date d) {
        return sdfDay.format(d);
    }

    public static String getTimeString(Date d) {
        return sdfTime.format(d);
    }

    public static String getDateString(long time) {
//        java.text.DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return getDateString(getDate(time));
    }


}
