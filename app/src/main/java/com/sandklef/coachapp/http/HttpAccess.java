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

    private static int maxBufferSize = 1 * 1024 * 1024;

    private String urlBase;
    private String serverUrl;
    private String clubUri;


    public HttpAccess(String baseUrl, String clubUrl)throws HttpAccessException  {
        if (baseUrl==null || clubUrl==null) {
            throw new HttpAccessException("NULL pointer passed to constructor (" + baseUrl + ", " + clubUrl + ")");
        }
        this.urlBase   = baseUrl;
        this.clubUri   = clubUrl;
        this.serverUrl = urlBase + HttpSettings.CLUB_PATH + HttpSettings.PATH_SEPARATOR + clubUri + HttpSettings.PATH_SEPARATOR;
    }


    public String sendHttpPost(StringEntity data,
                               String header,
                               String path)  throws HttpAccessException {
        HttpClient client = new DefaultHttpClient();
        String url = serverUrl + path;
        HttpResponse resp = null;
        String response=null;
        Log.d(LOG_TAG, "sendHttpPost()");

        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setHeader(HttpSettings.CONTENT_STATUS, header);
            httpPost.setEntity(data);
            resp = client.execute(httpPost);
            Log.d(LOG_TAG, resp + "  <==  sendHttpPost");
            Log.d(LOG_TAG, resp.getStatusLine().getStatusCode() + "  <==  sendHttpPost");
            if (HttpSettings.isResponseOk(resp.getStatusLine().getStatusCode())) {
                Log.d(LOG_TAG, " server response ok, returning data");
                response = EntityUtils.toString(resp.getEntity());
            } else {
                throw new HttpAccessException("sendHttpPost failed");
            }
        } catch (IOException e) {
            throw new HttpAccessException("sendHttpPost failed", e);
        }
        return response;
    }

    public String readEntireCoachServer() throws HttpAccessException {
        StringBuilder builder = new StringBuilder();
        HttpClient client     = new DefaultHttpClient();
        String url            = serverUrl+ HttpSettings.COMPOSITE_PATH;

        HttpGet httpGet = new HttpGet(url);
        Log.d(LOG_TAG, "Server url: " + url + "  in readEntireCoachServer()");
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode        = statusLine.getStatusCode();
            if (HttpSettings.isResponseOk(statusCode)) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.d(LOG_TAG, "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            throw new HttpAccessException("readEntireCoachServer failed", e);
        } catch (IOException e) {
            throw new HttpAccessException("readEntireCoachServer failed", e);
        }

        return builder.toString();
    }


    public String createVideo(String data, String header) throws HttpAccessException {
        HttpResponse resp = null;

        //$ curl --data "{ \"trainingPhaseUuid\": \"$TRAINING_PHASE_UUID\" }" --header "Content-Type: application/json" --request POST localhost:3000/0.0.0/clubs/$CLUB_UUID/videos
        try {
            return sendHttpPost(new StringEntity(data), header, HttpSettings.PATH_SEPARATOR + HttpSettings.VIDEO_URL_PATH);
        } catch (UnsupportedEncodingException e) {
            throw new HttpAccessException("Failed encoding", e);
        }
    }



    public void uploadTrainingPhaseVideo(String videoUuid,
                                            String fileName) throws HttpAccessException {

        //$ curl --data-binary @sample.3gp --insecure --request POST https://localhost/api/0.0.0/clubs/$CLUB_UUID/videos/uuid/$VIDEO_UUID/upload
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;


        String pathToOurFile = fileName;
        String urlServer = serverUrl + HttpSettings.VIDEO_URL_PATH +
                HttpSettings.UUID_PATH + videoUuid + HttpSettings.PATH_SEPARATOR +
                HttpSettings.UPLOAD_PATH;

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
            connection.setRequestMethod(HttpSettings.HTTP_POST);

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

        } catch (IOException e) {
            throw new HttpAccessException("Failed uploading trainingphase video", e);
        } catch (Exception e) {
            throw new HttpAccessException("Failed uploading trainingphase video", e);
        }
    }


    public void downloadVideo(String file, String videoUuid)  throws HttpAccessException{
        try {
            //$ GET http://localhost:3000/v0.0.0/clubs/<ID>/videos/uuid/<ID>/download
//            String file = LocalStorage.getInstance().getDownloadMediaDir() + "/" + videoUuid + SERVER_VIDEO_SUFFIX;
            int TIMEOUT_CONNECTION = 5000;//5sec
            int TIMEOUT_SOCKET = 30000;//30sec
            String urlServer = serverUrl + HttpSettings.VIDEO_URL_PATH +
                    HttpSettings.UUID_PATH  + videoUuid +
                    HttpSettings.PATH_SEPARATOR + HttpSettings.DOWNLOAD_PATH ;

            URL url = new URL(urlServer);
            long startTime = System.currentTimeMillis();
            Log.i(LOG_TAG, "video download beginning: " + urlServer);

            //Open a connection to that URL.
            HttpURLConnection ucon = (HttpURLConnection) url.openConnection();

            ucon.setRequestMethod(HttpSettings.HTTP_GET);

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
            if (!HttpSettings.isResponseOk(ucon.getResponseCode())) {
                throw new HttpAccessException("Failed downloading video, response from server " + ucon.getResponseCode());
            }
        } catch (Exception e) {
            throw new HttpAccessException("Failed downloading video", e);
        }
    }
}