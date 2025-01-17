package com.example.nickeltack.metronome;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.example.nickeltack.R;


public class CommonMetronomeFragment extends Fragment {


    private Spinner beatsOption;

    private View visualDot;
    private Button playPauseButton;

    private boolean isPlaying = false;
    private int bpm = 120; // 默认节拍
    private int rhythm = 4; // 默认时值 1/4
    private Handler handler = new Handler();
    private int beatCount = 0; // 记录当前的节拍


    private SoundPool soundPool;

    private int clickSoundId;

    public CommonMetronomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                         Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_common_metronome, container, false);

//        bpmSpinner = findViewById(R.id.bpm_spinner);
//        rhythmGroup = findViewById(R.id.rhythm_group);
//        visualDot = findViewById(R.id.visual_dot);
//        playPauseButton = findViewById(R.id.play_pause_button);
//
//        // 设置BPM选择器的值变化监听
//        bpmSpinner.setOnItemSelectedListener((parentView, view, position, id) -> {
//            bpm = Integer.parseInt(parentView.getItemAtPosition(position).toString());
//        });
//
//        // 设置时值选择监听
//        rhythmGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            switch (checkedId) {
//                case R.id.rhythm_quarter:
//                    rhythm = 4;
//                    break;
//                case R.id.rhythm_eighth:
//                    rhythm = 8;
//                    break;
//                case R.id.rhythm_sixteenth:
//                    rhythm = 16;
//                    break;
//            }
//        });

//        AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_MEDIA)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .build();
//        soundPool = new SoundPool.Builder()
//                .setAudioAttributes(audioAttributes)
//                .setMaxStreams(10) // 同时支持最多10个声音
//                .build();

        //clickSoundId = soundPool.load(this, R.raw.click_sound, 1); // 假设点击声音文件为 click_sound.mp3

        // 设置播放/暂停按钮点击监听
        //playPauseButton.setOnClickListener(v -> togglePlayPause());

        return rootView;
    }

    private void togglePlayPause() {
        if (isPlaying) {
            stopMetronome();
        } else {
            startMetronome();
        }
    }

    private void startMetronome() {
        isPlaying = true;
        playPauseButton.setText("暂停");
        // 每个节拍的间隔时间
        long interval = 60000 / bpm;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBeat();
                if (isPlaying) {
                    handler.postDelayed(this, interval);
                }
            }
        }, 0);
    }

    private void runMetronome(long interval) {
        // 播放节拍声音并更新状态
        beatCount++;
        soundPool.play(clickSoundId, 1f, 1f, 0, 0, 1f); // 播放节拍声音

        // 如果正在播放，并且未达到时值，继续
        if (isPlaying) {
            long nextInterval = interval;
            if (beatCount % rhythm == 0) {
                // 更新可视化点等
            }
            playPauseButton.postDelayed(() -> runMetronome(nextInterval), nextInterval);
        }
    }

    private void stopMetronome() {
        isPlaying = false;
        playPauseButton.setText("播放");
        handler.removeCallbacksAndMessages(null);
    }

    private void updateBeat() {
        beatCount++;
        // 更新可视化点的闪烁效果
        visualDot.setVisibility(beatCount % rhythm == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release(); // 释放资源
        }
    }


}