package ru.geekbrains.entities.objects;

import ru.geekbrains.screen.Renderer;

public class DummyObject extends GameObject {


    /**
     * Constructor without sprite - using ShapeRenderer to draw particles
     *
     * @param radius
     */
    public DummyObject(float radius) {
        super(radius);
    }

    @Override
    public void draw(Renderer renderer) {
        // not used
    }

    @Override
    public void dispose() {
        // not used
    }
}
