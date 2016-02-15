package com.sandklef.coachapp.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentManager;

import com.sandklef.coachapp.activities.MainActivity;
import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Media;
import com.sandklef.coachapp.model.Member;
import com.sandklef.coachapp.model.Team;
import com.sandklef.coachapp.model.TrainingPhase;
import com.sandklef.coachapp.storage.LocalMediaStorage;
import com.sandklef.coachapp.storage.LocalStorage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import coachassistant.sandklef.com.coachapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private final static String LOG_TAG = TopFragment.class.getSimpleName();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MemberFragment mmFragment;

    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;


    private ViewPager mTopViewPager;
    private ViewPager mBottomViewPager;

    private BottomFragmentAdapter bottomPagerAdapter;
    private TopFragmentAdapter topPagerAdapter;

    private OnFragmentInteractionListener mListener;

    public static int BOTTOM_FRAGMENT_TEAM_INDEX = 0;
    public static int BOTTOM_FRAGMENT_TRAININGPHASE_INDEX = 1;
    public static int BOTTOM_FRAGMENT_MEMBER_INDEX = 2;
    public static int BOTTOM_FRAGMENT_LAST_INDEX = 3;

    public static int TOP_FRAGMENT_USER_INDEX = 0;
    public static int TOP_FRAGMENT_VIDEO_INDEX = 1;
    public static int TOP_FRAGMENT_LAST_INDEX = 2;


    public static TopFragment newInstance() {
        TopFragment fragment = new TopFragment();
        Bundle args = new Bundle();
        Log.d(LOG_TAG, "  newInstance()");
        fragment.setArguments(args);
        return fragment;
    }

    public TopFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "  onCreate");

    }

    public void onTeamFragmentInteraction(Team t) {
        Log.d(LOG_TAG, "onTrainingphasesFragmentInteraction " + t);
        LocalStorage.getInstance().setCurrentTeam(t.getUuid());
        showTrainingPhases();
    }

    public void onTrainingphasesFragmentInteraction(TrainingPhase tp) {
        Log.d(LOG_TAG, "onTrainingphasesFragmentInteraction " + tp);
        LocalStorage.getInstance().setCurrentTrainingPhase(tp.getUuid());
        showMember();
    }


    private boolean recordVideo() {
        DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Date today = Calendar.getInstance().getTime();
        String mediaDate = df.format(today);

        String newFileName = LocalMediaStorage.getMediaFileNamePrefix() +
                "/new/" + mediaDate + ".mp4";

        File newFile = new File(newFileName);
        String dirName = newFile.getParent();
        File dir = new File(dirName);
        Log.d(LOG_TAG, "  Dir:  " + dir.getPath());
        boolean created = dir.mkdirs();

        topPagerAdapter.getSimpleVideoFragment().getVideoCapture().startRecording(newFile,5*1000);
        return true;
    }


    public void onMemberInteraction(Member m) {
        Log.d(LOG_TAG, " onMemberInteraction " + m);
        LocalStorage.getInstance().setCurrentMember(m.getUuid());
        showVideo();

        recordVideo();
    }

    public void onMediaInteraction(long id) {
        Log.d(LOG_TAG, " onMediaInteraction " + id);
    }


    public void showTeams() {
        Log.d(LOG_TAG, "showTeams()");
        setBottomFragmentIndex(BOTTOM_FRAGMENT_TEAM_INDEX);
    }

    public void showTrainingPhases() {
        Log.d(LOG_TAG, "showTrainingPhases()");
        setBottomFragmentIndex(BOTTOM_FRAGMENT_TRAININGPHASE_INDEX);
    }

    public void showMember() {
        Log.d(LOG_TAG, "showMembers()");
        setBottomFragmentIndex(BOTTOM_FRAGMENT_MEMBER_INDEX);
    }

    public void showVideo() {
        Log.d(LOG_TAG, "showVideos()");
        setTopFragmentIndex(TOP_FRAGMENT_VIDEO_INDEX);
    }

    public void showUser() {
        Log.d(LOG_TAG, "showUser()");
        setTopFragmentIndex(TOP_FRAGMENT_USER_INDEX);
    }

    public void setTopFragmentIndex(int index) {
        mTopViewPager.setCurrentItem(index, true);
        topPagerAdapter.notifyDataSetChanged();
    }

    public void setBottomFragmentIndex(int index) {
        mBottomViewPager.setCurrentItem(index, true);
        bottomPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.d(LOG_TAG, "  onCreateView()");
        View root = inflater.inflate(R.layout.fragment_top, container, false);

        mBottomViewPager = (ViewPager) root.findViewById(R.id.bottom_pager);
        mTopViewPager = (ViewPager) root.findViewById(R.id.top_pager);
        Log.d(LOG_TAG, "  onCreateView()  vp: " + R.id.bottom_pager);
        Log.d(LOG_TAG, "  onCreateView()  vp: " + mBottomViewPager);

        bottomPagerAdapter = new BottomFragmentAdapter(getChildFragmentManager());
        topPagerAdapter = new TopFragmentAdapter(getChildFragmentManager());

        mBottomViewPager.setAdapter(bottomPagerAdapter);
        mTopViewPager.setAdapter(topPagerAdapter);

        setSwipeListener(mBottomViewPager, bottomPagerAdapter);

        return root;
    }

    private void setSwipeListener(final ViewPager bottomPager, final BottomFragmentAdapter bottomAdapter) {
        bottomPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int i, final float v, final int i2) {
            }

            @Override
            public void onPageSelected(final int i) {
                Fragment fragment = (Fragment) bottomAdapter.instantiateItem(bottomPager, i);
                Log.d(LOG_TAG, "  onPageSelected " + i);
                if (i == BOTTOM_FRAGMENT_MEMBER_INDEX) {
                    showVideo();
                } else {
                    showUser();
                }
            }

            @Override
            public void onPageScrollStateChanged(final int i) {
                Log.d(LOG_TAG, "  onPageScrollStateChanged " + i);
            }
        });
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
    //    Log.d(LOG_TAG, "  ---> onAttach()");
        super.onAttach(activity);
        //Log.d(LOG_TAG, "  --- onAttach()");
        try {
      //      Log.d(LOG_TAG, "  --- onAttach() 1");
            mListener = (OnFragmentInteractionListener) activity;
        //    Log.d(LOG_TAG, "  --- onAttach() 2");
        } catch (ClassCastException e) {
          //  Log.d(LOG_TAG, "  --- onAttach() exception: " + e);

            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        //Log.d(LOG_TAG, "  <--- onAttach()");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public static class BottomFragmentAdapter extends FragmentPagerAdapter {
        private final static String LOG_TAG = BottomFragmentAdapter.class.getSimpleName();


        public BottomFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
          //  Log.d(LOG_TAG, "  getCount()");
            return BOTTOM_FRAGMENT_LAST_INDEX;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
//            args.putInt(ChildFragment.POSITION_KEY, position);
            Log.d(LOG_TAG, "  getItem(" + position + ")");
            if (position == 0) {
                return TeamFragment.newInstance();
            } else if (position == 1) {
                return TrainingPhasesFragment.newInstance();
            } else {
                return MemberFragment.newInstance();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position + "Child Fragment " + position;
        }

    }

    public static class TopFragmentAdapter extends FragmentPagerAdapter {
        private final static String LOG_TAG = TopFragmentAdapter.class.getSimpleName();
        private SimpleVideoFragment videoFragment;

        public TopFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
         //   Log.d(LOG_TAG, "  getCount()");
            return TOP_FRAGMENT_LAST_INDEX;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
//            args.putInt(ChildFragment.POSITION_KEY, position);
            Log.d(LOG_TAG, "  getItem()");
            if (position == 0)
                return UserFragment.newInstance();

            videoFragment = SimpleVideoFragment.newInstance();
            return videoFragment;
//            return Camera2Fragment.newInstance();

        }

        public SimpleVideoFragment getSimpleVideoFragment() {
            return videoFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position + "Child Fragment " + position;
        }

    }

}
