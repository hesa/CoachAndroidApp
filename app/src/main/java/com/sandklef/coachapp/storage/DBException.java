package com.sandklef.coachapp.storage;


public class DBException extends RuntimeException {

    public DBException(String s){
        super(s);
    }
    
    public DBException(){
        super("Could not get hold of db");
    }
    
}
