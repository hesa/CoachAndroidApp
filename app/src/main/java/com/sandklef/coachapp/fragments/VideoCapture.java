package com.sandklef.coachapp.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;

import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.storage.LocalMediaStorage;
import com.sandklef.coachapp.storage.LocalStorage;

import java.io.File;
import java.io.IOException;

public class VideoCapture extends SurfaceView implements SurfaceHolder.Callback {

    private final static String LOG_TAG = VideoCapture.class.getSimpleName();
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context context;

    private MediaRecorder mediaRecorder;
    private final int maxDurationInMs = 20000;
    private final long maxFileSizeInBytes = 500000;
    private final int videoFramesPerSecond = 20;

    public static final int VIDEO_CAPTURE = 101;


    private enum CAMERA_MODE {
        VC_UNDEFINED,
        VC_OPEN,
        VC_CLOSED,
        VC_READY,
        VC_PREVIEW,
        VC_RECORD
    }

    private CAMERA_MODE cameraMode;

    public VideoCapture(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraMode = CAMERA_MODE.VC_UNDEFINED;
        this.context = context;
        Log.d(LOG_TAG, "VideoCapture(Context context, attrs)");
    }


    public void openCamera() {
        if (mCamera != null) {
            return;
//            Log.d(LOG_TAG, "openCamere()  release camera");
//            mCamera.release();
        }
        Log.d(LOG_TAG, "openCamere()  open camera()");
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mCamera == null) {
            Log.d(LOG_TAG, "openCamere()  NULL, so open camera(0)");
            mCamera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        Camera.Parameters params = mCamera.getParameters();
        params.setRecordingHint(true);
        mCamera.setParameters(params);
        Log.d(LOG_TAG, "openCamere()  camera: " + mCamera);
    }

    public void startPreview() {
        openCamera();
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            cameraMode = CAMERA_MODE.VC_PREVIEW;
        } catch (IOException e) {
            // ignore: tried to stop a non-existent preview
            Log.d(LOG_TAG, "startPreview: IOException:   " + e.getMessage());
        }
    }

    public void stopPreview() {
        Log.d(LOG_TAG, "stopPreview()");
        try {
            Log.d(LOG_TAG, " mCamera stopPreview()");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            cameraMode = CAMERA_MODE.VC_CLOSED;
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
    }

    private void stopRecord() {
        Log.d(LOG_TAG, "stopRecord()");
        // Numbered comments from API description:
        //     http://developer.android.com/guide/topics/media/camera.html#capture-video

        // 5 Stop Recording Video
        // 5.a Stop MediaRecorder
        mediaRecorder.stop();
        // 5.b Reset MediaRecorder
        mediaRecorder.reset();
        // 5.c Release MediaRecorder
        mediaRecorder.release();
        // 5.d lock to make sure future sessions can use camera
        mCamera.lock();
        // 6 Stop the Preview
        mCamera.stopPreview();
        // 7 Release Camera
        mCamera.release();

        mCamera = null;
        cameraMode = CAMERA_MODE.VC_CLOSED;
    }


    private void stopCamera() {
        Log.d(LOG_TAG, "stopCamera()");
        if (cameraMode == CAMERA_MODE.VC_PREVIEW) {
            Log.d(LOG_TAG, "stopCamera()");
            stopPreview();
        } else if (cameraMode == CAMERA_MODE.VC_RECORD) {
            stopRecord();
        } else {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(LOG_TAG, "surfaceCreated()  1");
//        Log.d(LOG_TAG, " mCamera Camera.open  (surfaceCreated)");
        //      cameraMode = CAMERA_MODE.VC_OPEN;


/*        try {
            Log.d(LOG_TAG, " mCamera start preview (surfaceCreated)");
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
  */
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
//        stopPreview();
        startPreview();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(LOG_TAG, " mCamera stopCamera (surfaceDestroyed)   mode: " + cameraMode);
        stopCamera();
    }

    public void stopRecording() {
        stopRecord();
    }


    public boolean startRecording(File f, int msecs) {

        Uri uri = Uri.fromFile(f);

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        Log.d(LOG_TAG, "  file: " + f.getParent() + " " + f + " " + uri);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri); // set the image file name
        intent.putExtra("android.intent.extra.durationLimit", 5);
        intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
        // start the image capture Intent
        //context.startActivity(intent);
        Activity activity = (Activity) context;
        activity.startActivityForResult(intent, VIDEO_CAPTURE);
        return true;
    }


    public boolean startRecording_old(Media m, int msecs) {
        try {
            //         Log.d(LOG_TAG, " mCamera unlock  (startRecording)");
            //       stopCamera();

            String fileName = m.fileName();
            // DEBUG HESA HESA HESA HESA
//            fileName = LocalStorage.getInstance().getNewMediaDir() + "/apa.mp4";
            Log.d(LOG_TAG, "Record to file: " + fileName);
            File tempFile = new File(fileName);


            Log.d(LOG_TAG, " startRecording --  openCamera()");
            openCamera();
            // Numbered comments from API description:
            //     http://developer.android.com/guide/topics/media/camera.html#capture-video

            // 1. Open Camera - Use the Camera.open() to get an instance of the camera object.
            Log.d(LOG_TAG, " startRecording --  MediaRecorder=" + mediaRecorder);
            if (mediaRecorder != null) {
                Log.d(LOG_TAG, " startRecording --  MediaRecorder release");
                mediaRecorder.release();
            }
            mediaRecorder = new MediaRecorder();
            Log.d(LOG_TAG, " startRecording --  MediaRecorder=" + mediaRecorder);

            // 2. Connect Preview - Prepare a live camera image preview
            //    by connecting a SurfaceView to the camera using Camera.setPreviewDisplay().
            Log.d(LOG_TAG, " startRecording --  set preview ...");
            mediaRecorder.setPreviewDisplay(mHolder.getSurface());
            mediaRecorder.setMaxFileSize(maxFileSizeInBytes);

            // 3. Start Preview - Call Camera.startPreview() to begin displaying the
            //    live camera images.
            Log.d(LOG_TAG, " startRecording --  set camera preview...");
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

            // 4 Start Recording Video - The following steps must be
            //   completed in order to successfully record video:

            // 4.a Unlock the Camera - Unlock the camera for use by MediaRecorder by calling Camera.unlock().
            Log.d(LOG_TAG, " startRecording --  unlock");
            mCamera.unlock();

            // 4.b Configure MediaRecorder
            Log.d(LOG_TAG, " startRecording --  configure mediarecorder");

            mediaRecorder.setCamera(mCamera);

            // Call this only before setOutputFormat().
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

            //  Call this only before setOutputFormat().
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            // Call this after setAudioSource()/setVideoSource() but before prepare().
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // Call this after setOutputFormat() and before prepare().
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

            // Call this after setOutputFormat() but before prepare()
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);


            // Call this after setOutFormat() but before prepare()
            mediaRecorder.setMaxDuration(msecs);

            // Must be called after setVideoSource ...
            mediaRecorder.setVideoFrameRate(videoFramesPerSecond);

            // Must be called after setVideoSource
            Log.d(LOG_TAG, "  size: " + getWidth() + "x" + getHeight());
            mediaRecorder.setVideoSize(getWidth(), getHeight());


            // Call this after setOutputFormat() but before prepare
            mediaRecorder.setOutputFile(tempFile.getPath());

            // 4.c Prepare MediaRecorder
            Log.d(LOG_TAG, " startRecording --  prepare");
            mediaRecorder.prepare();

            // 4.d Start MediaRecorder
            Log.d(LOG_TAG, " startRecording --  start()");
            mediaRecorder.start();

            cameraMode = CAMERA_MODE.VC_RECORD;

            int cnt = 5;
            while (cnt-- > 0) {
                Log.d(LOG_TAG, "  cnt: " + cnt);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Log.d(LOG_TAG, " startRecording --  STOP CAMERA!!");
            stopCamera();
            cameraMode = CAMERA_MODE.VC_CLOSED;
            startPreview();


            return true;
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, e.toString());
            e.printStackTrace();
            stopCamera();
            return false;
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
            stopCamera();
            return false;
        }
    }

}