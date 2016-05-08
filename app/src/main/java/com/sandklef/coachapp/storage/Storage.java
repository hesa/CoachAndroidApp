package com.sandklef.coachapp.storage;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
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
    private BaseStorageHelper   baseStorage;
    private List<Member>        members;
    private List<Team>          teams;
    private List<TrainingPhase> trainingPhases;
    private List<Media>         media;
    private List<LogMessage>    logs;

    private static Storage storage;


    private Context context;



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

    public List<Member> getMembers() throws StorageNoClubException {
        return baseStorage.getMembersFromDB();
    }

    public List<Member> getMembersTeam(String uuid) {
        return baseStorage.getMembersTeamFromDB(uuid);
    }

    public List<Team> getTeams() throws StorageNoClubException {
        if (teams==null) {
            teams = baseStorage.getTeamsFromDB();
        }
        return teams;
    }

    //   public  List<TrainingPhase> getTrainingPhasesFromDB()
    public List<TrainingPhase> getTrainingPhases() throws StorageNoClubException {
        trainingPhases = baseStorage.getTrainingPhasesFromDB();
        return trainingPhases;
    }

    public TrainingPhase getTrainingPhase(String uuid) {
        for (TrainingPhase tp: trainingPhases) {
            if (tp.getUuid().equals(uuid)){
                return tp;
            }
        }
        return null;
    }

    public Team getTeam(String uuid) {
        for (Team t: teams) {
            if (t.getUuid().equals(uuid)){
                return t;
            }
        }
        return null;
    }

    public List<Media> getMedia() throws StorageNoClubException {
        media = baseStorage.getMediaFromDB();
        return media;
    }

    public List<LogMessage> getLogMessage() {
        logs = baseStorage.getLogMessageFromDB();
        return logs;
    }

    public Media getMediaUuid(String uuid) {
        try {
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
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Media getMediaDate(String date) {
        try {
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
        }catch (StorageNoClubException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Media> getMediaTrainingPhase(String uuid) {
        try {
            if (media == null) {
                media = getMedia();
            }
            List<Media> filteredMedia = new ArrayList<Media>();
            Log.d(LOG_TAG, "Search for media using uuid : " + uuid + "  in sizes media: " + media.size());
            if (media != null) {
                for (Media m : media) {
                    if (m.getTrainingPhase().equals(uuid)) {
                        filteredMedia.add(m);
                    }
                }
            }
            return filteredMedia;
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Member getMemberTeam(String uuid) {
        try {
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
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Member getMemberUUid(String uuid) {
        try {
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
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Storage getInstance() {
        return storage;
    }

    private Storage(Context c) {
        this.context = c;
        baseStorage = new BaseStorageHelper(c);
            /*memberStorage = new MemberStorageHelper(context);
            teamStorage   = new TeamStorageHelper(context);
       */
        //        LocalStorage.getInstance().setCurrentClub(club);
        LocalStorage.getInstance().setCurrentTeam(null);
        LocalStorage.getInstance().setCurrentTrainingPhase(null);
        LocalStorage.getInstance().setCurrentMember(null);
    }

    public void setClubUuid(String club) {
        baseStorage.setClubUuid(club);
    }


    public static Storage newInstance(Context c) {
        storage = new Storage(c);

        return storage;
    }

    public void saveMedia(Media m) {
        try {
            baseStorage.storeMedia(m);
            if (media==null) {
                getMedia();
            }
            media.add(m);
        } catch (StorageNoClubException e) {
            e.printStackTrace();
        }
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


    public void downloadMediaFromServer(Context c, Media m) {
        try {
            StorageRemoteWorker.AsyncBundle bundle;
            StorageRemoteWorker srw;
            bundle =
                    new StorageRemoteWorker.AsyncBundle(Storage.MODE_DOWNLOAD,
                            new StorageRemoteWorker.SimpleAsyncBundle(0, m));
            srw =
                    new StorageRemoteWorker(LocalStorage.getInstance().getCurrentClub());
            srw.execute(bundle);
        } catch (StorageException e) {
            Log.e(LOG_TAG, "Failed uploading media on server");
            ReportUser.warning(c, "Failed uploading media on server");
        }
    }


    public void uploadMediaToServer(Context c, Media m) {
        try {
            StorageRemoteWorker.AsyncBundle bundle;
            StorageRemoteWorker srw;
            bundle =
                    new StorageRemoteWorker.AsyncBundle(Storage.MODE_UPLOAD,
                            new StorageRemoteWorker.SimpleAsyncBundle(0, m));
            srw =
                    new StorageRemoteWorker(LocalStorage.getInstance().getCurrentClub());
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
                    new StorageRemoteWorker(LocalStorage.getInstance().getCurrentClub());
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
                    new StorageRemoteWorker(LocalStorage.getInstance().getCurrentClub());
            srw.execute(bundle);
        } catch (StorageException e) {
            Log.e(LOG_TAG, "Failed updating media from server " + e.getMessage());
            e.printStackTrace();
            ReportUser.warning(c, "Failed updating media from server");
        }
    }

    public void log(String msg) {
        baseStorage.log(msg);
    }

    public List<LocalUser> getLocalUsers() {
        return baseStorage.getLocalUserFromDB();
    }

    public LocalUser getLocalUser(int id) {
        for (LocalUser lu: baseStorage.getLocalUserFromDB()) {
            if (lu.getId()==id) {
                return lu;
            }
        }
        return null;
    }

    public void storeLocalUser(LocalUser lu) {
        baseStorage.storeLocalUser(lu);
    }

}