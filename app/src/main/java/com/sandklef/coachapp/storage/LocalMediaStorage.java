package com.sandklef.coachapp.storage;


import com.sandklef.coachapp.model.Media;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.io.File;
import java.util.Date;

public class LocalMediaStorage  {
/*

    File dir ;

    public LocalMediaStorage() {
        dir = new File(LocalStorage.getInstance().getNewMediaDir());
        createMediaDir();
    }


    private Collection<Media> getMediaFiles(String[] extensions, boolean rec) {
        Collection<Media> list = new ArrayList<Media>();

        if (!dir.exists()) {
            return list;
        }
        File[] files = dir.listFiles();

        for (File f : files) {
            if (f.isDirectory()) {
                if (rec) {
                    list.addAll(getMediaFiles(extensions, rec));
                }
            } else {
                String name = f.getName().toLowerCase();
		
                for (String ext : extensions) {
                    if (name.endsWith(ext)) {
                        list.add(new Media(f));
                    }
                }
            }
        }
        return list;
    }


    public static String getMediaFileNamePrefix() {
        return LocalStorage.DEFAULT_COACHAPP_DATA_DIR;
    }

    public String getNextImageName() {
        return getMediaFileNamePrefix()+LocalStorage.COACHAPP_IMAGE_SUFFIX;
    }

    public String getNextMovieName() {
        return getMediaFileNamePrefix()+LocalStorage.COACHAPP_VIDEO_SUFFIX;
    }

    private boolean createMediaDir() {
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }
*/
}
