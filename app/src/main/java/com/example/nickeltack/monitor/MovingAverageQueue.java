package com.example.nickeltack.monitor;

import java.util.LinkedList;
import java.util.Queue;

// 快速求和求平均的循环队列，用于
public class MovingAverageQueue <T extends Number> {
    private final Queue<T> window;
    private final int size;
    private double sum;

    public MovingAverageQueue(int size) {
        this.size = size;
        this.window = new LinkedList<>();
        this.sum = 0.0;
    }

    public double addData(T data) {
        // 如果队列已经有n个数据了，移除最旧的数据
        if (window.size() == size) {
            T oldest = window.poll(); // 移除最旧的数据
            sum -= oldest.doubleValue(); // 从总和中减去最旧的数据
        }

        // 添加新数据到队列
        window.offer(data);
        sum += data.doubleValue(); // 更新总和

        // 返回当前窗口的平均值
        return sum / window.size();
    }

    public double getAverage() {
        return sum / window.size();
    }

    public double getSum()
    {
        return sum;
    }

}
