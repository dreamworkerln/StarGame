package ru.geekbrains.entities.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public class Explosion extends ParticleObject {

    private float maxRadius;
    private long start;
    long frame;

    private boolean readyToDisposeSelf = false;

    private List<SmokeTrail> smokeTrailList = null;


    public static float calculateNewRadius(GameObject owner) {

        float newRadius = owner.getRadius() * 4;
//        if (owner.type.contains(ObjectType.MISSILE)) {
//            newRadius *= 10;
//        }

        return newRadius;
    }

    public Explosion (GameObject owner) {

        super(Explosion.calculateNewRadius(owner), owner);

        this.mass = owner.getMass();
        this.pos = owner.pos.cpy();
        this.vel = owner.vel.cpy();
        this.start = GameScreen.INSTANCE.getTick();
        this.maxRadius = this.radius;

        // move SmokeTrail fom owner to this
        if (owner.type.contains(ObjectType.DRIVEN_OBJECT)) {
            this.smokeTrailList = ((DrivenObject)owner).getSmokeTrailList();
        }
    }

    public void update(float dt) {

        super.update(dt);

        frame = GameScreen.INSTANCE.getTick() - start;

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

        if (smokeTrailList != null) {

            for (SmokeTrail st : smokeTrailList) {
                st.update(dt);

                readyToDispose = readyToDisposeSelf && st.readyToDispose;
            }

        }
        else {
            readyToDispose = readyToDisposeSelf;
        }
    }

    @Override
    public void draw(Renderer renderer) {

        ShapeRenderer shape = renderer.shape;


        // render smokeTrail if have one
        if (smokeTrailList != null) {
            for (SmokeTrail st : smokeTrailList) {
                st.draw(renderer);
            }
        }

        shape.begin();
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.set(ShapeRenderer.ShapeType.Filled);


        if (frame < 10) {
            shape.setColor(1f, 1f, 0.2f, 1);
        }
        else if (frame < 15) {
            shape.setColor(1f, 1f, 0.2f, 1);
        }
        else {
            shape.setColor(1f, 1f, 0.2f, 1 - (frame - 15) / 15f);
        }

        shape.circle(pos.x, pos.y, radius);

        Gdx.gl.glLineWidth(1);
        shape.end();
    }

    @Override
    public void dispose() {

        if (smokeTrailList != null) {
            for (SmokeTrail st : smokeTrailList) {
                st.dispose();
            }
        }
    }
}
