package com.example.nickeltack.metronome;

import android.content.Context;
import android.util.Log;

import com.example.nickeltack.R;

import java.lang.reflect.Field;

public class ResourceUtils {
    // 通过资源ID获取资源名称
    public static String getResourceName(Context context, int resourceId) {
        try {
            // 获取 R.raw 类
            Class<?> rawClass = R.raw.class;

            // 获取 R.raw 类中的所有字段
            Field[] fields = rawClass.getFields();
            for (Field field : fields) {
                // 如果资源ID与当前字段的ID匹配
                if (field.getInt(null) == resourceId) {
                    // 返回资源名称（字段名）
                    return field.getName();
                }
            }
        } catch (Exception e) {
            Log.e("ResourceUtils", "Failed to get resource name", e);
        }
        return null;
    }
}
