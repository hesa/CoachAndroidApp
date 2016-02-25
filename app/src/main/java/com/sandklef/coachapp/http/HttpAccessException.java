package com.sandklef.coachapp.http;

/**
 * Created by hesa on 2016-02-25.
 */
public class HttpAccessException extends Exception {

    public HttpAccessException(String msg, Exception e) {
        super(msg, e);
    }

    public HttpAccessException(String msg) {
        super(msg);
    }

}
