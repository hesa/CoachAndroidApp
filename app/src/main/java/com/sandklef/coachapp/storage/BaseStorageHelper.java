package com.sandklef.coachapp.storage;

import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.*;
import com.sandklef.coachapp.report.ReportUser;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class BaseStorageHelper extends SQLiteOpenHelper {

    private final static String LOG_TAG = BaseStorageHelper.class.getSimpleName();

    // Tables
    private static final String TEAM_TABLE               = "teams";
    private static final String MEMBER_TABLE             = "members";
    private static final String TRAININGPHASE_TABLE      = "trainingphases";
    private static final String MEDIA_TABLE              = "media";

    // Base
    public static final String UUID_COLUMN_NAME          = "uuid";
    public static final String NAME_COLUMN_NAME          = "name";
    public static final String CLUB_COLUMN_NAME          = "club_uuid";

    // extending Base
    public static final String TEAM_COLUMN_NAME          = "team_uuid";
    public static final String MEMBER_COLUMN_NAME        = "member_uuid";
    public static final String TRAININGPHASE_COLUMN_NAME = "trainingphase_uuid";
    public static final String URI_COLUMN_NAME           = "uri";
    public static final String STATUS_COLUMN_NAME        = "status";
    public static final String DATE_COLUMN_NAME          = "date";


    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "coachassistant.db";
    public static final String SORT_ORDER    = UUID_COLUMN_NAME + " DESC";


    private Context context;
    private String currentClubUuid;

    private boolean        isCreating = false;
    private SQLiteDatabase currentDB  = null;

//    private  db;


    private static final String BASE_COLUMNS =
            BaseStorageHelper.UUID_COLUMN_NAME + " text primary key," +
                    BaseStorageHelper.CLUB_COLUMN_NAME + " text ," +
                    BaseStorageHelper.NAME_COLUMN_NAME + " text ";

    private String buildCreateString(String tableName, String col) {
        if (col == null) {
            col = "";
        }
        return "CREATE TABLE " + tableName + "(" + BASE_COLUMNS + col + ");";
    }

    private String buildDeleteString(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    public BaseStorageHelper(String club, Context context) {
        super(context, BaseStorageHelper.DATABASE_NAME,
                null, BaseStorageHelper.DATABASE_VERSION);
        // TODO: Verify existance of club
        this.currentClubUuid = club;
        this.context         = context;
    }

    public static ContentValues buildContentValues(Base b) {
        ContentValues values = new ContentValues();
        values.put(BaseStorageHelper.UUID_COLUMN_NAME, b.getUuid());
        values.put(BaseStorageHelper.NAME_COLUMN_NAME, b.getName());
        values.put(BaseStorageHelper.CLUB_COLUMN_NAME, b.getClubUuid());
        return values;
    }

    private void logExecSQL(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        Log.d(LOG_TAG, "SQL (" + db + ") stmt: " + sql);

        db.execSQL(sql);
    }

    public void onCreate(SQLiteDatabase db) {
        isCreating = true;
        currentDB = db;
        logExecSQL(buildCreateString(TEAM_TABLE, null));
        logExecSQL(buildCreateString(MEMBER_TABLE, ", " + TEAM_COLUMN_NAME + " text"));
        logExecSQL(buildCreateString(MEDIA_TABLE, ", "
                + TEAM_COLUMN_NAME + " text, "
                + URI_COLUMN_NAME + " text, "
                + STATUS_COLUMN_NAME + "  int,"
                + TRAININGPHASE_COLUMN_NAME + " text, "
                + MEMBER_COLUMN_NAME + " text, "
                + DATE_COLUMN_NAME + " int"));
        logExecSQL(buildCreateString(TRAININGPHASE_TABLE, null));
        isCreating = false;
        currentDB = null;
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(buildDeleteString(MEMBER_TABLE));
        db.execSQL(buildDeleteString(TEAM_TABLE));
        db.execSQL(buildDeleteString(TRAININGPHASE_TABLE));
        db.execSQL(buildDeleteString(MEDIA_TABLE));
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public Context getContext() {
        return context;
    }

    public ArrayList<String> buildProjection(String[] projections) {
        ArrayList<String> projs = new ArrayList<String>();
        projs.add(UUID_COLUMN_NAME);
        projs.add(NAME_COLUMN_NAME);
        projs.add(CLUB_COLUMN_NAME);

        if (projections != null) {
            for (int i = 0; i < projections.length; i++) {
                projs.add(projections[i]);
            }
        }
        return projs;
    }

    public String[] buildProjectionArray(String[] projections) {
        ArrayList<String> projs = buildProjection(projections);
        Object[] projsOArray = projs.toArray();
        String[] projsArray  = Arrays.copyOf(projsOArray,
                projsOArray.length, String[].class);
        return projsArray;
    }


    public Cursor getCursorFirst(String tbl, String[] projections) {
        //  SQLiteDatabase db = getWritableDatabase();

        Log.d(LOG_TAG, "  projection array: " + buildProjectionArray(projections));
        Log.d(LOG_TAG, "  Printing projection array: " + buildProjectionArray(projections).length);
        if (projections != null) {
            for (String a : buildProjectionArray(projections)) {
                Log.d(LOG_TAG, "  * " + a);
            }
        }

        String whereClause =  CLUB_COLUMN_NAME + " = ? ";

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(tbl,
                buildProjectionArray(projections),
                whereClause, new String[]{currentClubUuid}, null, null,
                BaseStorageHelper.SORT_ORDER);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        return cursor;
    }

    public void closeCursor(Cursor cursor) {
        cursor.close();
    }


    public void storeBaseToDB(SQLiteDatabase db, List<Base> bases, String table) {
        Log.d(LOG_TAG, "Getting db " + db);
        if (db == null) {
            ReportUser.warning(getContext(), "Failed to get hold of db");
            throw new DBException();
        }
        try {
            db.delete(table, null, null);
        } catch (android.database.sqlite.SQLiteException e) {
            Log.d(LOG_TAG, "storeBaseToDB: " + e.getMessage());
        }

    }

    public void storeTeamsToDB(List<Team> teams) {
        Log.d(LOG_TAG, "Storing teams " + teams.size());
        SQLiteDatabase db = getWritableDatabase();
        storeBaseToDB(db, (List<Base>) (List<? extends Base>) teams, TEAM_TABLE);
        for (Team t : teams) {
            ContentValues values = buildContentValues(t);
            long rowId = db.insert(TEAM_TABLE, null, values);
            Log.d(LOG_TAG, " * " + rowId + " inserted " + t);
            if (rowId < 0) {
                Log.e(LOG_TAG, "ERROR inserting (" + rowId + "): " + t);
            }
        }
    }

    public List<Team> getTeamsFromDB() {
        String[] subProjectionArray = null;
        List<Team> teams = new ArrayList<Team>();
        Cursor cursor = getCursorFirst(TEAM_TABLE, subProjectionArray);

        while (!cursor.isAfterLast()) {
            teams.add(cursorToTeam(cursor));
            cursor.moveToNext();
        }
        closeCursor(cursor);
        return teams;
    }

    private Team cursorToTeam(Cursor cursor) {
        if (cursor == null) {
            Log.d(LOG_TAG, "Cursor is null in team database");
            return null;
        }
        Team t = new Team(cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2));
        return t;
    }


    public void storeTrainingPhasesToDB(List<TrainingPhase> TrainingPhases) {
        Log.d(LOG_TAG, "Storing TrainingPhases " + TrainingPhases.size());
        SQLiteDatabase db = getWritableDatabase();
        storeBaseToDB(db, (List<Base>) (List<? extends Base>) TrainingPhases, TRAININGPHASE_TABLE);
        for (TrainingPhase t : TrainingPhases) {
            ContentValues values = buildContentValues(t);
            long rowId = db.insert(TRAININGPHASE_TABLE, null, values);
            Log.d(LOG_TAG, " * " + rowId + " inserted " + t);
            if (rowId < 0) {
                Log.e(LOG_TAG, "ERROR inserting (" + rowId + "): " + t);
            }
        }
    }

    public List<TrainingPhase> getTrainingPhasesFromDB() {
        String[] subProjectionArray = null;
        List<TrainingPhase> trainingPhases = new ArrayList<TrainingPhase>();
        Cursor cursor = getCursorFirst(TRAININGPHASE_TABLE, subProjectionArray);

        while (!cursor.isAfterLast()) {
            trainingPhases.add(cursorToTrainingPhase(cursor));
            cursor.moveToNext();
        }
        closeCursor(cursor);
        return trainingPhases;
    }

    private TrainingPhase cursorToTrainingPhase(Cursor cursor) {
        if (cursor == null) {
            Log.d(LOG_TAG, "Cursor is null in TrainingPhase database");
            return null;
        }
        TrainingPhase t = new TrainingPhase(cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2));
        return t;
    }

    public void storeMedia(Media m) {
        Log.d(LOG_TAG, "Storing media" + m.fileName());
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = buildContentValues(m);
        values.put(TEAM_COLUMN_NAME, m.getTeam());
        values.put(URI_COLUMN_NAME, m.fileName());
        values.put(STATUS_COLUMN_NAME, m.getStatus());
        values.put(TRAININGPHASE_COLUMN_NAME, m.getTrainingPhase());
        values.put(MEMBER_COLUMN_NAME, m.getMember());
        values.put(DATE_COLUMN_NAME, m.getDate());

        long rowId = db.insert(MEDIA_TABLE, null, values);
        Log.d(LOG_TAG, " * " + rowId + " inserted " + m + " (Storing media)");
        if (rowId < 0) {
            Log.e(LOG_TAG, "ERROR inserting (" + rowId + "): " + m);
        }
    }

    public void storeMediaToDB(List<Media> media) {
        Log.d(LOG_TAG, "Storing Media " + media.size());
        SQLiteDatabase db = getWritableDatabase();

        // TODO: better removal of media present on server (keep local)

        for (Media m : media) {
            ContentValues values = buildContentValues(m);
            values.put(TEAM_COLUMN_NAME, m.getTeam());
            values.put(URI_COLUMN_NAME, m.fileName());
            values.put(STATUS_COLUMN_NAME, m.getStatus());
            values.put(TRAININGPHASE_COLUMN_NAME, m.getTrainingPhase());
            values.put(MEMBER_COLUMN_NAME, m.getMember());
            values.put(DATE_COLUMN_NAME, m.getDate());

            long rowId = db.insert(MEDIA_TABLE, null, values);
            Log.d(LOG_TAG, " * " + rowId + " inserted " + m + " (Storing media)");
            if (rowId < 0) {
                Log.e(LOG_TAG, "ERROR inserting (" + rowId + "): " + m);
            }
        }
    }

    public List<Member> getMembersFromDB() {
        String[] subProjectionArray = {TEAM_COLUMN_NAME};
        List<Member> members = new ArrayList<Member>();
        Cursor cursor = getCursorFirst(MEMBER_TABLE, subProjectionArray);

        while (!cursor.isAfterLast()) {
            members.add(cursorToMember(cursor));
            cursor.moveToNext();
        }
        closeCursor(cursor);
        return members;
    }

    public List<Media> getMediaFromDB() {
        String[] subProjectionArray = {TEAM_COLUMN_NAME, URI_COLUMN_NAME, STATUS_COLUMN_NAME,
                TRAININGPHASE_COLUMN_NAME, MEMBER_COLUMN_NAME, DATE_COLUMN_NAME};


        List<Media> media = new ArrayList<Media>();
        Cursor cursor = getCursorFirst(MEDIA_TABLE, subProjectionArray);

        while (!cursor.isAfterLast()) {
            Media m = cursorToMedia(cursor);
            media.add(m);
            cursor.moveToNext();
        }
        closeCursor(cursor);
        return media;
    }


    public void storeMembersToDB(List<Member> members) {
        Log.d(LOG_TAG, "Storing member " + members.size());
        SQLiteDatabase db = getWritableDatabase();
        storeBaseToDB(db, (List<Base>) (List<? extends Base>) members, MEMBER_TABLE);

        for (Member m : members) {
            ContentValues values = buildContentValues(m);
            // Add team to member
            values.put(TEAM_COLUMN_NAME, m.getTeamUuid());

            long rowId = db.insert(MEMBER_TABLE, null, values);
            Log.d(LOG_TAG, " * " + rowId + " inserted " + m);
            if (rowId < 0) {
                Log.e(LOG_TAG, "ERROR inserting (" + rowId + "): " + m);
            }
        }
    }

    private Member cursorToMember(Cursor cursor) {
        if (cursor == null) {
            Log.d(LOG_TAG, "Cursor is null in member database");
            return null;
        }
//        Log.d(LOG_TAG, "Cursor in member database" + cursor + " count: " + cursor.getColumnCount() + "  name: " + cursor.getColumnName(0));
        Member m = new Member(cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3));
        return m;
    }


    public boolean updateMediaState(Media m, int state) {
        ContentValues values = buildContentValues(m);
        values.put(STATUS_COLUMN_NAME, state);
        SQLiteDatabase db = getWritableDatabase();

        long rows = db.update(MEDIA_TABLE,
                values, "uuid = ? ",
                new String[]{m.getUuid()});

        Log.d(LOG_TAG, " * " + rows + " updated" + m);

        return rows == 1;
    }


    public boolean updateMediaStateCreated(Media m, String uuid) {
        Log.d(LOG_TAG, "Updating media element: " + m + " " + uuid);
        ContentValues values = new ContentValues();
        values.put(STATUS_COLUMN_NAME, Media.MEDIA_STATUS_CREATED);
        values.put(UUID_COLUMN_NAME, uuid);
        SQLiteDatabase db = getWritableDatabase();

/*
        List<Media> mediaList  = getMediaFromDB();
        for (Media med: mediaList) {
            Log.d(LOG_TAG, " * media: " + m.getUuid() + " " + m.fileName());
        }
*/
        long rows = db.update(MEDIA_TABLE,
                values, "  " + URI_COLUMN_NAME + " = ? ",
                new String[]{m.fileName()});

        Log.d(LOG_TAG, " * Updating media element " + rows + " updated to: " + uuid  + " (media)");

/*
        Log.d(LOG_TAG, "Listing all Media after status update");
        mediaList  = getMediaFromDB();
        for (Media med: mediaList) {
            Log.d(LOG_TAG, " * media: " + m.getUuid() + " " + m.fileName());
        }
*/
        return rows == 1;
    }

    private Media cursorToMedia(Cursor cursor) {
        if (cursor == null) {
            Log.d(LOG_TAG, "Cursor is null in member database");
            return null;
        }

/*
UUID_COLUMN_NAME,
NAME_COLUMN_NAME,
CLUB_COLUMN_NAME,
TEAM_COLUMN_NAME
URI_COLUMN_NAME
STATUS_COLUMN_NAME
TRAININGPHASE_COLUMN_NAME
MEMBER_COLUMN_NAME
DATE_COLUMN_NAME
*/
        //  Log.d(LOG_TAG, "Media: ");
        String uuid = cursor.getString(0);
        String name = cursor.getString(1);
        String club = cursor.getString(2);

        String team = cursor.getString(3);
        String uri = cursor.getString(4);
        int status = cursor.getInt(5);
        String tp = cursor.getString(6);
        String member = cursor.getString(7);
        long date = cursor.getLong(8);
/*
        Log.d(LOG_TAG, " creating Media from db:  uri: " + uri);
        Log.d(LOG_TAG, "    * uuid:  " + uuid);
        Log.d(LOG_TAG, "    * name:  " + name);
        Log.d(LOG_TAG, "    * club:  " + club);
        Log.d(LOG_TAG, "    * team:  " + team);
        Log.d(LOG_TAG, "    * uri:   " + uri);
        Log.d(LOG_TAG, "    * status:" + status);
        Log.d(LOG_TAG, "    * tp:    " + tp);
        Log.d(LOG_TAG, "    * member:" + member);
        Log.d(LOG_TAG, "    * date:  " + date);
*/
        Media m = new Media(uuid, name, club,
                uri, status, date,
                team, tp, member);

        return m;
    }


    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (isCreating && currentDB != null) {
            return currentDB;
        }
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (isCreating && currentDB != null) {
            return currentDB;
        }
        return super.getReadableDatabase();
    }

    public boolean updateMediaReplaceDownloadedFile(Media m, String file) {
        Log.d(LOG_TAG, "Updating media element: " + m.getUuid() + "   new file: " + file);
        ContentValues values = new ContentValues();
        values.put(STATUS_COLUMN_NAME, Media.MEDIA_STATUS_DOWNLOADED);
        values.put(URI_COLUMN_NAME, file);
        SQLiteDatabase db = getWritableDatabase();

        long rows = db.update(MEDIA_TABLE,
                values, "  " + UUID_COLUMN_NAME + " = ? ",
                new String[]{m.getUuid()});

        Log.d(LOG_TAG, " * " + rows + " updated to downloaded");


        return rows == 1;
    }

}

