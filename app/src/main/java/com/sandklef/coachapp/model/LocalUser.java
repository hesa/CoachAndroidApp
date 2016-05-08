package com.sandklef.coachapp.model;

import java.util.List;

/**
 * Created by hesa on 2016-05-07.
 */
public class LocalUser  {

    private int          id;
    private String       name;
    private String       email;
    private String       password; // currently not used
    private List<String> clubUuids;
    private String       latestClubUuid;
    private String       token;

    public LocalUser(int          id,
                     String       name,
                     String       email,
                     String       password,
                     List<String> clubUuids,
                     String       latestClubUuid,
                     String       token) {
        this.name           = name;
        //    super(uuid,name, "");
        this.email          = email;
        this.password       = password;
        this.clubUuids      = clubUuids;
        this.latestClubUuid = latestClubUuid;
        this.token          = token;
    }

    public LocalUser(String       name,
                     String       email,
                     String       password,
                     List<String> clubUuids,
                     String       latestClubUuid,
                     String       token) {
        this(-1, name, email, password, clubUuids, latestClubUuid, token);
    }


    public int    getId()               { return id;        }
    public String getName()             { return name;      }
    public String getEmail()            { return email;     }
    public String getPassword()         { return password;  }// currently not used
    public List<String> getClubUuids()  { return clubUuids; }
    public String getLatestClubUuid()   { return latestClubUuid;}
    public String getToken()            { return token;}

    public String toString() {
        return "[ " + id + " | " +
                name + " | " +
                email + " | " +
                password + " | " +
                clubUuids + " | " +
                latestClubUuid + " | " +
                token + " ]" ;
    }

}
