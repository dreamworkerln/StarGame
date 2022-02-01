package ru.dreamworkerln.stargame.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Renderer {

    public SpriteBatch batch;
    public ShapeRenderer shape;
    public RendererType rendererType;

    public Renderer(SpriteBatch batch, ShapeRenderer shape, RendererType rendererType) {
        this.batch = batch;
        this.shape = shape;
        this.rendererType = rendererType;
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
