package com.sandklef.coachapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.sandklef.coachapp.fragments.VideoCapture;

import coachassistant.sandklef.com.coachapp.R;

public class DummyVideo extends AppCompatActivity {
    private VideoCapture videoCapture;
    private Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_simple_video);

        videoCapture = (VideoCapture) findViewById(R.id.videoView);
        stop= (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                //videoCapture.stopCapturingVideo();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });


    }

}
