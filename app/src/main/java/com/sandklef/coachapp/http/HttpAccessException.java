package com.sandklef.coachapp.http;

/**
 * Created by hesa on 2016-02-25.
 */
public class HttpAccessException extends Exception {

    public final static int NETWORK_ERROR   = 1;  // netowrk down, http problem
    public final static int ACCESS_ERROR    = 2;  // server responds "not ok"
    public final static int CONFLICT_ERROR  = 2;  // server responds "Conflict"

    private int mode  ;

    public HttpAccessException(String msg, Exception e, int mode) {
        super(msg, e); this.mode=mode;
    }

    public HttpAccessException(Exception e, int mode) {
        super(e); this.mode=mode;
    }

    public HttpAccessException(String msg, int mode) {
        super(msg);
        this.mode=mode;
    }

    public int getMode() {
        return mode;
    }

}
