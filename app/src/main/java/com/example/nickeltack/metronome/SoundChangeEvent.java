package com.example.nickeltack.metronome;

import java.util.EventObject;

public class SoundChangeEvent extends EventObject {

    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public SoundChangeEvent(Object source) {
        super(source);
    }
}
