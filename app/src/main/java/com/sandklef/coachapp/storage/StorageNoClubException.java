package com.sandklef.coachapp.storage;

/**
 * Created by hesa on 2016-05-07.
 */
public class StorageNoClubException extends StorageException {

    public StorageNoClubException(String msg) {
        super(msg);
    }
    public StorageNoClubException(String msg, Exception e) {
        super(msg, e);
    }


}
