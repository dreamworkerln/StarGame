package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.storage.Game;

public class Shell extends GameObject {

    private int TTL = 10000;

    private long start;


    public Shell(float radius) {
        super(radius);

        mass = 0.02f;
        start = GameScreen.INSTANCE.getTick();
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        long frame = GameScreen.INSTANCE.getTick() - start;

        if (frame >= TTL) {
            readyToDispose = true;
        }

    }

    @Override
    public void draw(Renderer renderer) {

        tmp0.set(dir).setLength(radius).add(pos);

        renderer.shape.begin();
        Gdx.gl.glLineWidth(5);
        renderer.shape.set(ShapeRenderer.ShapeType.Line);
        renderer.shape.setColor(Color.WHITE);
        renderer.shape.line(pos, tmp0);
        Gdx.gl.glLineWidth(5);
        renderer.shape.end();

    }
}
