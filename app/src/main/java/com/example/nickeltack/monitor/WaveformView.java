package com.example.nickeltack.monitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class WaveformView extends View {
    private Paint paint;
    private int[] waveformData; // 存储每一帧的波形数据
    private int viewWidth;
    private int viewHeight;
    private Random random;
    private int frameIndex = 0;
    private final int FRAME_SIZE = 512; // 每次更新显示的波形数据长度
    private boolean isRunning = false;

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

        random = new Random();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        waveformData = new int[FRAME_SIZE];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 每帧开始时获取新的波形数据
        if (isRunning) {
            generateWaveformData();
        }

        // 绘制波形
        int midY = viewHeight / 2;
        int frameWidth = viewWidth / FRAME_SIZE;

        for (int i = 0; i < FRAME_SIZE - 1; i++) {
            int startX = i * frameWidth;
            int startY = midY + waveformData[i];
            int stopX = (i + 1) * frameWidth;
            int stopY = midY + waveformData[i + 1];
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
    }

    // 模拟音频波形数据
    private void generateWaveformData() {
        for (int i = 0; i < FRAME_SIZE; i++) {
            // 随机生成-50到50之间的数，模拟音频波形数据
            waveformData[i] = random.nextInt(100) - 50;
        }
        invalidate();  // 刷新视图，重新绘制
    }

    // 启动波形更新
    public void start() {
        isRunning = true;
        postInvalidate(); // 强制刷新UI
    }

    // 停止波形更新
    public void stop() {
        isRunning = false;
    }
}
