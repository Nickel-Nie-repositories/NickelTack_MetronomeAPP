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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.nickeltack.starting.AccentEvent;
import com.example.nickeltack.starting.AccentEventListener;

import java.util.ArrayList;
import java.util.List;
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

    private List<AccentEventListener> listeners = new ArrayList<>();

    private int lastAmplitude = 2147483647; //保存上一次的数据用于计算差值，初值为最大值以避免第一个时刻必然识别为重音
    private int threshold = 12000; //基本阈值，低于这个阈值的不会识别为重音
    private int risingEdgeThreshold = 7800; //上升沿阈值，两个时刻差值大于这个值将会被识别为重音
    private int refractoryPeriod = 200; // 不应期，触发重音后将进入不应期，不应期内将不再会识别成重音
    private int fallingEdgeThreshold = 6800; //下降沿阈值，两个时刻差值大于这个值时将会提前解除不应期
    private boolean isInRefractoryPeriod = false; // 不应期标识，处于不应期将不会触发重音事件


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

        // 重音识别逻辑
        boolean isAvailable = !isInRefractoryPeriod;
        if(lastAmplitude - maxAmplitude > fallingEdgeThreshold)
        {
            isInRefractoryPeriod = false;
        } // 识别为重音的条件：高于基本阈值，上升沿差值高于阈值，非不应期
        else if(maxAmplitude > threshold && maxAmplitude- lastAmplitude > risingEdgeThreshold && isAvailable)
        {
            triggerEvent(maxAmplitude);
        }
        lastAmplitude = maxAmplitude;

    }

    // 注册监听器
    public void addAccentEventListener(AccentEventListener listener) {
        listeners.add(listener);
    }

    // 移除监听器
    public void removeAccentEventListener(AccentEventListener listener) {
        listeners.remove(listener);
    }

    // 触发事件
    public void triggerEvent(int amplitude) {
        AccentEvent event = new AccentEvent(this, amplitude, System.currentTimeMillis());
        for (AccentEventListener listener : listeners) {
            listener.onAccentEvent(event);
        }
    }

    public void SetListenerArguments(int threshold, int risingEdgeThreshold, int fallingEdgeThreshold, int refractoryPeriod)
    {
        this.threshold = threshold;
        this.risingEdgeThreshold = risingEdgeThreshold;
        this.fallingEdgeThreshold = fallingEdgeThreshold;
        this.refractoryPeriod = refractoryPeriod;
    }
}
