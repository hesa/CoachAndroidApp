package com.sandklef.coachapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sandklef.coachapp.filters.MediaFilterEngine;
import com.sandklef.coachapp.filters.MediaMemberFilter;
import com.sandklef.coachapp.filters.MediaStatusNameFilter;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Team;
import com.sandklef.coachapp.storage.LocalStorage;
import com.sandklef.coachapp.storage.Storage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import coachassistant.sandklef.com.coachapp.R;

public class ClubInfoActivity extends ActionBarActivity {

    private final static String LOG_TAG = ClubInfoActivity.class.getSimpleName();


    private AbsListView mListView;
    private ListAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_info);
        mListView = (AbsListView) findViewById(R.id.club_info_team_list);


    }

    private void setTextViewText(int id, String text) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(text);
    }

    public void onStart() {
        super.onStart();

        setTextViewText(R.id.teams_info,
                getResources().getString(R.string.info_teams) + "  " + Storage.getInstance().getTeams().size());

        setTextViewText(R.id.trainingphases_info,
                getResources().getString(R.string.info_trainingphases) + "  " + Storage.getInstance().getTrainingPhases().size());

        setTextViewText(R.id.members_info,
                getResources().getString(R.string.info_members) + "  " + Storage.getInstance().getMembers().size());

        setTextViewText(R.id.local_media_info,
                getResources().getString(R.string.info_media_server) + "  " + Storage.getInstance().getMedia().size());

        setTextViewText(R.id.server_media_info,
                getResources().getString(R.string.info_media_local) + "  " +
                        MediaFilterEngine.apply(Storage.getInstance().getMedia(),
                                MediaStatusNameFilter.newMediaFilterStatus(Media.MEDIA_STATUS_DOWNLOADED)).size());

        setTextViewText(R.id.deletable_media_info,
                getResources().getString(R.string.info_media_deletable) + "  " +
                        new File(LocalStorage.getInstance().getDeletableMediaDir()).listFiles().length);
        
        setTextViewText(R.id.instructional_media_info,
                getResources().getString(R.string.info_media_instructional) + "  " +
                        MediaFilterEngine.apply(Storage.getInstance().getMedia(),
                                new MediaMemberFilter()).size());


        List<String> teams = new ArrayList<String>();
        for (Team t: Storage.getInstance().getTeams()) {
            Log.d(LOG_TAG, " " + t.getName());
            teams.add(t.getName()+ " " + Storage.getInstance().getMembersTeam(t.getUuid()).size());
        }
        Log.d(LOG_TAG, "teams : " + teams.size());
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                teams);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);


    }


}


