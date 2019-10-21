package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.SmokeTrail;
import ru.geekbrains.screen.GameScreen;

public class FragMissile extends Missile{

    private final int fragCount;
    protected boolean shapedExplosion = true;

    public FragMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        //mass = 0.03f;

        fuel = 10;

        damage = 0.5f;
        setMaxHealth(0.05f);
        boost = 700;

        maxThrottle = 15f;
        throttle = maxThrottle;

        fragCount = 500;

        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = false;
        selfdOnProximityMiss = false;


        proximityMinDistance = 70;
        proximitySafeDistance = 150;
        proximityMissTargetDistance = 100;

        type.add(ObjectType.FRAGMISSILE);

        maxRotationSpeed = 0.1f;



        this.engineTrail.color = new Color(0.7f, 0.2f, 0.2f, 1);
    }


    @Override
    protected void guide(float dt) {

        // стандартное наведение
        super.guide(dt);

        
        if (target == null || target.readyToDispose) {

            // отворачиваемся от носителяб чтоб не подоравть его случайно
            if(owner!=null && !owner.readyToDispose) {
                guideVector.set(owner.pos).sub(pos).scl(-1).nor().rotate(45);
            }

            return;
        }

        // разворот в сторону цели
        if (distToTarget <  proximityMinDistance*2.5  &&
                distToTarget > proximityMinDistance) {
            
            guideVector.set(target.pos).sub(pos).nor();
        }
        // взвод направленного подрыва
        // находимся от носителя дальше безопасного расстояния
        else if (distToTarget < proximityMinDistance &&
                 distToCarrier > proximitySafeDistance) {

            //shapedExplosion = true;
            //readyToDispose = true;
        }

    }



    @Override
    public void dispose() {

        

        float power = 20f;

        Fragment trash = new Fragment(4f, owner);
        trash.setMass(fragCount*trash.getMass());
        trash.pos.set(pos);
        trash.vel.set(vel);
        trash.dir.set(dir);
        trash.damage = 1;
        SmokeTrail smoke = new SmokeTrail(1, new Color(0.5f, 0.2f, 0.7f, 1), this);
        smoke.speed = 0;
        smoke.TTL = 100;
        trash.smokeTrailList.add(smoke);



        GameScreen.addObject(trash);

        // create fragments
        for (int i = 0; i < fragCount; i++) {


            Projectile frag = new Fragment(2f, owner);

            //frag.setTTL(200);

            frag.pos.set(pos);
            frag.vel.set(vel);
            frag.dir.set(dir);


            double fromAn;
            double toAn;


            if (shapedExplosion) {
                fromAn = Math.PI / 4;
                toAn = Math.PI / 4;
            } else {
                fromAn = 0;
                toAn = 2 * Math.PI;
            }

            float fi_min = (float) (dir.angleRad() - fromAn);
            float fi_max = (float) (dir.angleRad() + toAn);


            float r = (float) ThreadLocalRandom.current().nextDouble(power - power*0.1f, power);
            //float r = power;
            float fi = (float) ThreadLocalRandom.current().nextDouble(fi_min, fi_max);

            float x = (float) (r * Math.cos(fi));
            float y = (float) (r * Math.sin(fi));

            tmp0.set(x, y); // force
            frag.applyForce(tmp0);          // apply force applied to frag
            trash.applyForce(tmp0.scl(-1));

            frag.setTTL(ThreadLocalRandom.current().nextLong(400,600));
            GameScreen.addObject(frag);
        }

        super.dispose();
    }




}
