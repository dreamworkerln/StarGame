package ru.geekbrains;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Timer;
import java.util.TimerTask;

public class StarGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;
    TextureRegion region0, region1;
    BitmapFont font;
    Sound sound;
    Music music;

    @Override
    public void create () {
        batch = new SpriteBatch();
        img = new Texture("doom_asshole.png");
        region0 = new TextureRegion(img, 0, 0, img.getWidth() - 50, img.getHeight());
        region1 = new TextureRegion(img, 50, 50, 150, 150);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);
        font.getData().setScale(6.5f, 6.5f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        //sound = Gdx.audio.newSound(Gdx.files.internal("audio/full.ogg"));
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/full.ogg"));
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                music.play();
            }
        }, 1000, 10000);
    }

    @Override
    public void render () {


        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.setColor(0.6f, 0.6f, 0.6f, 1f);

        //batch.draw(img, 100, 100, 1280, 1024);
        batch.draw(region0, 100, 100, 0, 0, 900, 700, 1f, 1f, 0f);

        batch.setColor(1.f, 1.f, 1.f, 1f);
        batch.draw(region1, 100, 100, 0, 0, 300, 250, 1f, 1f, 0f);
        //batch.draw(region, 10, 10);
        batch.setColor(1f, 1f, 1f, 0.6f);

        font.draw(batch, "Fuck yourself asshole", 100, 1400);
        font.draw(batch, "Get off scum", 100, 1280);
        font.draw(batch, "Eat shit hell spawn", 100, 1160);
        batch.end();
    }

    @Override
    public void dispose () {
        batch.dispose();
        img.dispose();
    }
}
