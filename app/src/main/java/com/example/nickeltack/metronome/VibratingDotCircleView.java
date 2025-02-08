package com.example.nickeltack.metronome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VibratingDotCircleView extends FrameLayout {

    private int numDots = 5; // 默认的振动点数量
    private float radius = 300; // 圆的半径
    private float dotSize = 30; // 振动点的初始大小
    private int interval = 200; // 振动间隔
    private int frequency = 1; // 振动频率
    private List<VibratingDotView> dots = new ArrayList<>();
    private Timer timer;
    private int currentDotIndex = 0;

    private Runnable vibrationRunnable;  // 控制非等距振动的Runnable
    private int[] intervals = {200, 100, 400};  // 默认间隔数组
    private int currentIntervalIndex = 0;  // 当前数组的索引
    private Handler handler = new Handler();  // 用来控制非匀速定时任务
    private double[] dotsAngles = {};

    private float touchX, touchY; // 用于存储触摸的 X 和 Y 坐标

    private SoundChangeEventListener listener;

    private String[] sounds;

    public VibratingDotCircleView(Context context) {
        super(context);
        init(context);
    }

    public VibratingDotCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VibratingDotCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //setOrientation(LinearLayout.VERTICAL);
        setWillNotDraw(false);
        setClipChildren(false); // 禁用裁剪，确保子视图不被裁剪
        setLongClickListener(); // 设置长按监听器

        // 非匀速振动控制
        vibrationRunnable = new Runnable() {
            @Override
            public void run() {
                // 触发当前索引的振动点
                VibratingDotView currentDot = dots.get(currentIntervalIndex);
                currentDot.startVibration();

                int nextInterval = intervals[currentIntervalIndex % intervals.length];
                // 更新当前振动点的索引
                currentIntervalIndex = (currentIntervalIndex + 1) % numDots;

                // 更新当前间隔，并设置下一次振动
                //int nextInterval = intervals[(currentIntervalIndex-1) % intervals.length];
                handler.postDelayed(this, nextInterval);
            }
        };

        updateDots();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算并绘制每个振动点
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // 绘制底下的圆圈线，作为连接振动点的底层
        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.WHITE);  // 设置圆圈的颜色为白色
        circlePaint.setStyle(Paint.Style.STROKE);  // 设置为只绘制圆圈的边框
        circlePaint.setStrokeWidth(5);  // 设置圆圈线的宽度

        // 设置圆圈的半径
        float circleRadius = radius;  // 使用你想要的圆的半径

        // 绘制圆圈
        canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);


        if (dotsAngles == null || dotsAngles.length == 0){

            // 根据数量和大小计算每个点的坐标
            for (int i = 0; i < numDots; i++) {
                float angle = (float) (2 * Math.PI * i / numDots);
                float x = (float) (centerX + radius * Math.cos(angle));
                float y = (float) (centerY + radius * Math.sin(angle));

                dots.get(i).setPosition(x, y);
                dots.get(i).draw(canvas);
            }

        }
        else
        {
            // 根据角度数组计算每个点的坐标
            double angle = Math.PI*1.5f;
            for (int i = 0; i < numDots; i++) {
                float x = (float) (centerX + radius * Math.cos(angle));
                float y = (float) (centerY + radius * Math.sin(angle));
                angle += dotsAngles[i % dotsAngles.length];
                dots.get(i).setPosition(x, y);
                dots.get(i).draw(canvas);
            }
        }

    }

    // 更新圆圈上的振动点数量和大小
    public void updateDots() {

        for (View dot : dots) {
            removeView(dot);  // 从父视图中移除
        }
        dots.clear();
        dotSize = 30f - (numDots * 1f); // 根据振动点数量调整大小

        // 创建振动点
        for (int i = 0; i < numDots; i++) {
            VibratingDotView dot = new VibratingDotView(getContext());
            dot.setSize(dotSize);
            dot.setSoundChangeListener(listener);
            if(sounds != null && sounds.length != 0 ){dot.setSoundByName(sounds[i % sounds.length]);}
            // 将控件添加到父视图
            this.addView(dot); // 添加到当前视图
            //dot.setClickable(true); // 确保每个振动点可以响应点击事件
            dots.add(dot);
        }

        // 开始控制振动
        //startVibration();
    }


    // 设置振动点数量
    public void setNumDots(int numDots) {
        this.numDots = numDots;
        updateDots();
        invalidate();
    }

    // 设置振动频率（控制振动间隔）
    public void setVibrationInterval(int interval) {
        this.interval = interval;
        startVibration();
    }

    // 启动流水灯效果
    @SuppressLint("DiscouragedApi")
    private void startVibration() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                post(() -> {
                    // 更新当前振动点，其他点保持不动
                    for (int i = 0; i < numDots; i++) {
                        if (i == currentDotIndex) {
                            dots.get(i).startVibration();
                        }
                    }
                    currentDotIndex = (currentDotIndex + 1) % numDots;
                    invalidate(); // 重绘
                });
            }
        }, 0, interval); // 振动间隔
    }

    private void setLongClickListener() {
        this.setOnLongClickListener(v -> {
            // 在长按事件中获取触摸位置并判断哪个振动点被触发
//            float touchX = getTouchX();
//            float touchY = getTouchY();

            // 根据触摸位置判断对应的振动点
            for (int i = 0; i < numDots; i++) {
                VibratingDotView dot = dots.get(i);
                float dotX = dot.getXPosition();
                float dotY = dot.getYPosition();

                // 如果触摸位置在振动点的范围内，则调用长按事件
                if (Math.pow(touchX - dotX, 2) + Math.pow(touchY - dotY, 2) <= Math.pow(dotSize, 2)) {
                    dot.onLongClick(); // 调用振动点的长按事件
                    return true; // 返回 true，表示事件已处理
                }
            }
            return false;
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 获取触摸事件的坐标
        touchX = event.getX(); // 获取相对于父控件的 X 坐标
        touchY = event.getY(); // 获取相对于父控件的 Y 坐标

        // 处理触摸事件
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 按下事件
                break;

            case MotionEvent.ACTION_MOVE:
                // 移动事件（如果需要做跟踪操作可以在这里处理）
                break;

            case MotionEvent.ACTION_UP:
                // 放开事件
                break;
        }
        return super.onTouchEvent(event);
    }

    // 开始循环振动
    public void startNonUniformVibrating() {
        handler.post(vibrationRunnable);
    }

    // 停止振动
    public void stopNonUniformVibrating() {
        handler.removeCallbacks(vibrationRunnable);
    }

    // 设置间隔数组
    public void setIntervals(int[] newIntervals) {
        this.intervals = newIntervals;
    }

    public void setDotsAngles(double[] newDotsAngles){
        this.dotsAngles = newDotsAngles;
        invalidate();
    }

    public String[] getSounds()
    {
        String[] sounds = new String[dots.size()];
        for (int i = 0; i< dots.size(); i++)
        {
            sounds[i] = dots.get(i).getSoundName();
        }
        return sounds;
    }

    public void setSounds(String[] sounds)
    {
        this.sounds = sounds;
        for (int i = 0; i< dots.size(); i++)
        {
            int finalI = i;
            VibratingDotView dot = dots.get(i);
            dot.post(()-> dot.setSoundByName(sounds[finalI]));
        }
    }

    public void setSoundChangeListener(SoundChangeEventListener soundChangeListener)
    {
        listener = soundChangeListener;
        for (VibratingDotView dot:dots) {
            dot.setSoundChangeListener(listener);
        }
    }

}
