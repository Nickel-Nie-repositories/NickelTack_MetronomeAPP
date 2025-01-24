package com.example.nickeltack.monitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class GridBackgroundView extends View {
    private Paint gridPaint;
    private final int gridSpacing = 300; // 网格间距
    private int gridOffset = 0; // 网格偏移量
    private int width, height;
    private Handler handler;

    Runnable runnable;

    public GridBackgroundView(Context context) {
        super(context);
        init();
    }

    public GridBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint = new Paint();
        gridPaint.setColor(Color.WHITE);
        gridPaint.setStrokeWidth(2);
        gridPaint.setAntiAlias(true);

        // 使用Handler定期更新网格偏移量，模拟网格移动
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                gridOffset -= 3; // 每次更新偏移
                if (gridOffset > gridSpacing) {
                    gridOffset = 0; // 重置偏移，模拟循环
                }
                invalidate(); // 刷新视图
                handler.postDelayed(this, 50); // 每50毫秒更新一次
            }
        };
        //handler.post(runnable); // 启动定时任务
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        width = getWidth();
        height = getHeight();

        // 绘制黑色背景
        canvas.drawColor(Color.BLACK);

        // 绘制网格
        drawGrid(canvas);
    }

    private void drawGrid(Canvas canvas) {
        // 绘制水平线
        for (int y = 2; y < height; y += gridSpacing) {
            canvas.drawLine(0, y, width, y, gridPaint);
        }

        // 绘制垂直线
        for (int x = gridOffset; x < width; x += gridSpacing) {
            canvas.drawLine(x, 0, x, height, gridPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation(); // 停止更新
    }

    public void startAnimation() {
        // 启动动画或定时器任务
        handler.post(runnable);
    }

    public void stopAnimation() {
        // 停止动画或定时器任务
        handler.removeCallbacksAndMessages(null);
    }
}
