package com.sandklef.coachapp.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.StrictMode;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.sandklef.coachapp.fragments.TeamFragment;
import com.sandklef.coachapp.fragments.TraningPhasesFragment;
import com.sandklef.coachapp.json.JsonParser;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.storage.Storage;

import java.util.Locale;

import coachassistant.sandklef.com.coachapp.R;
// AppCompatActivity
public class MainActivity extends AppCompatActivity implements
        com.sandklef.coachapp.fragments.TeamFragment.TeamFragmentListener,
        com.sandklef.coachapp.fragments.TraningPhasesFragment.TrainingPhasesFragmentListener {

    public final static int TEAM_FRAGMENT_POSITION = 0;
    public final static int TP_FRAGMENT_POSITION = 1;
/*    public final static int TEAM_FRAGMENT_POSITION = 1;
    public final static int PERFORM_FRAGMENT_POSITION = 3;
*/
    private TeamFragment teamFragment;
    private TraningPhasesFragment tpFragment;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private static Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        Log.d(LOG_TAG, "mSectionsPagerAdapter: " + mSectionsPagerAdapter);
/*
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        TeamFragment fragment = new TeamFragment();
        fragmentTransaction.add(R.id.main_pager, fragment);
        fragmentTransaction.commit();
*/

    }


    @Override
    protected void onStart() {
            super.onStart();
        Log.d(LOG_TAG, " reading ...");

        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        storage = Storage.newInstance(getApplicationContext());

        /*
        JsonParser jsp = new JsonParser("e0b7098f-b7e1-4fe4-89bb-22c4d83f1141", getApplicationContext());
        jsp.update();
*/
        for (Member m: storage.getMembers()) {
            Log.d(LOG_TAG, " * " + m);
        }
    }

    public static Storage getStorage() {
        return storage;
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            Log.d(LOG_TAG, "MainActivity,   pos: " + position);
            switch (position) {
                case MainActivity.TEAM_FRAGMENT_POSITION:
                    Log.d(LOG_TAG, "MainActivity, create mainfragment" + position);
                    teamFragment = TeamFragment.newInstance();
                    Log.d(LOG_TAG, "MainActivity, create mainfragment" + teamFragment);
                    return (android.support.v4.app.Fragment) teamFragment;
                case MainActivity.TP_FRAGMENT_POSITION:
                    Log.d(LOG_TAG, "MainActivity, create mainfragment" + position);
                    tpFragment = TraningPhasesFragment.newInstance();
                    return (android.support.v4.app.Fragment) tpFragment;
                default:
                    return null;

            }
        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            Log.d(LOG_TAG, "getPagetitle pos: " + position);
            return "TiTLE" + position;
        }
    }

    public void onTeamFragmentInteraction(long id) {
        Log.d(LOG_TAG, "onTrainingphasesFragmentInteraction " + id + " => " + storage.getMembers().get((int) id));
        mViewPager.setCurrentItem(TP_FRAGMENT_POSITION, true);
        mSectionsPagerAdapter.notifyDataSetChanged();
    }
    public void onTrainingphasesFragmentInteraction(long id) {
        Log.d(LOG_TAG, "onTrainingphasesFragmentInteraction " + id + " => " + storage.getMembers().get((int) id));
        mViewPager.setCurrentItem(TEAM_FRAGMENT_POSITION, true);
        mSectionsPagerAdapter.notifyDataSetChanged();
    }


}
