package com.example.nickeltack.metronome;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FractionRuler {
    private List<Fraction> fractions; // 存储分数

    // 构造函数，初始化空的分数列表
    public FractionRuler() {
        this.fractions = new ArrayList<>();
    }

    // 向分数尺添加一个新的分数
    public void addFraction(Fraction fraction) {
        // 判断分数尺中是否已经有相同的分数（按简化后的形式判断）
        fraction = fraction.reduce();
        for (int i = 0; i < fractions.size(); i++) {
            Fraction existing = fractions.get(i);
            if (existing.equals(fraction)) {
                return; // 如果已经存在这个分数，直接返回
            }
        }
        // 将分数添加到合适的位置
        fractions.add(fraction);
        Collections.sort(fractions); // 排序，保持从小到大
    }

    // 将浮点数转换为分数尺上最接近的分数
    public Fraction closestFraction(double value) {
        // 使用二分查找找出最接近的分数
        int index = Collections.binarySearch(fractions, new Fraction(0, 1) {
            @Override
            public double toDouble() {
                return value;
            }
        });

        // 如果没有找到精确的分数，binarySearch会返回负数，表示应该插入的位置
        if (index < 0) {
            index = -index - 1;
        }

        // 检查相邻的分数来找到最接近的
        Fraction closest = null;
        double minDiff = Double.MAX_VALUE;

        if (index > 0) {
            Fraction left = fractions.get(index - 1);
            double diff = Math.abs(left.toDouble() - value);
            if (diff < minDiff) {
                minDiff = diff;
                closest = left;
            }
        }

        if (index < fractions.size()) {
            Fraction right = fractions.get(index);
            double diff = Math.abs(right.toDouble() - value);
            if (diff < minDiff) {
                minDiff = diff;
                closest = right;
            }
        }

        return closest;
    }

    public List<Fraction> getFractions() {
        return fractions;
    }

}