package com.example.nickeltack.starting;

public class CalculateUtils {

    public static long[] calculateDifferences(long[] input) {
        if (input == null || input.length < 2) {
            // 如果输入数组为空或长度小于2，则无法计算差值
            return new long[0];
        }

        // 创建一个长度为 input.length - 1 的新数组用于存储差值
        long[] differences = new long[input.length - 1];

        // 计算相邻元素之间的差值
        for (int i = 0; i < input.length - 1; i++) {
            differences[i] = input[i + 1] - input[i];
        }

        return differences;
    }

    public static long calculateHarmonicMean(long[] input) {
        if (input == null || input.length == 0) {
            // 如果数组为空或长度为零，返回0或做其他适当处理
            throw new IllegalArgumentException("Array cannot be empty");
        }

        double sumOfReciprocals = 0;
        for (long value : input) {
            sumOfReciprocals += 1.0 / value;
        }

        // 返回调和平均数，取整为long类型
        return Math.round(input.length / sumOfReciprocals);
    }

    public static long firstValueFirstAverage(long[] input) {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }

        long weightedSum = 0;
        long totalWeight = 0;

        for (int i = 0; i < input.length; i++) {
            // 如果是第一个元素，权重为2，其他元素权重为1
            long weight = (i == 0) ? 2 : 1;
            weightedSum += weight * input[i];
            totalWeight += weight;
        }

        // 返回加权平均数，向下取整为long类型
        return weightedSum / totalWeight;
    }

    public static long lastValueFirstAverage(long[] input) {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }

        long weightedSum = 0;
        long totalWeight = 0;

        for (int i = 0; i < input.length; i++) {
            // 如果是第一个元素，权重为2，其他元素权重为1
            long weight = (i == input.length-1) ? 2 : 1;
            weightedSum += weight * input[i];
            totalWeight += weight;
        }

        // 返回加权平均数，向下取整为long类型
        return weightedSum / totalWeight;
    }
}
