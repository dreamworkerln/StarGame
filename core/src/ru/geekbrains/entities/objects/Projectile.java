package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public abstract class Projectile extends GameObject {

    protected int TTL = 10000;

    protected long start;

    public Projectile(float height, GameObject owner) {
        super(height, owner);

        type.add(ObjectType.PROJECTILE);

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

        ShapeRenderer shape =renderer.shape;



        shape.begin();
        shape.setColor(Color.WHITE);
        Gdx.gl.glLineWidth(1);

        if (type.contains(ObjectType.BULLET)) {

            Gdx.gl.glLineWidth(2);

            shape.set(ShapeRenderer.ShapeType.Line);
            if (radius > 1) {
                tmp0.set(dir).setLength(radius * 2).add(pos);
                shape.line(pos, tmp0);
            }
            else {
                shape.point(pos.x, pos.y, 0);
            }

        }
        else {
            Gdx.gl.glLineWidth(1);
            shape.set(ShapeRenderer.ShapeType.Filled);
            shape.circle(pos.x,pos.y,radius);
        }



        shape.end();

    }

}
