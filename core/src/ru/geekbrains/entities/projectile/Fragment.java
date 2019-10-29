package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.SmokeTrail;
import ru.geekbrains.entities.particles.SmokeTrailList;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public class Fragment extends Projectile implements SmokeTrailList {

    public List<SmokeTrail> smokeTrailList = new ArrayList<>();


    public Fragment(float height, GameObject owner) {
        super(height, owner);

        type.add(ObjectType.FRAG);

        mass = 0.002f;

        setMaxHealth(0.002f);
        //damage = 0.15f;
        damage = 0.015f;
        //TTL = 100;

        //damage = 1;


        //SmokeTrail smoke = new SmokeTrail(1, new Color(0.5f,0.5f,0.5f,1f), this);

        //SmokeTrail smoke = new SmokeTrail(1, new Color(0.3f,0.2f,0.3f,1f), this);

        //smoke.speed = 0;
        //smoke.TTL = 50;
        //smokeTrailList.add(smoke);
    }

    public Fragment(float height, boolean trail, GameObject owner) {
        this(height, owner);

        SmokeTrail smoke = new SmokeTrail(1, new Color(0.5f, 0.2f, 0.7f, 1), this);
        smoke.pos.set(pos);
        smoke.vel.set(vel);
        smoke.speed = 0;
        smoke.setTTL(40);
        smokeTrailList.add(smoke);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        //tmp0.set(vel).scl(5* -dt);
        //tmp1.set(pos).add(tmp0);

        for (SmokeTrail trail : smokeTrailList) {
            trail.setTrailPos(pos);
            trail.add(1);
            trail.update(dt);
        }


    }


    @Override
    public void draw(Renderer renderer) {

        super.draw(renderer);

        if (renderer.rendererType!= RendererType.SHAPE) {
            return;
        }


        for (SmokeTrail trail : smokeTrailList) {
            trail.draw(renderer);
        }
    }


    @Override
    public List<SmokeTrail> removeSmokeTrailList() {
        return smokeTrailList;
    }
}
