package com.sandklef.coachapp.http;

/**
 * Created by hesa on 2016-02-25.
 */
public class HttpSettings {

    public static final String PATH_SEPARATOR = "/";
    public static final String CLUB_PATH      = "clubs"  + PATH_SEPARATOR;
    public static final String VIDEO_URL_PATH = "videos" + PATH_SEPARATOR;
    public static final String UUID_PATH      = "uuid"   + PATH_SEPARATOR;
    public static final String UPLOAD_PATH    = "upload";
    public static final String COMPOSITE_PATH = "composite";
    public static final String DOWNLOAD_PATH  = "download";
    public static final String CONTENT_STATUS = "Content-Type";

    public static final String HTTP_POST      = "POST";
    public static final String HTTP_GET       = "GET";

    public static final int HTTP_RESPONSE_OK_LOW  = 200;
    public static final int HTTP_RESPONSE_OK_HIGH = 299;

    private static boolean isBetweenInclusive(int value, int low, int high) {
        return ((value >= low) && (value <= high));
    }

    public static boolean isResponseOk(int response) {
        return isBetweenInclusive(response, HTTP_RESPONSE_OK_LOW, HTTP_RESPONSE_OK_HIGH);
    }


}
