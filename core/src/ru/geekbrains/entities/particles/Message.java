package ru.geekbrains.entities.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public class Message extends ParticleObject {

    public long TTL = 400; // time to live (in ticks)

    BitmapFont font;
    long expired;
    String text;

    public Message(float height, GameObject owner, String text) {
        super(height, owner);

        this.expired = GameScreen.INSTANCE.getTick() + TTL;
        this.text = text;

        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(height);


    }


    @Override
    public void update(float dt) {
        super.update(dt);

        long tick = GameScreen.INSTANCE.getTick();

        if (tick > expired) {
            readyToDispose = true;
        }
    }


    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);


        renderer.batch.begin();

        font.setColor(Color.RED);
        font.draw(renderer.batch, text, -400, 10);
        renderer.batch.end();
    }



}
