package ru.geekbrains.utils;

import com.badlogic.gdx.audio.Sound;

import java.util.Objects;

public class SoundPlay {
    public Sound sound;
    public float durationMilli;
    public long durationTick;
    public SoundType type;

    public SoundPlay(Sound sound, float durationMilli, SoundType type) {
        this.sound = sound;
        this.durationMilli = durationMilli;
        this.type = type;
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

    public enum SoundType {

        HEALTH_HALF(1),
        HEALTH_LOW(2),
        HEALTH_DEAD(3);

        private int status;

        SoundType(int status) {
            this.status = status;
        }
    }
}
