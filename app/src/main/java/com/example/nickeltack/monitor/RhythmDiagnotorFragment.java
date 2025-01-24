package com.example.nickeltack.monitor;

import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.nickeltack.R;


public class RhythmDiagnotorFragment extends Fragment {

    private WaveformView waveformView;
    private TextView largeNumber, smallNumber;
    private Button startStopButton, exportButton, settingsButton;

    private boolean isRecording = false;

    private FrameLayout waveformContainer;

    private TextView logTextView;
    private ScrollView logScrollView;

    private GridBackgroundView gridBackgroundView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rhythm_diagnotor, container, false);

        // 初始化控件
        waveformView = view.findViewById(R.id.waveformView);
        largeNumber = view.findViewById(R.id.largeNumber);
        smallNumber = view.findViewById(R.id.smallNumber);
        startStopButton = view.findViewById(R.id.startStopButton);
        exportButton = view.findViewById(R.id.exportButton);
        settingsButton = view.findViewById(R.id.settingsButton);

        waveformContainer = view.findViewById(R.id.waveformContainer);
        gridBackgroundView = view.findViewById(R.id.over_grid_background);

        logTextView = view.findViewById(R.id.logTextView);
        logScrollView = view.findViewById(R.id.logScrollView);
        simulateLogUpdates();

        // 设置按钮点击事件
        setupButtons();

        return view;
    }

    private void setupButtons() {
        // 开始/停止按钮逻辑
        startStopButton.setOnClickListener(v -> {
            isRecording = !isRecording;
            startStopButton.setText(isRecording ? "停止" : "开始");

            if (isRecording) {
                gridBackgroundView.startAnimation();
                startRecording();
            } else {
                gridBackgroundView.stopAnimation();
                stopRecording();
            }
        });

        // 导出文件按钮逻辑
        exportButton.setOnClickListener(v -> exportFile());

        // 设置按钮逻辑
        settingsButton.setOnClickListener(v -> openSettings());
    }

    private void startRecording() {
        // 开始录音逻辑
        waveformView.startRecording();
    }

    private void stopRecording() {
        // 停止录音逻辑
        waveformView.stopRecording();
    }

    private void exportFile() {
        // 导出文件逻辑
    }

    private void openSettings() {
        // 打开设置逻辑
    }

    // 设置大数字内容和颜色
    public void setLargeNumber(String number, int color) {
        largeNumber.setText(number);
        largeNumber.setTextColor(color);
    }

    // 设置小数字内容和颜色
    public void setSmallNumber(String number, int color) {
        smallNumber.setText(number);
        smallNumber.setTextColor(color);
    }


    private void simulateLogUpdates() {
        // 模拟定时增加日志
        new Handler().postDelayed(() -> {
            addLogMessage("Clock Log Message " + System.currentTimeMillis());
            simulateLogUpdates(); // 循环调用
        }, 2000);
    }

    public void addLogMessage(String message) {
        logTextView.append(message + "\n");
        logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN)); // 滚动到底部
    }

    private void OnLoudSoundDetected()
    {
        addLogMessage("检测到重音");
    }


}