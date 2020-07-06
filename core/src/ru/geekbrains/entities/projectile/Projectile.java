package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.SmokeTrail;
import ru.geekbrains.entities.particles.SmokeTrailList;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public abstract class Projectile extends GameObject implements SmokeTrailList {

    protected List<SmokeTrail> smokeTrailList = new ArrayList<>();

    //protected boolean trail = false;

    public Projectile(float height, GameObject owner) {
        super(owner, height);

        type.add(ObjectType.PROJECTILE);
        TTL = 10000;

        postConstruct();
    }

    public Projectile(float height, Color color, GameObject owner) {
        this(height, owner);
        this.color = color;
    }

    public Projectile(float height, float trailRadius, GameObject owner) {
        this(height, owner);

        addTrace(trailRadius, new Color(0.5f, 0.2f, 0.7f, 1));
    }


    public Projectile(float height, float trailRadius, Color traceColor, GameObject owner) {
        this(height, owner);

        addTrace(trailRadius, traceColor);
    }



    protected abstract void postConstruct();



    private void addTrace(float trailRadius, Color traceColor) {

        SmokeTrail smoke = new SmokeTrail(trailRadius, traceColor, this);

        //SmokeTrail smoke = new SmokeTrail(1, new Color(0.7f, 0.2f, 0.5f, 1), this);
        smoke.pos.set(pos);
        smoke.vel.set(vel);
        smoke.speed = 0;
        smoke.setTTL(25);
        smoke.isStatic = true;
        smokeTrailList.add(smoke);

    }


    @Override
    public void update(float dt) {
        super.update(dt);

        if (age >= TTL) {
            readyToDispose = true;
        }


        for (SmokeTrail trail : smokeTrailList) {


            tmp1.set(vel).nor().scl(-5f);
            tmp2.set(pos).add(tmp1);
            trail.setTrailPos(tmp2);


            //trail.setTrailPos(pos);

            trail.add(1);
            trail.update(dt);
        }

    }

    @Override
    public void draw(Renderer renderer) {

        super.draw(renderer);

        if (renderer.rendererType != RendererType.SHAPE) {
            return;
        }

        ShapeRenderer shape = renderer.shape;

        Gdx.gl.glLineWidth(1);

        shape.setColor(color);

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
        else if (type.contains(ObjectType.SHELL) ||
                type.contains(ObjectType.FRAG)) {
            Gdx.gl.glLineWidth(1);
            shape.set(ShapeRenderer.ShapeType.Filled);
            shape.circle(pos.x,pos.y,radius);
        }

        for (SmokeTrail trail : smokeTrailList) {
            trail.draw(renderer);
        }

    }


    @Override
    public List<SmokeTrail> removeSmokeTrailList() {
        return smokeTrailList;
    }

    @Override
    public void stop() {

        for (SmokeTrail trail : smokeTrailList) {
            trail.stop();
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
