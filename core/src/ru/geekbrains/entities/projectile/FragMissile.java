package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;

public class FragMissile extends Missile{

    protected boolean shapedExplosion = false;

    public FragMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        damage = 0.5f;
        setMaxHealth(0.04f);

        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = false;

        proximityTargetDistance = 150;
        proximitySafeDistance = 150;

        type.add(ObjectType.FRAGMISSILE);



        this.engineTrail.color = new Color(0.7f, 0.2f, 0.2f, 1);
    }


    @Override
    protected void guide(float dt) {

        // стандартное наведение
        super.guide(dt);

        if (target == null || target.readyToDispose) {
            return;
        }

        // разворот в сторону цели
        if (distToTarget <  proximityTargetDistance + proximityTargetDistance * 0.5 &&
            distToTarget >  proximityTargetDistance) {
            guideVector.set(target.pos).sub(pos).nor();
        }
        // подрыв
        else if (distToTarget <  proximityTargetDistance){
            shapedExplosion = true;
            readyToDispose = true;
        }

    }



    @Override
    public void dispose() {

        // create fragments
        for (int i = 0; i < 50; i++) {


            Projectile frag = new Fragment(3f, owner);
            float power = 10f;
            
            //frag.setTTL(200);

            frag.pos.set(pos);
            frag.vel.set(vel);
            frag.dir.set(dir);


            double fromAn;
            double toAn;



            if (shapedExplosion) {
                fromAn = Math.PI /3;
                toAn = Math.PI /3;
            }
            else {
                fromAn = 0;
                toAn =  2*Math.PI;
            }

            float fi_min = (float) (dir.angleRad() - fromAn);
            float fi_max = (float) (dir.angleRad() + toAn);


            float r = (float)ThreadLocalRandom.current().nextDouble(0, power);
            float fi = (float)ThreadLocalRandom.current().nextDouble(fi_min, fi_max);

            float x = (float) (r * Math.cos(fi));
            float y = (float) (r * Math.sin(fi));

            tmp0.set(x, y); // force
            frag.applyForce(tmp0);          // apply force applied to bullet

            GameScreen.addObject(frag);
        }

    }




}
