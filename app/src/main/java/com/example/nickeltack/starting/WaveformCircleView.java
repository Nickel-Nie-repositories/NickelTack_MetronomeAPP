package com.example.nickeltack.starting;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class WaveformCircleView extends View {

    private Paint mPaint;
    private Path mPath;
    private int mRadius;
    private float mAmplitude;
    private ValueAnimator mAnimator;
    private float animatorPhase;

    private AudioRecord audioRecord;
    private int bufferSize;
    private boolean isRecording = false;
    private short[] audioBuffer;

    private float[] randomArray =new float[360];

    private Handler handler = new Handler();

    private List<AccentEventListener> listeners = new ArrayList<>();

    private int lastAmplitude = 2147483647; //保存上一次的数据用于计算差值，初值为最大值以避免第一个时刻必然识别为重音
    private int threshold = 12000; //基本阈值，低于这个阈值的不会识别为重音
    private int risingEdgeThreshold = 7800; //上升沿阈值，两个时刻差值大于这个值将会被识别为重音
    private int refractoryPeriod = 200; // 不应期，触发重音后将进入不应期，不应期内将不再会识别成重音
    private int fallingEdgeThreshold = 6800; //下降沿阈值，两个时刻差值大于这个值时将会提前解除不应期
    private boolean isInRefractoryPeriod = false; // 不应期标识，处于不应期将不会触发重音事件
    private ValueAnimator backAnimator;
    private AnimatorSet fullAnimatorSet;

    public WaveformCircleView(Context context) {
        super(context);
        init();
    }

    public WaveformCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStrokeWidth(5);

        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRadius = Math.min(getWidth(), getHeight()) / 2;

        mPath.reset();
        // 绘制圆圈
        //mPath.addCircle(getWidth() / 2, getHeight() / 2, mRadius, Path.Direction.CW);

        // 绘制振动效果
        for (int i = 0; i <= 360; i++) { // 每5度绘制一个点
            float angle = (float) Math.toRadians(i);
            float offsetR = randomArray[i%360] * mAmplitude * mRadius * animatorPhase * 0.35f; // 振动幅度
            float r = mRadius - offsetR;
            float x = (float) (getWidth() / 2 + Math.cos(angle) * r);
            float y = (float) (getHeight() / 2 + Math.sin(angle) * r);
            if (i == 0) {
                mPath.moveTo(x, y);
            } else {
                mPath.lineTo(x, y);
            }
        }

        canvas.drawPath(mPath, mPaint);
    }

    // 振动方法
    public void vibrate(float amplitude) {
        mAmplitude = amplitude; // 更新振动幅度
        startAnimation();
    }

    private void startAnimation() {
        if (fullAnimatorSet != null && fullAnimatorSet.isRunning()) {
            Log.d("TAG_0","isRunning, Cancel Last animation.");
//            fullAnimatorSet.cancel();
            Handler uiThreadHandler = new Handler(Looper.getMainLooper());
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    fullAnimatorSet.cancel();  // 动画的执行和取消方法必须在UI线程中调用
                }});
        }

        isInRefractoryPeriod = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isInRefractoryPeriod = false;
            }
        }, refractoryPeriod);
        //Log.d("TAG_0","posted handler.");

        for (int i = 0; i < 360; i++) {
            randomArray[i] = Math.random() < 0.35f ? 0: (float) Math.random();
        }
        //Log.d("TAG_0","generated RandomArray.");

        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(350); // 持续时间
        mAnimator.addUpdateListener(animation -> {
            animatorPhase = (float) animation.getAnimatedValue();
            invalidate();
        });
        //Log.d("TAG_0","created animator.");

//        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                animatorPhase =0;
//            }
//        };
//
//        mAnimator.addListener(listener);

        backAnimator = ValueAnimator.ofFloat(1,0);
        backAnimator.setDuration(30);
        backAnimator.addUpdateListener(animation -> {
            animatorPhase = (float) animation.getAnimatedValue();
            invalidate();
        });
        //Log.d("TAG_0","created back_animator.");

        fullAnimatorSet = new AnimatorSet();
        fullAnimatorSet.playSequentially(mAnimator, backAnimator);
        //Log.d("TAG_0","created animatorSet.");

        //fullAnimatorSet.start();
        Handler uiThreadHandler = new Handler(Looper.getMainLooper());
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                fullAnimatorSet.start();  // 改为在主线程中执行动画
            }});
        //Log.d("TAG_0","started animator.");
    }

    public void startRecording() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && !isRecording) {
            int sampleRate = 44100; // 采样率（常见的值：44100 Hz）
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioBuffer = new short[bufferSize / 2];

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
                            processAudioData(audioBuffer);
                        }
                    }
                }
            }).start();

        }
    }

    public void stopRecording() {
        isRecording = false;
        lastAmplitude = 2147483647;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void processAudioData(short[] audioBuffer) {
        int maxAmplitude = 0;
        for (short sample : audioBuffer) {
            int amplitude = Math.abs(sample);
            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude;
            }
        }
        boolean isAvailable = !isInRefractoryPeriod;

        if(lastAmplitude - maxAmplitude > fallingEdgeThreshold)
        {
            isInRefractoryPeriod = false;
        }
        else if(maxAmplitude > threshold && maxAmplitude- lastAmplitude > risingEdgeThreshold && isAvailable) // 识别为重音的条件：高于基本阈值，上升沿差值高于阈值，非不应期
        {
            Log.d("TAG_0","detected an accent,do a vibration.");
            vibrate((float) (maxAmplitude - threshold) / (32767-threshold));
            //vibrate(1f);
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
