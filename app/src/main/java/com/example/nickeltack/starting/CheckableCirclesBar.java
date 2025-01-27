package com.example.nickeltack.starting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CheckableCirclesBar extends View {
    private int circleCount = 4; // 默认4个圆圈
    private int circleRadius = 60; // 圆圈半径
    private int padding = 5; // 胶囊形背景的内边距
    private boolean[] checkedState; // 存储每个圆圈的勾选状态
    private Paint circlePaint; // 圆圈的画笔
    private Paint checkPaint; // 对勾的画笔
    private Paint backgroundPaint; // 胶囊形背景的画笔
    private int outerRadius = 65;

    public CheckableCirclesBar(Context context) {
        super(context);
        init();
    }

    public CheckableCirclesBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckableCirclesBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化画笔
        circlePaint = new Paint();
        circlePaint.setColor(Color.GREEN);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(20);
        setNumber(4);

        checkPaint = new Paint();
        checkPaint.setColor(Color.RED);
        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeWidth(8);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);

        // 初始化勾选状态
        checkedState = new boolean[circleCount];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制胶囊形背景
        RectF backgroundRect = new RectF(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawRoundRect(backgroundRect, getHeight() / 2, getHeight() / 2, backgroundPaint);

        // 计算圆圈之间的间距
        float spaceBetween = (float) (getWidth() - 2 * padding - circleCount * 2 * outerRadius) / (circleCount - 1);

        // 绘制同心圆圈
        for (int i = 0; i < circleCount; i++) {
            float centerX = padding + outerRadius + i * (2 * outerRadius + spaceBetween);
            float centerY = (float) getHeight() / 2;

            // 绘制圆圈
            canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);

            // 如果该圆圈被勾选，绘制对勾
            if (checkedState[i]) {
                drawCheckMark(canvas, centerX, centerY);
            }
        }
    }

    // 绘制对勾图案
    private void drawCheckMark(Canvas canvas, float centerX, float centerY) {
        float offset = circleRadius / 0.8f;
        Path checkPath = new Path();
        checkPath.moveTo(centerX - offset, centerY);
        checkPath.lineTo(centerX, centerY + offset * 0.7f);
        checkPath.lineTo(centerX + offset, centerY - offset);
        canvas.drawPath(checkPath, checkPaint);
    }

    // 设置圆圈的数量
    public void setNumber(int number) {
        this.circleCount = number;
        checkedState = new boolean[circleCount]; // 重新分配勾选状态数组
        outerRadius = 320/circleCount;
        circleRadius = outerRadius - 18;
        invalidate(); // 重新绘制视图
    }

    // 设置某个圆圈的勾选状态
    public void setCheck(int index, boolean checked) {
        if (index >= 0 && index < circleCount) {
            checkedState[index] = checked;
            invalidate(); // 重新绘制视图
        }
    }
}
