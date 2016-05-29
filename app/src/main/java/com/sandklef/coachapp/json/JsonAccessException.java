package com.sandklef.coachapp.json;

/**
 * Created by hesa on 2016-02-16.
 */
public class JsonAccessException extends Exception {

    public final static int OK            = 0;  // no problemos
    public final static int NETWORK_ERROR = 1;  // netowrk down, http problem
    public final static int ACCESS_ERROR  = 2;  // server responds "not ok"

    private int mode  ;

    public JsonAccessException(String msg, int mode) {
        super(msg);
        this.mode=mode;
    }

    public JsonAccessException(String msg, Exception e, int mode) {
        super(msg, e);
        this.mode=mode;
    }

    public JsonAccessException(Exception e, int mode) {
        super(e);
        this.mode=mode;
    }

    public int getMode() {
        return mode;
    }

}
