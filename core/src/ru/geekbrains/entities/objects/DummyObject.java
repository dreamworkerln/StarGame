package ru.geekbrains.entities.objects;

import ru.geekbrains.screen.Renderer;

public class DummyObject extends GameObject {


    /**
     * Constructor without sprite - using ShapeRenderer to draw particles
     *
     * @param radius
     */
    public DummyObject(float height, GameObject owner) {
        super(height, owner);
    }


    public DummyObject(GameObject owner) {

        super(owner);
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
