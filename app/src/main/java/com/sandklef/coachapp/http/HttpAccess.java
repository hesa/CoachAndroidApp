package com.sandklef.coachapp.http;

import com.sandklef.coachapp.misc.*;
import com.sandklef.coachapp.storage.LocalStorage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.MalformedURLException;

public class HttpAccess {

    private final static String LOG_TAG = HttpAccess.class.getSimpleName();


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

    private static int maxBufferSize = 1 * 1024 * 1024;

    private String urlBase;
    private String serverUrl;
    private String clubUri;


    public HttpAccess(String baseUrl, String clubUrl) {
        this.urlBase   = baseUrl;
        this.clubUri   = clubUrl;
        this.serverUrl = urlBase + CLUB_PATH + PATH_SEPARATOR + clubUri + PATH_SEPARATOR;
    }


/*    private HttpAccess(String urlBase,
                       String clubUrl) {
        this.urlBase   = urlBase;
        this.clubUri   = clubUrl;
        this.serverUrl = urlBase + CLUB_PATH + PATH_SEPARATOR + clubUri + PATH_SEPARATOR;
    }
*/

    public String sendHttpPost(StringEntity data,
                               String header,
                               String path) {
        HttpClient client = new DefaultHttpClient();
        String url = serverUrl + path;
        HttpResponse resp = null;

        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setHeader(CONTENT_STATUS, header);
            httpPost.setEntity(data);
            resp = client.execute(httpPost);
            return EntityUtils.toString(resp.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String readEntireCoachServer() {
        StringBuilder builder = new StringBuilder();
        HttpClient client     = new DefaultHttpClient();
        String url            = serverUrl+ COMPOSITE_PATH;

        HttpGet httpGet = new HttpGet(url);
        Log.d(LOG_TAG, "url: " + url);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode        = statusLine.getStatusCode();
            Log.d(LOG_TAG, "sc: " + statusCode);
            if (isResponseOk(statusCode)) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d(LOG_TAG, " line: " + line);
                    builder.append(line);
                }
            } else {
                System.err.println("Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }


    public String postTrainingPhase(String data, String header) {
        HttpResponse resp = null;

        //$ curl --data "{ \"trainingPhaseUuid\": \"$TRAINING_PHASE_UUID\" }" --header "Content-Type: application/json" --request POST localhost:3000/0.0.0/clubs/$CLUB_UUID/videos
        try {
            return sendHttpPost(new StringEntity(data), header, PATH_SEPARATOR + VIDEO_URL_PATH);
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "Problem with encoding :(");
        }
        return null;
    }

/*

    public boolean uploadTrainingPhaseVideo0(String videoUuid,
                                             String fileName) {
        try {
            String completeUrl = serverUrl + UUID_PATH + videoUuid + PATH_SEPARATOR + UPLOAD_PATH;

            Log.d(LOG_TAG, "Uploading file");
            Log.d(LOG_TAG, " * " + completeUrl );

            HttpURLConnection connection = null;
            DataOutputStream outputStream = null;
            DataInputStream inputStream = null;

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            FileInputStream fileInputStream = new FileInputStream(new File(fileName));

            URL url = new URL(completeUrl );
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                            + fileName + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean uploadTrainingPhaseVideo777(String videoUuid,
                                               String fileName) {
        String url = serverUrl + VIDEO_URL_PATH + UUID_PATH + videoUuid + PATH_SEPARATOR + VIDEO_URL_PATH;
        File file = new File(fileName);

        Log.d(LOG_TAG, "Uploading file");
        Log.d(LOG_TAG, " * " + url);
        Log.d(LOG_TAG, " * " + file.getPath());

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            InputStreamEntity reqEntity = new InputStreamEntity(
                    new FileInputStream(file), -1);
            reqEntity.setContentType("binary/octet-stream");
            reqEntity.setChunked(false); // Send in multiple parts if needed
            httppost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httppost);


            Log.d(LOG_TAG, " * response " + response);


        } catch (Exception e) {
            Log.d(LOG_TAG, " exception caught while uploading: " + e);
            Log.d(LOG_TAG, " exception caught while uploading: " + e.getStackTrace());

            // show error

            return false;
        }
        return true;
    }
*/

    private static boolean isBetweenInclusive(int value, int low, int high) {
        return ((value >= low) && (value <= high));
    }

    public static boolean isResponseOk(int response) {
        return isBetweenInclusive(response, HTTP_RESPONSE_OK_LOW, HTTP_RESPONSE_OK_HIGH);
    }

    public boolean uploadTrainingPhaseVideo(String videoUuid,
                                            String fileName) {

        //$ curl --data-binary @sample.3gp --insecure --request POST https://localhost/api/0.0.0/clubs/$CLUB_UUID/videos/uuid/$VIDEO_UUID/upload
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;


        String pathToOurFile = fileName;
        String urlServer = serverUrl + VIDEO_URL_PATH + UUID_PATH + videoUuid + PATH_SEPARATOR + UPLOAD_PATH;

        Log.d(LOG_TAG, "Upload server url: " + urlServer);

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;



        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));

            URL url    = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            Log.d(LOG_TAG, "connection: " + connection + "  uploading data to video: " + videoUuid);

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod(HTTP_POST);

            outputStream = new DataOutputStream(connection.getOutputStream());
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                Log.d(LOG_TAG, " writing data to stream");
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }


            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            Log.d("ServerCode", "" + serverResponseCode);
            Log.d("serverResponseMessage", "" + serverResponseMessage);

            // Responses from the server (code and message)
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            if (isResponseOk(serverResponseCode)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            Log.d(LOG_TAG, "exception: " + ex);
            ex.printStackTrace();
            return false;
        }

        return false;
    }


    public boolean downloadVideo(String file, String videoUuid) {
        try {
            //$ GET http://localhost:3000/v0.0.0/clubs/<ID>/videos/uuid/<ID>/download
//            String file = LocalStorage.getInstance().getDownloadMediaDir() + "/" + videoUuid + SERVER_VIDEO_SUFFIX;
            int TIMEOUT_CONNECTION = 5000;//5sec
            int TIMEOUT_SOCKET = 30000;//30sec
            String urlServer = serverUrl + VIDEO_URL_PATH + UUID_PATH  + videoUuid + PATH_SEPARATOR + DOWNLOAD_PATH ;

            URL url = new URL(urlServer);
            long startTime = System.currentTimeMillis();
            Log.i(LOG_TAG, "video download beginning: " + urlServer);

            //Open a connection to that URL.
            HttpURLConnection ucon = (HttpURLConnection) url.openConnection();

            ucon.setRequestMethod(HTTP_GET);

            //this timeout affects how long it takes for the app to realize there's a connection problem
            ucon.setReadTimeout(TIMEOUT_CONNECTION);
            ucon.setConnectTimeout(TIMEOUT_SOCKET);

            Log.d(LOG_TAG, " and now to ...1");

            //Define InputStreams to read from the HttpURLConnection
            // uses 3KB download buffer
            InputStream is = ucon.getInputStream();
            Log.d(LOG_TAG, " and now to ...1");
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
            Log.d(LOG_TAG, " and now to ...2 to file: " + file);
            FileOutputStream outStream = new FileOutputStream(file);
            Log.d(LOG_TAG, " and now to ..31");
            byte[] buff = new byte[5 * 1024];

            System.err.println("For those about to ... file: " + file);
            //Read bytes (and store them) until there is nothing more to read(-1)
            int len;
            while ((len = inStream.read(buff)) != -1) {
                Log.d(LOG_TAG, "Downloading...");
                outStream.write(buff, 0, len);
            }

            Log.d(LOG_TAG, "response code: " +            ucon.getResponseCode());

            //clean up
            outStream.flush();
            outStream.close();
            inStream.close();

            System.err.println("For those about to ...");

            Log.d(LOG_TAG, "download completed in "
                    + ((System.currentTimeMillis() - startTime) / 1000)
                    + " sec");
            return isResponseOk(ucon.getResponseCode());

        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, " shit, MalformedURLException " + e);
        } catch (IOException e) {
            Log.d(LOG_TAG, " shit, IOException " + e);

        }
        return false;
    }
}
