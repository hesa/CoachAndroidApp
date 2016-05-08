package com.sandklef.coachapp.model;

import com.sandklef.coachapp.misc.CADateFormat;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.storage.LocalStorage;

import java.security.acl.LastOwnerException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hesa on 2016-02-26.
 */
public class LogMessage {

    private String message;
    private String clubUuid;
    private Date   date;
    private long   id;

    private final static String LOG_TAG = LogMessage.class.getSimpleName();


    //        String[] projectionArray = {LOG_ID, CLUB_COLUMN_NAME, LOG_MSG, LOG_DATE};

    public LogMessage(long id,
                      String clubUuid,
                      String msg,
                      Date   date) {
        this.id       = id;
        this.clubUuid = clubUuid;
        this.message  = msg;
        this.date     = date;
    }

    public String getMesssage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public String toString() {
        // Log.d(LOG_TAG, "LOG: converting date: " + date);
        return "[" + CADateFormat.getDateString(date) + "] " + message;
    }
}
