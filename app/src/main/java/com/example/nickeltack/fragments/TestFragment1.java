package com.example.nickeltack.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.nickeltack.R;
import com.example.nickeltack.monitor.WaveformView;
import com.example.nickeltack.starting.CheckableCirclesBar;
import com.example.nickeltack.starting.MultiVibratingDotView;
import com.example.nickeltack.starting.WaveformCircleView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TestFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TestFragment1 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int index = 0;

    private WaveformView waveformView;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    public TestFragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TestFragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static TestFragment1 newInstance(String param1, String param2) {
        TestFragment1 fragment = new TestFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            //waveformView.onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //waveformView.onPermissionGranted();
            } else {
                Toast.makeText(getContext(), "权限被拒绝，无法录音", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_test1, container, false);

//        VibratingDotView vibratingDotView = rootView.findViewById(R.id.vibratingDotViewTest1);
//        vibratingDotView.setVibrationFrequency(200); // 频率为200ms
//        vibratingDotView.startContinuousVibration();

//        waveformView = rootView.findViewById(R.id.vibratingDotViewTest1);
//        requestAudioPermission();
//        //waveformView.startRecording();
//
//        Button button2 = rootView.findViewById(R.id.button2);
//        button2.setOnClickListener(view -> waveformView.startRecording());

//        WaveformCircleView waveformCircleView = rootView.findViewById(R.id.vibratingDotViewTest1);
        requestAudioPermission();
//        waveformCircleView.vibrate(1f);
//        Button button2 = rootView.findViewById(R.id.button2);
        //button2.setOnClickListener(view -> waveformCircleView.startRecording());


//        CheckableCirclesBar checkalbeCirclesBar = rootView.findViewById(R.id.checkableCirclesBar);
//        button2.setOnClickListener(view -> {
//            checkalbeCirclesBar.setCheck(index,true);
//            index = (index+1) %4;
//        }
//        );

//        VibratingDotCircleView vibratingDotCircleView = rootView.findViewById(R.id.vibratingDotCircleViewTest);
//        vibratingDotCircleView.setNumDots(6);
//        vibratingDotCircleView.setIntervals(new int[]{400,400,200,200,200,200});
//        vibratingDotCircleView.setDotsAngles(new double[]{Math.PI/2,Math.PI/2,Math.PI/4,Math.PI/4,Math.PI/4});
//        vibratingDotCircleView.startNonUniformVibrating();

//        MultiVibratingDotView multiVibratingDotView = rootView.findViewById(R.id.vibratingDotCircleViewTest);

//        vibratingDotCircleView.stopNonUniformVibrating();
//        vibratingDotCircleView.setNumDots(5);
//        vibratingDotCircleView.setIntervals(new int[]{200,200,200,200,200});
//        vibratingDotCircleView.setDotsAngles(new double[]{Math.PI*0.4f});
//        vibratingDotCircleView.startNonUniformVibrating();


        return rootView;
    }
}