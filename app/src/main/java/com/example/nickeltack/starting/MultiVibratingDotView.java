package com.example.nickeltack.starting;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.example.nickeltack.metronome.AudioManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiVibratingDotView extends View {
    private Paint paint;
    private int centerColor = Color.GRAY; // 中心圆颜色
    private int[] circleColors; // 每个圈的颜色
    private float[] circleRadii; // 每个圈的半径
    private int numCirclesB = 16; // 每圈的小圆数量
    private int numCirclesC = 32;
    private int duration = 100; // 动画时长

    private int smallDelay = 10;
    private float centerRadius = 100f; // 中心圆半径
    private int outerCircleRadius = 150; // 外圈圆的半径
    private int outermostCircleRadius = 200; // 最外圈圆的半径
    private float centerX, centerY; // 圆心位置

    private Context context;

    // 每个圈的缩放因子，分别控制每个圆的放大动画
    private float[] scaleFactorsA = {1f}; // 中心圆的缩放因子
    private float[] scaleFactorsB; // 外圈圆的缩放因子
    private float[] scaleFactorsC; // 最外圈圆的缩放因子-

    private AnimatorSet[] animatorSets;
    private int animatorIndex = 0;

    private int[] soundIds;
    private float[] soundVolumes;
    private AudioManager audioManager;
    private int soundIndex = 0;
    private int soundsNumber = 1;

    public MultiVibratingDotView(Context context) {
        this(context, null);
    }

    public MultiVibratingDotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiVibratingDotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        // 初始化每圈的小圆颜色
        circleColors = new int[3]; // 三圈，分别是中心圆、外圈圆、最外圈圆
        circleRadii = new float[3];
        circleColors[0] = centerColor;
        circleColors[1] = Color.GRAY;
        circleColors[2] = Color.GRAY;

        scaleFactorsB = new float[numCirclesB];
        scaleFactorsC = new float[numCirclesC];
        Arrays.fill(scaleFactorsB,1f);
        Arrays.fill(scaleFactorsC,1f);

        animatorSets = new AnimatorSet[4];

        animatorSets[0] = initAnimator(Color.RED);
        animatorSets[1] = initAnimator(Color.YELLOW);
        animatorSets[2] = animatorSets[1];
        animatorSets[3] = animatorSets[1];

        // 设置点击和长按事件
        setOnClickListener(v -> onShortPress());  // 短按
        setOnLongClickListener(v -> {
            onLongPress(); // 长按
            return true;   // 返回true表示长按事件已经处理
        });

        audioManager = AudioManager.getInstance(getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;

        // 绘制中心圆 A[0]
        paint.setColor(circleColors[0]);
        canvas.drawCircle(centerX, centerY, centerRadius * scaleFactorsA[0], paint);

        // 绘制外圈圆 B
        for (int i = 0; i < numCirclesB; i++) {
            float angle = (float) (i * 2 * Math.PI / numCirclesB);
            float radius = outerCircleRadius * scaleFactorsB[i];
            float x = centerX + (radius * (float) Math.cos(angle));
            float y = centerY + (radius * (float) Math.sin(angle));
            paint.setColor(circleColors[1]);
            canvas.drawCircle(x, y, 20 * scaleFactorsB[i], paint);
        }

        // 绘制最外圈圆 C
        for (int i = 0; i < numCirclesC; i++) {
            float angle = (float) (i * 2 * Math.PI / numCirclesC);
            float radius = outermostCircleRadius * scaleFactorsC[i];
            float x = centerX + (radius * (float) Math.cos(angle));
            float y = centerY + (radius * (float) Math.sin(angle));
            paint.setColor(circleColors[2]);
            canvas.drawCircle(x, y, 10 * scaleFactorsC[i], paint);
        }
    }

    public AnimatorSet initAnimator(int color) {
        // 动画：中心圆放大并变色
        ValueAnimator centerColorAnimator = ValueAnimator.ofArgb(circleColors[0], color);
        centerColorAnimator.setDuration(duration/2);
        centerColorAnimator.addUpdateListener(animation -> {
            circleColors[0] = (int) animation.getAnimatedValue(); // 更新中心圆颜色
        });

        ValueAnimator centerScaleAnimator = ValueAnimator.ofFloat(1f, 1.2f); // 中心圆的放大
        centerScaleAnimator.setDuration(duration/2);
        centerScaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // 平滑过渡
        centerScaleAnimator.addUpdateListener(animation -> {
            scaleFactorsA[0] = (float) animation.getAnimatedValue();
            invalidate();
        });

        // 外圈圆放大并变色，逐个动画依次进行
        AnimatorSet outerCircleAnimatorSet = new AnimatorSet();
        List<Animator> outerCircleAnimators = new ArrayList<>();
        for (int i = 0; i < numCirclesB; i++) {
            // 颜色变化动画
            ValueAnimator outerColorAnimator = ValueAnimator.ofArgb(circleColors[1], color/2);
            outerColorAnimator.setDuration(duration);
            outerColorAnimator.addUpdateListener(animation -> {
                circleColors[1] = (int) animation.getAnimatedValue(); // 更新外圈圆颜色
            });

            // 缩放动画
            ValueAnimator outerScaleAnimator = ValueAnimator.ofFloat(1f, 1.2f); // 外圈圆的放大
            outerScaleAnimator.setDuration(duration);
            outerScaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // 平滑过渡
            int finalI = i;
            outerScaleAnimator.addUpdateListener(animation -> {
                scaleFactorsB[finalI] = (float) animation.getAnimatedValue();
                invalidate();
            });

            outerScaleAnimator.setStartDelay(smallDelay); // 每个外圈圆延迟逐个放大

            outerCircleAnimators.add(outerColorAnimator);
            outerCircleAnimators.add(outerScaleAnimator);
        }
        outerCircleAnimatorSet.playTogether(outerCircleAnimators);

        // 最外圈圆放大并变色，逐个动画依次进行
        AnimatorSet outermostCircleAnimatorSet = new AnimatorSet();
        List<Animator> outermostCircleAnimators = new ArrayList<>();
        for (int i = 0; i < numCirclesC; i++) {
            // 颜色变化动画
            ValueAnimator outermostColorAnimator = ValueAnimator.ofArgb(circleColors[2], color/4);
            outermostColorAnimator.setDuration(duration);
            outermostColorAnimator.addUpdateListener(animation -> {
                circleColors[2] = (int) animation.getAnimatedValue(); // 更新最外圈圆颜色
            });

            // 缩放动画
            ValueAnimator outermostScaleAnimator = ValueAnimator.ofFloat(1f, 1.2f); // 最外圈圆的放大
            outermostScaleAnimator.setDuration(duration);
            outermostScaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // 平滑过渡
            int finalI = i;
            outermostScaleAnimator.addUpdateListener(animation -> {
                scaleFactorsC[finalI] = (float) animation.getAnimatedValue();
                invalidate();
            });

            outermostScaleAnimator.setStartDelay(smallDelay); // 每个最外圈圆延迟逐个放大

            outermostCircleAnimators.add(outermostColorAnimator);
            outermostCircleAnimators.add(outermostScaleAnimator);
        }
        outermostCircleAnimatorSet.playTogether(outermostCircleAnimators);


        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 恢复圆形的大小和颜色
                circleColors[0] = Color.GRAY;
                circleColors[1] = Color.GRAY;
                circleColors[2] = Color.GRAY;
                Arrays.fill(scaleFactorsA,1f);
                Arrays.fill(scaleFactorsB,1f);
                Arrays.fill(scaleFactorsC,1f);
            }
        };

        // 创建整体的动画顺序
        AnimatorSet fullAnimatorSet = new AnimatorSet();
        fullAnimatorSet.playSequentially(
                centerColorAnimator, centerScaleAnimator,
                outerCircleAnimatorSet,
                outermostCircleAnimatorSet
        );

        fullAnimatorSet.addListener(listener);

        return fullAnimatorSet;
    }

    public void vibrate()
    {
        animatorSets[animatorIndex].start();
        animatorIndex = (animatorIndex + 1) % 4;
        audioManager.playSound(soundIds[soundIndex],soundVolumes[soundIndex]);
        soundIndex = (soundIndex + 1) % soundsNumber;
    }


    // 短按事件
    private void onShortPress() {
        vibrate();
        Toast.makeText(getContext(), "Short Press", Toast.LENGTH_SHORT).show();
    }

    // 长按事件
    private void onLongPress() {
        Toast.makeText(getContext(), "Long Press", Toast.LENGTH_SHORT).show();
    }

    public void setLongPressListener(OnLongClickListener onLongClickListener)
    {
        setOnLongClickListener(onLongClickListener);
    }

    public void setSounds(int[] soundIds, float[] soundVolumes)
    {
        this.soundIds =soundIds;
        this.soundVolumes = soundVolumes;
        this.soundsNumber = soundIds.length;
        this.soundIndex = 0;
    }

}
