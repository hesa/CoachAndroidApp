package com.sandklef.coachapp.storage;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import com.sandklef.coachapp.misc.CADateFormat;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.*;
import com.sandklef.coachapp.report.ReportUser;

public class Storage {

    private final static String LOG_TAG = Storage.class.getSimpleName();

    public static final int MODE_CREATE = 0;
    public static final int MODE_UPLOAD = 1;
    public static final int MODE_DOWNLOAD = 2;
    public static final int MODE_COMPOSITE = 3;


    //    private MemberStorageHelper memberStorage;
    //  private TeamStorageHelper teamStorage;
    private BaseStorageHelper baseStorage;
    private List<Member> members;
    private List<Team> teams;
    private List<TrainingPhase> trainingPhases;
    private List<Media> media;

    private static Storage storage;
    private static String currentClub;


    private Context context;

    private String currentTeam;
    private String currentTrainingPhase;
    private String currentMember;


    public void updateDB(List<Member> members,
                         List<Team> teams,
                         List<Media> media,
                         List<TrainingPhase> tps) {
        try {
            System.out.println("storing members:  " + members.size());
            baseStorage.storeMembersToDB(members);
            baseStorage.storeTeamsToDB(teams);
            baseStorage.storeTrainingPhasesToDB(tps);
            baseStorage.storeMediaToDB(media);

            this.members = members;
            this.teams = teams;
            this.trainingPhases = tps;
            this.media = media;
        } catch (DBException e) {
            Log.d(LOG_TAG, "Failed getting hold of a db");
                // TODO: remove report user stuff
            ReportUser.warning(context, "Failed to get hold of db... :(");
        }
    }

    public List<Member> getMembers() {
        return baseStorage.getMembersFromDB();
    }

    public List<Team> getTeams() {
        return baseStorage.getTeamsFromDB();
    }

    //   public  List<TrainingPhase> getTrainingPhasesFromDB()
    public List<TrainingPhase> getTrainingPhases() {
        return baseStorage.getTrainingPhasesFromDB();
    }

    public List<Media> getMedia() {
        media = baseStorage.getMediaFromDB();
        return media;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public Media getMediaUuid(String uuid) {
        if (media == null) {
            media = getMedia();
        }
        Log.d(LOG_TAG, "Search for media using uuid : " + uuid + "  in sizes media: " + media.size());
        if (media != null) {
            for (Media m : media) {
                if (m.getUuid().equals(uuid)) {
                    return m;
                }
            }
        }
        return null;
    }

    public Media getMediaDate(String date) {
        if (media == null) {
            media = getMedia();
        }
        Log.d(LOG_TAG, "Search for media using date : " + date + "  in sizes media: " + media.size());
        if (media != null) {
            for (Media m : media) {
                Log.d(LOG_TAG, " date comp: " + CADateFormat.getDateString(m.getDate()) + " ? " + date);
                if (CADateFormat.getDateString(m.getDate()).equals(date)) {
                    return m;
                }
            }
        }
        return null;
    }

    public Member getMemberTeam(String uuid) {
        if (members == null) {
            members = getMembers();
        }

        Log.d(LOG_TAG, "  members: " + members.size());

        if (members != null) {
            for (Member m : members) {
                if (m.getTeamUuid().equals(uuid)) {
                    return m;
                }
            }
        }
        return null;
    }

    public Member getMemberUUid(String uuid) {
        if (members == null) {
            members = getMembers();
        }
        if (members != null) {
            for (Member m : members) {
                if (m.getUuid().equals(uuid)) {
                    return m;
                }
            }
        }
        return null;
    }

    public static Storage getInstance() {
        return storage;
    }

    private Storage(String club, Context c) {
        this.context = c;
        baseStorage = new BaseStorageHelper(club, c);
        /*memberStorage = new MemberStorageHelper(context);
        teamStorage   = new TeamStorageHelper(context);
   */
        currentClub = club;
        currentTeam = null;
        currentTrainingPhase = null;
        currentMember = null;
    }

    public void setCurrentTeam(String t) {
        currentTeam = t;
    }

    public String getCurrentTeam() {
        return currentTeam;
    }

    public void setCurrentTrainingPhase(String tp) {
        currentTrainingPhase = tp;
    }

    public String setCurrentTrainingPhase() {
        return currentTrainingPhase;
    }

    public void setCurrentMember(String m) {
        currentMember = m;
    }

    public String getCurrentMember() {
        return currentMember;
    }

    public static Storage newInstance(String club, Context c) {
        storage = new Storage(club, c);
        currentClub = club;
        return storage;
    }

    public void saveMedia(Media m) {
        baseStorage.storeMedia(m);
    }

    public boolean updateMediaState(Media m, int state) {
        return baseStorage.updateMediaState(m, state);
    }

    public boolean updateMediaStateCreated(Media m, String uuid) {
        return baseStorage.updateMediaStateCreated(m, uuid);
    }

    public boolean updateMediaReplaceDownloadedFile(Media m, String file) {
        return baseStorage.updateMediaReplaceDownloadedFile(m, file);
    }


    public void uploadMediaToServer(Context c, Media m) {
        try {
            StorageRemoteWorker.AsyncBundle bundle;
            StorageRemoteWorker srw;
            bundle =
                    new StorageRemoteWorker.AsyncBundle(Storage.MODE_UPLOAD,
                            new StorageRemoteWorker.SimpleAsyncBundle(0, m));
            srw =
                    new StorageRemoteWorker(LocalStorage.getInstance().getCurrentClub(), c);
            srw.execute(bundle);
        } catch (StorageException e) {
            Log.e(LOG_TAG, "Failed uploading media on server");
            ReportUser.warning(c, "Failed uploading media on server");
        }
    }


    public void createMediaOnServer(Context c, Media m) {
        try {
            StorageRemoteWorker.AsyncBundle bundle;
            StorageRemoteWorker srw;
            bundle =
                    new StorageRemoteWorker.AsyncBundle(Storage.MODE_CREATE,
                            new StorageRemoteWorker.SimpleAsyncBundle(0, m));
            srw =
                    new StorageRemoteWorker(LocalStorage.getInstance().getCurrentClub(), c);
            srw.execute(bundle);
        } catch (StorageException e) {
            Log.e(LOG_TAG, "Failed creating media on server");
            ReportUser.warning(c, "Failed creating media on server");
        }
    }

    public void update(Context c) {
        try {
            StorageRemoteWorker.AsyncBundle bundle;
            StorageRemoteWorker srw;
            bundle =
                    new StorageRemoteWorker.AsyncBundle(Storage.MODE_COMPOSITE);
            srw =
                    new StorageRemoteWorker(LocalStorage.getInstance().getCurrentClub(), c);
            srw.execute(bundle);
        } catch (StorageException e) {
            Log.e(LOG_TAG, "Failed updating media from server " + e.getMessage());
            e.printStackTrace();
            ReportUser.warning(c, "Failed updating media from server");
        }
    }

}