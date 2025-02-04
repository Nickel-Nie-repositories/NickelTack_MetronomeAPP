package com.example.nickeltack.monitor;

public enum ReferenceSourceModel {
    REFERENCE_VALUE("参考节奏"),
    REAL_TIME("上一拍"),
    GLOBAL_MEAN("全局平均"),
    RECENT_RHYTHM("最近节奏");


    private final String description;

    ReferenceSourceModel(String description)
    {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
