package com.example.nickeltack.starting;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.nickeltack.R;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class StartingBlockFragment extends Fragment {

    private Button startButton;
    private Button playPauseButton;
    private Button settingsButton;
    private boolean isStarted = false;
    private boolean isPlaying = false;

    private Handler handler;
    private Runnable vibrationRunnable;

    private MultiVibratingDotView multiVibratingDotView;
    private WaveformCircleView waveformCircle;

    private AverageAlgorithm AverageMode = AverageAlgorithm.ARITHMETIC_MEAN; // 平均模式，用于节拍间隔的计算
    private int interval = 200;
    private long[] accentTimes;

    private int startingAccentCount  = 4;
    private int detectedAccentNum = 0;

    public StartingBlockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_starting_block, container, false);

        // Get references to the views
        startButton = view.findViewById(R.id.startButton);
        playPauseButton = view.findViewById(R.id.playPauseButton);
        settingsButton = view.findViewById(R.id.settingsButton);

        multiVibratingDotView = view.findViewById(R.id.multiVibratingDot);
        waveformCircle = view.findViewById(R.id.waveformCircle);

        // Set the start button logic
        startButton.setOnClickListener(v -> onStartButtonClicked());

        // Set the play/pause button logic
        playPauseButton.setOnClickListener(v -> onPlayPauseButtonClicked());

        // Set the settings button logic
        settingsButton.setOnClickListener(v -> onSettingsButtonClicked());

        // set the runnable
        vibrationRunnable = new Runnable() {
            @Override
            public void run() {
                multiVibratingDotView.vibrate();
                waveformCircle.vibrate(1f);
                handler.postDelayed(this,interval);
            }
        };
        handler = new Handler(Looper.getMainLooper());

        // Disable start button if the game is paused
        updateStartButtonState();

        return view;
    }

    private void onStartButtonClicked() {
        // Handle start logic here
        if (!isPlaying) {
            // Do the initialization or start logic
            isStarted = true;
            showStartingDialog(getContext());

        }
    }

    private void onPlayPauseButtonClicked() {
        // Toggle between play and pause
        if (isPlaying) {
            isPlaying = false;
            playPauseButton.setText("开始");
            stopVibration();

        } else {
            isPlaying = true;
            playPauseButton.setText("暂停");
            startVibration();

        }
        updateStartButtonState();
    }

    private void onSettingsButtonClicked() {
        // Handle settings button click
        // For example, navigate to another fragment or show settings UI
        Toast.makeText(getContext(), "进入设置", Toast.LENGTH_SHORT).show();
    }

    private void updateStartButtonState() {
        playPauseButton.post(new Runnable() {
            @Override
            public void run() {
                playPauseButton.setEnabled(isStarted);
            }
        });
        startButton.post(new Runnable() {
            @Override
            public void run() {
                startButton.setEnabled(!isPlaying);
            }
        });
        settingsButton.post(new Runnable() {
            @Override
            public void run() {
                settingsButton.setEnabled(!isPlaying);
            }
        });

        // playPauseButton.setEnabled(isStarted);
        // startButton.setEnabled(!isPlaying);
    }

    // 计算并启动节拍，根据已经记录的重音的间隔计算节拍的间隔，
    private void calculateAndStartBeats()
    {
        long[] differences = CalculateUtils.calculateDifferences(accentTimes);
        Log.d("TAG_0","get differences.");
        interval = Math.toIntExact(Objects.requireNonNull(AverageAlgorithm.AverageAlgorithms.get(AverageMode)).apply(differences));
        Log.d("TAG_0","get interval.");

        onPlayPauseButtonClicked();
    }

    private void startVibration() {
        handler.postDelayed(vibrationRunnable,interval);
    }

    private void stopVibration(){
        handler.removeCallbacks(vibrationRunnable);
    }


    public void showStartingDialog(Context context) {
        // 创建一个布局容器
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setGravity(Gravity.CENTER);

        // 创建圆圈细线控件
        WaveformCircleView circleLineView = new WaveformCircleView(context);
        circleLineView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400));
        circleLineView.startRecording();

        // 创建可勾选的同心圆控件
        CheckableCirclesBar checkableCircleView = new CheckableCirclesBar(context);
        checkableCircleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300));

        // 将控件添加到布局中
        layout.addView(circleLineView);
        layout.addView(checkableCircleView);


        // 创建并显示Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        builder.setCancelable(true); //可以点击外部区域关闭
        AlertDialog dialog = builder.create();

        // 设置窗口关闭时的监听器
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                circleLineView.stopRecording();
            }
        });

        accentTimes = new long[startingAccentCount];
        detectedAccentNum = 0;
        circleLineView.addAccentEventListener(new AccentEventListener() {
            @Override
            public void onAccentEvent(AccentEvent event) {
                accentTimes[detectedAccentNum] = event.getCurrentTime();
                checkableCircleView.setCheck(detectedAccentNum,true);
                detectedAccentNum++;
                if(detectedAccentNum == startingAccentCount)
                {
                    calculateAndStartBeats();
                    dialog.cancel();
                }

            }
        });

        // 显示弹窗
        dialog.show();

    }


}