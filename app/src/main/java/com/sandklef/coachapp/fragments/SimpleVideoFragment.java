package com.sandklef.coachapp.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.model.Media;

import coachassistant.sandklef.com.coachapp.R;

public class SimpleVideoFragment extends Fragment {

    private final static String LOG_TAG = SimpleVideoFragment.class.getSimpleName();

    private OnSimpleVideoListener mListener;

    public static SimpleVideoFragment newInstance() {
        SimpleVideoFragment fragment = new SimpleVideoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SimpleVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }


    public VideoCapture getVideoCapture() {
            return videoCapture;
    }

    private VideoCapture videoCapture;
    //private Button stop;

/*    public void startRecorder(Media m){
        videoCapture.startCapturingVideo(m.fileName());
    }

    public void stopRecorder(Media m){
        videoCapture.stopCapturingVideo();
    }
*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_simple_video, container, false);
        videoCapture = (VideoCapture) root.findViewById(R.id.videoView);
/*
        stop = (Button) root.findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Log.d(LOG_TAG, "onClick()");
                videoCapture.stopCapturingVideo();

                //setResult(Activity.RESULT_OK);
                //finish();
            }
        });
        */
        return root;
    }

/*    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

*/

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

    public interface OnSimpleVideoListener {
        // TODO: Update argument type and name
        public void onSimpleVideoInteraction(Uri uri);
    }


}
