package com.example.nickeltack.monitor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class WaveformView extends View {
    private Paint paint;
    private int[] waveformData;
    private int viewWidth;
    private int viewHeight;
    private AudioRecord audioRecord;
    private int bufferSize;
    private boolean isRecording = false;
    private short[] audioBuffer;
    private Handler handler;

    private boolean isPermissionGranted = false;

    public WaveformView(Context context) {
        super(context);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        waveformData = new int[viewWidth];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int midY = viewHeight / 2;

        for (int i = 0; i < waveformData.length - 1; i++) {
            int startX = i;
            int startY = midY + waveformData[i];
            int stopX = i + 1;
            int stopY = midY + waveformData[i + 1];
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
    }

    // 外部调用此方法以通知权限状态
    public void onPermissionGranted() {
        isPermissionGranted = true;
        Toast.makeText(getContext(), "权限已获取", Toast.LENGTH_SHORT).show();
        startRecording();
    }

    // 启动音频录制
    public void startRecording() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && !isRecording) {
            int sampleRate = 44100; // 采样率（常见的值：44100 Hz）
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioBuffer = new short[bufferSize / 2];

            Toast.makeText(getContext(), "录制开始", Toast.LENGTH_SHORT).show();
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            audioRecord.startRecording();
            isRecording = true;

            // 处理音频数据的线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRecording) {
                        int readResult = audioRecord.read(audioBuffer, 0, audioBuffer.length);

                        if (readResult > 0) {
                            // 处理音频数据并更新波形图
                            processAudioData(audioBuffer);
                        }
                    }
                }
            }).start();

        }
    }

    // 停止录音
    public void stopRecording() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    // 处理音频数据并更新波形图
    private void processAudioData(short[] buffer) {
        // 计算当前音频数据的最大振幅
        int maxAmplitude = 0;
        for (short sample : buffer) {
            int amplitude = Math.abs(sample);
            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude;
            }
        }

        // 将最大振幅映射到屏幕坐标系
        int value = (int) (((float) maxAmplitude / Short.MAX_VALUE) * ((float) viewHeight / 2));

        // 将新数据添加到波形数据数组
        System.arraycopy(waveformData, 1, waveformData, 0, waveformData.length - 1);
        waveformData[waveformData.length - 1] = value;

        // 更新波形图
        handler.post(new Runnable() {
            @Override
            public void run() {
                invalidate(); // 刷新视图
            }
        });
    }
}
