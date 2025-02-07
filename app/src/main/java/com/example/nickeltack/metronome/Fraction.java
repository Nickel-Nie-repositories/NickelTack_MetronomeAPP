package com.example.nickeltack.metronome;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Fraction implements Comparable<Fraction>, Serializable {
    private int numerator;  // 分子
    private int denominator;  // 分母

    // 默认分数尺，用于小数转近似分数
    private static final FractionRuler fractionRuler = new FractionRuler(){
        {
            // 分音：
            addFraction(new Fraction("1/64"));
            addFraction(new Fraction("1/32"));
            addFraction(new Fraction("1/16"));
            addFraction(new Fraction("1/8"));
            addFraction(new Fraction("1/4"));
            addFraction(new Fraction("1/2"));
            // 整音：
            this.addFraction(new Fraction(1));
            this.addFraction(new Fraction(2));
            this.addFraction(new Fraction(3));
            this.addFraction(new Fraction(4));
            this.addFraction(new Fraction(5));
            this.addFraction(new Fraction(6));
            this.addFraction(new Fraction(8));
            this.addFraction(new Fraction(9));
            this.addFraction(new Fraction(10));
            this.addFraction(new Fraction(12));
            // 附点音：
            this.addFraction(new Fraction("3/16"));
            this.addFraction(new Fraction("3/8"));
            this.addFraction(new Fraction("3/4"));
            this.addFraction(new Fraction("3/2"));
            // 三连音：
            this.addFraction(new Fraction("1/3"));
            this.addFraction(new Fraction("2/3"));
            // 五连音：
//            this.addFraction(new Fraction("2/5"));
//            this.addFraction(new Fraction("4/5"));
        }
    };

    // 构造函数：仅接收一个整数，即A/1形式分数
    public Fraction(int number) {
        this.numerator = number;
        this.denominator = 1;
    }
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

    // 构造函数：拷贝构造
    public Fraction(Fraction fraction) {
        this.numerator = fraction.getNumerator();
        this.denominator = fraction.getDenominator();
    }

    // 构造函数：接收一个浮点数，生成一个近似的分数
    public Fraction(double value) {
        //Log.d("TAG_0","获得小数：" + value);
        Fraction fraction = Fraction.fractionRuler.closestFraction(value);
        //Log.d("TAG_0","生成分数：" + fraction.toString());
        this.numerator = fraction.getNumerator();
        this.denominator = fraction.getDenominator();
    }
    public Fraction(float value) {
        Fraction fraction = Fraction.fractionRuler.closestFraction(value);
        this.numerator = fraction.getNumerator();
        this.denominator = fraction.getDenominator();
    }

    // 返回分数的字符串表示，例如 "3/4"
    @NonNull
    @Override
    public String toString() {
        if(denominator == 1)
        {
            return String.valueOf(numerator);
        }
        return numerator + "/" + denominator;
    }

    // 返回分数的小数表示，例如 3/4 => 0.75
    public float toFloat() {
        return (float) numerator / denominator;
    }
    public double toDouble() {
        return (double) numerator / denominator;
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

    // 获取简化后的分数
    public Fraction reduce() {
        int gcd = gcd(numerator, denominator);  // 求最大公约数
        return new Fraction(numerator / gcd, denominator / gcd);
    }

    // 计算最大公约数
    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = a % b;
            a = b;
            b = temp;
        }
        return a;
    }


    @Override
    public int compareTo(Fraction other) {
        // cross multiplication to avoid floating point precision issues
        //return Integer.compare(this.numerator * other.denominator, this.denominator * other.numerator);
        return Double.compare(this.toDouble(),other.toDouble());
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fraction other = (Fraction) obj;
        return this.numerator * other.denominator == this.denominator * other.numerator;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numerator) * 31 + Integer.hashCode(denominator);
    }





}