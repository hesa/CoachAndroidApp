package com.sandklef.coachapp.json;

/**
 * Created by hesa on 2016-02-16.
 */
public class JsonAccessException extends Exception {

    public JsonAccessException(String msg) {
        super(msg);
    }
    public JsonAccessException(String msg, Exception e) {
        super(msg, e);
    }

}
