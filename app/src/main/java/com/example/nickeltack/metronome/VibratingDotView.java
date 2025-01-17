package com.example.nickeltack.metronome;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.nickeltack.R;

import java.util.HashMap;
import java.util.Map;

public class VibratingDotView extends View {

    private Paint paint;
    private float centerX, centerY;
    private float radius = 50f; // 初始半径
    private float maxRadius = 200f; // 最大扩散半径
    private boolean isVibrating = false;
    private float currentRadius = radius;
    private ValueAnimator animator;
    private int vibrationFrequency = 500; // 默认频率为 500ms
    private Handler handler;
    private Runnable vibrationRunnable;
    private MediaPlayer mediaPlayer;
    private int selectedSound = 0; // 默认无声音，0表示无声音
    private final String[] soundOptions = {"【None】", "arcade game jump coin", "classic click", "modern technology select"};

    private AudioManager audioManager;

    //private SoundPool soundPool;
    private int soundId;
    //private Map<Integer, Integer> soundToColorMap; // 声音到颜色的映射
    private int currentColor = Color.WHITE; // 默认颜色为白色

    public VibratingDotView(Context context) {
        super(context);
        init(context);
    }

    public VibratingDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VibratingDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        audioManager = AudioManager.getInstance(context);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

//        // 初始化声音和颜色映射
//        soundToColorMap = new HashMap<>();
//        soundToColorMap.put(1, Color.RED);   // 声音1对应红色
//        soundToColorMap.put(2, Color.GREEN); // 声音2对应绿色
//        soundToColorMap.put(3, Color.BLUE);  // 声音3对应蓝色
//        soundToColorMap.put(-1, Color.WHITE); // 无声音对应白色

        handler = new Handler(Looper.getMainLooper());
        vibrationRunnable = new Runnable() {
            @Override
            public void run() {
                startVibration();
                handler.postDelayed(this, vibrationFrequency);
            }
        };

//        // 初始化 MediaPlayer
//        mediaPlayer = MediaPlayer.create(context, R.raw.modern_technology_select); // 默认声音
//        mediaPlayer.setLooping(false);

//        // 初始化SoundPool (替换了 MediaPlayer 的实现)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            soundPool = new SoundPool.Builder().setMaxStreams(1).build();
//        } else {
//            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
//        }
//        // 加载默认声音
//        soundId = soundPool.load(context, R.raw.modern_technology_select, 1);

        // 长按监听器
//        setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                showSoundSelectionDialog();
//                return true;
//            }
//        });

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        // 渐变效果：中心大，外部小
        @SuppressLint("DrawAllocation") RadialGradient gradient = new RadialGradient(centerX, centerY, currentRadius,
                    new int[]{currentColor, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        paint.setShader(gradient);

        // 默认填充颜色
        //paint.setColor(currentColor);


        canvas.drawCircle(centerX, centerY, currentRadius, paint);
    }

    public void startVibration() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(radius, maxRadius);
        animator.setDuration(200); // 振动扩展的时间
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                currentRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        audioManager.playSound(soundId);


//        // 播放声音
//        if (selectedSound != 0) {
//
//            mediaPlayer.setLooping(false); // 不循环播放
//            mediaPlayer.start(); // 播放声音
//        }

//        // （替换为soundPool实现）
//        // 播放声音
//        if (selectedSound != -1) {
//            // 播放声音
//            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
//        }

        animator.start();
    }

    public void stopVibration() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        isVibrating = false;
        currentRadius = radius; // 恢复到初始半径
        invalidate();
    }

    public void setVibrationFrequency(int frequency) {
        this.vibrationFrequency = frequency;
    }

    public void startContinuousVibration() {
        if (!isVibrating) {
            isVibrating = true;
            handler.post(vibrationRunnable); // 每隔 vibrationFrequency 频率执行一次振动
        }
    }

    public void stopContinuousVibration() {
        isVibrating = false;
        handler.removeCallbacks(vibrationRunnable); // 停止持续振动
    }

    // 设置声音的函数
    public void setSound(int soundId) {
        this.soundId = soundId;
        // 根据选择的声音，更新振动点颜色
        //currentColor =  (soundId == -1)? Color.WHITE : ColorManager.colorArray[soundId % ColorManager.colorArray.length];
        currentColor = audioManager.getColorBySoundId(soundId);
//        switch (selectedSound) {
//                        case 0: // 无声音
//                            mediaPlayer.reset();
//                            break;
//                        case 1: // Beep 1
//                            mediaPlayer = MediaPlayer.create(getContext(), R.raw.arcade_game_jump_coin);
//                            break;
//                        case 2: // Beep 2
//                            mediaPlayer = MediaPlayer.create(getContext(), R.raw.classic_click);
//                            break;
//                        case 3: // Beep 3
//                            mediaPlayer = MediaPlayer.create(getContext(), R.raw.modern_technology_select);
//                            break;
//                    }
        invalidate();  // 触发视图重绘
    }

    // 显示声音选择对话框
//    private void showSoundSelectionDialog(final Context context) {
//        new AlertDialog.Builder(context)
//                .setTitle("选择声音")
//                .setItems(soundOptions, (dialog, which) -> {
//                    selectedSound = which; // 更新选择的声音
//                    switch (selectedSound) {
//                        case 0: // 无声音
//                            mediaPlayer.reset();
//                            break;
//                        case 1: // Beep 1
//                            mediaPlayer = MediaPlayer.create(context, R.raw.arcade_game_jump_coin);
//                            break;
//                        case 2: // Beep 2
//                            mediaPlayer = MediaPlayer.create(context, R.raw.classic_click);
//                            break;
//                        case 3: // Beep 3
//                            mediaPlayer = MediaPlayer.create(context, R.raw.modern_technology_select);
//                            break;
//                    }
//                    Toast.makeText(context, "选择了: " + soundOptions[which], Toast.LENGTH_SHORT).show();
//                })
//                .setCancelable(true)
//                .show();
//    }
    private void showSoundSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择声音");

        // 声音选项
        //String[] soundOptions = this.soundOptions;
        String[] soundOptions = audioManager.getAllSoundsName();

        Log.d("TAG_0", String.format("sound number: %d", soundOptions.length) );

        // 使用自定义布局
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 0, 40, 0);

        // 为每个声音选项创建视图
        for (int i = 0; i < soundOptions.length; i++) {
            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(10, 10, 10, 10);

            // 创建颜色方块
            View colorBlock = new View(getContext());
            LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(50, 50);
            colorParams.setMargins(0, 0, 20, 0);  // 设置间隔
            colorBlock.setLayoutParams(colorParams);
            int color = audioManager.getColorBySoundId(audioManager.getSoundIdByName(soundOptions[i]));
            colorBlock.setBackgroundColor(color);  // 设置颜色
            // 使用GradientDrawable来设置背景颜色和边框
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(color); // 填充颜色
            drawable.setStroke(3, Color.BLACK); // 设置边框（黑色，宽度为3）

            colorBlock.setBackground(drawable);  // 设置背景为带边框的颜色方块

            // 创建文字
            TextView textView = new TextView(getContext());
            textView.setText(soundOptions[i]);
            textView.setTextSize(16);
            textView.setPadding(10, 0, 10, 0);

            // 添加颜色方块和文字到单独的布局
            itemLayout.addView(colorBlock);
            itemLayout.addView(textView);

            // 添加到总布局
            layout.addView(itemLayout);

            // 设置点击事件
            final int index = audioManager.getSoundIdByName(soundOptions[i]);
            itemLayout.setOnClickListener(v -> {
                // 选择声音并设置颜色
                setSound(index);
            });
        }

        // 设置对话框视图
        builder.setView(layout);
        builder.show();
    }

    public void setSize(float size) {
        this.radius = size;
        this.maxRadius = size*4;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int) (size * 2), (int) (size * 2));
        setLayoutParams(params);
        invalidate();
    }

    public void setPosition(float x, float y) {
        this.centerX = x;
        this.centerY = y;
        invalidate(); // 重新绘制
    }

    // 获取振动点的 X 坐标
    public float getXPosition() {
        return centerX;
    }

    // 获取振动点的 Y 坐标
    public float getYPosition() {
        return centerY;
    }


    public void onLongClick() {
        showSoundSelectionDialog();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // 判断触摸事件是否发生在圆形区域内
//        float dx = event.getX() - centerX;
//        float dy = event.getY() - centerY;
//        if (dx * dx + dy * dy <= radius * radius) {
//            // 如果点击在圆形范围内，执行点击事件
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                // 处理点击事件，例如弹出选择声音对话框
//                performClick();
//                return true;
//            }
//        }
//        return super.onTouchEvent(event);
//    }


}
