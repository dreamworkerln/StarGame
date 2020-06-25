package ru.geekbrains.utils;

import com.badlogic.gdx.audio.Sound;

import java.util.Objects;

public class SoundPlay {
    public Sound sound;
    public float durationMilli;
    public long durationTick;

    public SoundPlay(Sound sound, float durationMilli) {
        this.sound = sound;
        this.durationMilli = durationMilli;

        durationTick = (long) (durationMilli /16.666666667);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoundPlay soundPlay = (SoundPlay) o;
        return sound.equals(soundPlay.sound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sound);
    }
}
