package com.example.nickeltack.funclist;

import androidx.annotation.NonNull;

import com.example.nickeltack.R;

import java.util.HashMap;
import java.util.Map;

public enum PanelType {
    // 定义面板种类
    NONE("无面板"),
    COMMON_METRONOME_PANEL("常规节拍器"),
    COMPLEX_METRONOME_PANEL("复杂节拍器"),
    DASHBOARD_PANEL("仪表盘面板"),
    HELP_PANEL("帮助面板"),
    NOTIFICATION_PANEL("通知面板");

    @NonNull
    private static final Map<PanelType, Integer> panelIcons = new HashMap<>() {{
        put(PanelType.COMMON_METRONOME_PANEL, R.drawable.button_metronome1);
        put(PanelType.COMPLEX_METRONOME_PANEL, R.drawable.button_metronome2);
    }};

    private final String description;

    // 构造函数，用于初始化每个枚举项的描述
    PanelType(String description) {
        this.description = description;
    }

    // 获取面板的描述
    public String getDescription() {
        return description;
    }

    public static int getIconResource(PanelType panelType)
    {
        return panelIcons.getOrDefault(panelType, 0);
    }


}
