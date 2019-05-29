package ru.geekbrains.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.storage.Game;

public class Explosion extends ParticleObject {

    private Vector2 pos;
    private float maxRadius;
    private float radius;
    private long start;

    private boolean readyToDisposeSelf = false;

    private SmokeTrail smokeTrail = null;

    public Explosion (Vector2 pos, float radius) {

        super(radius);
        this.pos = pos.cpy();
        this.start = Game.INSTANCE.getTick();
        this.maxRadius = radius;
    }

    public void addSmokeTrail(SmokeTrail smokeTrail) {

        this.smokeTrail = smokeTrail;
    }


    public void update(float dt) {

        long frame = (int)(Game.INSTANCE.getTick() - start);

        if(frame >= 0 && frame < 5) {
            radius =  maxRadius * 0.1f;
        }
        else if(frame >= 5 && frame < 10) {
            radius =  maxRadius * 0.5f;
        }
        else if(frame >= 10 && frame < 15) {
            radius =  maxRadius * 1f;
        }
        else if(frame >= 15 && frame < 30) {
            radius =  maxRadius  - maxRadius * ((frame - 15)/15f);
        }
        else {
            radius = 0;
            readyToDisposeSelf = true;
        }

        // ---------------------------------------------------------

        if (smokeTrail != null) {

            smokeTrail.update(dt);
            readyToDispose = readyToDisposeSelf && smokeTrail.readyToDispose;
        }
        else {
            readyToDispose = readyToDisposeSelf;
        }
    }

    @Override
    public void draw(Renderer renderer) {

        ShapeRenderer shape = renderer.shape;


        // render smokeTrail if have one
        if (smokeTrail != null) {
            smokeTrail.draw(renderer);
        }

        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin();
        shape.set(ShapeRenderer.ShapeType.Filled);

        shape.setColor(1f, 1f, 0.2f, 1);
        shape.circle(pos.x, pos.y, radius);

        Gdx.gl.glLineWidth(1);
        shape.end();



    }

    @Override
    public void dispose() {
        smokeTrail.dispose();
    }
}
