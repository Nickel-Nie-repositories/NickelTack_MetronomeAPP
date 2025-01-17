package com.example.nickeltack.funclist;

public enum PanelType {
    // 定义面板种类
    COMMON_METRONOME_PANEL("常规节拍器"),
    PROFILE_PANEL("个人资料面板"),
    DASHBOARD_PANEL("仪表盘面板"),
    HELP_PANEL("帮助面板"),
    NOTIFICATION_PANEL("通知面板");

    private final String description;

    // 构造函数，用于初始化每个枚举项的描述
    PanelType(String description) {
        this.description = description;
    }

    // 获取面板的描述
    public String getDescription() {
        return description;
    }
}
