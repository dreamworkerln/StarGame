package ru.geekbrains;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StarGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;
    TextureRegion region0, region1;
    Sprite sprite;

    @Override
    public void create () {
        batch = new SpriteBatch();
        img = new Texture("starsky3.jpg");
    }

    @Override
    public void render () {


        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.f, 0.f, 0.f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.setColor(0.9f, 0.9f, 0.9f, 1.f);
        batch.draw(img, 0, 0, w, h);
        batch.end();
    }

    @Override
    public void dispose () {
        batch.dispose();
        img.dispose();
    }
}
