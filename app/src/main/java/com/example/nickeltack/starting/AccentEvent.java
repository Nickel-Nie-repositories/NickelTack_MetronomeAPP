package com.example.nickeltack.starting;

import java.util.EventObject;
public class AccentEvent extends EventObject {


    private int amplitude;

    private long currentTime;

    public AccentEvent(Object source, int amplitude, long currentTime) {
        super(source);
        this.amplitude = amplitude;
        this.currentTime = currentTime;
    }

    public int getAmplitude()
    {
        return amplitude;
    }
    public long getCurrentTime()
    {
        return currentTime;
    }

}
