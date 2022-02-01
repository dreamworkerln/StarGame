package ru.dreamworkerln.stargame.entities.particles;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.screen.GameScreen;
import ru.dreamworkerln.stargame.screen.Renderer;
import ru.dreamworkerln.stargame.screen.Font;
import ru.dreamworkerln.stargame.screen.RendererType;

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

        float rightBound = GameScreen.INSTANCE.worldBounds.getRight() * GameScreen.INSTANCE.aspect;

        if (ps ==1) {
            this.expired = Long.MAX_VALUE;
            pos.set(rightBound *2 - 150 , 0);
        }
        else if (ps ==2) {
            this.expired = Long.MAX_VALUE;
            pos.set(rightBound  * 2- 300 , 0);
        }
        else if (ps == -1) {
            pos.set(0, 40);
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
