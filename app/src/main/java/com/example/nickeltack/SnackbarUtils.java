package com.example.nickeltack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {

    // 静态方法：显示半透明 Snackbar 提示信息
    public static void showTemporarySnackbar(View view, String message) {
        // 创建 Snackbar 实例
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);

        // 设置半透明黑色背景
        snackbar.getView().setBackgroundColor(Color.argb(128, 0, 0, 0));  // 50% 透明黑色

        // 显示 Snackbar
        snackbar.show();
    }

    // 静态方法：直接通过 Context 获取根视图来显示 Snackbar
    public static void showTemporarySnackbar(Context context, String message) {
        // 获取根视图
        View rootView = ((Activity) context).findViewById(android.R.id.content);

        // 调用前一个方法来显示 Snackbar
        showTemporarySnackbar(rootView, message);
    }
}