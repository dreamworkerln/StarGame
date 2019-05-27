package ru.geekbrains.storage;

public enum Game {

    INSTANCE;

    private long tick = 0;

    /**
     * Get current value
     */
    public long getTick() {

        return tick;
    }

    /**
     * Progress updateTick
     */
    public void updateTick() {

        tick++;
    }

}
