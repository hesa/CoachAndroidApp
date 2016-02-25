package com.sandklef.coachapp.storage;

/**
 * Created by hesa on 2016-02-25.
 */
public class StorageException  extends Exception{

    public StorageException(String msg) {
        super(msg);
    }
    public StorageException(String msg, Exception e) {
        super(msg, e);
    }

}
