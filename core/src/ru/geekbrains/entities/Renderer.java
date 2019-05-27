package ru.geekbrains.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Renderer {

    public SpriteBatch batch;
    public ShapeRenderer shape;

    public Renderer(SpriteBatch batch, ShapeRenderer shape) {
        this.batch = batch;
        this.shape = shape;
    }

    public void dispose() {

        batch.dispose();
        shape.dispose();
    }

    public void begin() {

        //batch.begin();
        //shape.begin();
    }

    public void end() {
        //batch.end();
        //shape.end();
    }
}
