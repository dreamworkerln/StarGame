package ru.geekbrains.entities.particles;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.base.Font;
import ru.geekbrains.screen.RendererType;

public class Message extends GameObject {

    private static Font font;

    static {

        font = new Font("font/font.fnt", "font/font2.png");
        font.setSize(20f);
    }



    public long TTL = 400; // time to live (in ticks)

    long expired;

    String text;

    public Message(String text) {
        super();

        //super(1, null);

        this.expired = GameScreen.INSTANCE.getTick() + TTL;
        this.text = text;

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
                GameScreen.INSTANCE.worldBounds.getLeft() * GameScreen.INSTANCE.aspect + 5,
                GameScreen.INSTANCE.worldBounds.getTop()-5);
        //renderer.batch.end();
    }

}
