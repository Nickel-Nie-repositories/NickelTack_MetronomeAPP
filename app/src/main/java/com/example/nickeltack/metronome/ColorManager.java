package com.example.nickeltack.metronome;

public class ColorManager {

    static int[] colorArray = {
            //0xFFFFFFFF,
            0xFFDD4B39, // 红色
            0xFF4B9ED7, // 蓝色
            0xFF43B600, // 绿色
            0xFFFBB313, // 黄色
            0xFFFF8A00, // 橙色
            0xFF9C27B0, // 紫色
            0xFF8D6E63, // 棕色
            0xFF00BCD4, // 青色
            0xFF009688, // 深绿色
            0xFF795548, // 深棕色
            0xFF607D8B, // 灰蓝色
            0xFF03A9F4, // 浅蓝色
            0xFF673AB7, // 深紫色
            0xFFCDDC39, // 亮绿色
            0xFFFFC107  // 亮黄色
    };
    static int colorArrayLength = colorArray.length;


    public static int[] errorLevelColors = {
            0xFF000066, // 深蓝
            0xFF0000CC, // 中蓝
            0xFF0000FF, // 浅蓝
            0xFF000000, // 黑色
            0xFFFF6666, // 浅红
            0xFFFF3333, // 中红
            0xFFCC0000  // 深红
    };
    public static int errorLevelPivot = 3;

}
