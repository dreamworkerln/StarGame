package ru.geekbrains.entities.particles;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.base.Font;
import ru.geekbrains.screen.RendererType;

public class Message extends GameObject {

    public Font font;


    long TTL = 400; // time to live (in ticks)

    long expired;

    public String text;
    public float down;

    public Message(String text, int ps) {
        super();


        font = new Font("font/font.fnt", "font/font2.png");
        font.setSize(20f);

        //super(1, null);

        this.expired = GameScreen.INSTANCE.getTick() + TTL;
        this.text = text;

        if (ps ==1) {
            this.expired = Long.MAX_VALUE;
            pos.set(GameScreen.INSTANCE.BACKGROUND_SIZE - 100 , 0);
        }
        else if (ps ==2) {
            this.expired = Long.MAX_VALUE;
            pos.set(GameScreen.INSTANCE.BACKGROUND_SIZE - 300 , 0);
        }

        rendererType.add(RendererType.FONT);
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

        if (renderer.rendererType!=RendererType.FONT) {
            return;
        }


        //renderer.batch.begin();
        font.draw(renderer.batch, text,
                GameScreen.INSTANCE.worldBounds.getLeft() * GameScreen.INSTANCE.aspect + 5 + pos.x,
                GameScreen.INSTANCE.worldBounds.getTop() - 5 - pos.y);
        //renderer.batch.end();
    }

    @Override
    public void setTTL(long TTL) {
        super.setTTL(TTL);
        expired = GameScreen.INSTANCE.getTick() + TTL;
    }
}
