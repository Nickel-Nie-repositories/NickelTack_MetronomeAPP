package com.example.nickeltack.metronome;

public class Fraction {
    private int numerator;  // 分子
    private int denominator;  // 分母

    // 构造函数：接收分子和分母
    public Fraction(int numerator, int denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("Denominator cannot be zero.");
        }
        this.numerator = numerator;
        this.denominator = denominator;
    }

    // 构造函数：接收一个分数字符串，例如 "3/4"
    public Fraction(String fractionStr) {
        String[] parts = fractionStr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid fraction string format.");
        }
        try {
            this.numerator = Integer.parseInt(parts[0]);
            this.denominator = Integer.parseInt(parts[1]);
            if (this.denominator == 0) {
                throw new IllegalArgumentException("Denominator cannot be zero.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in fraction string.");
        }
    }

    // 返回分数的字符串表示，例如 "3/4"
    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }

    // 返回分数的小数表示，例如 3/4 => 0.75
    public float toFloat() {
        return (float) numerator / denominator;
    }

    // 获取分子
    public int getNumerator() {
        return numerator;
    }

    // 获取分母
    public int getDenominator() {
        return denominator;
    }

    // 设置分子
    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    // 设置分母
    public void setDenominator(int denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("Denominator cannot be zero.");
        }
        this.denominator = denominator;
    }
}