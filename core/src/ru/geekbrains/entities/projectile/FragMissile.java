package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.screen.GameScreen;

public class FragMissile extends Missile{

    protected boolean shapedExplosion = false;

    public FragMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        damage = 0.5f;

        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = true;
        selfdOnProximity = true;

        proximityTargetDistance = 100;
        proximitySafeDistance = 200;
    }


    @Override
    protected void guide(float dt) {
        super.guide(dt);

        if (distToTarget < proximityTargetDistance) {

            dir.set(target.pos).sub(pos).nor();

            shapedExplosion = true;
            readyToDispose = true;
        }

    }



    @Override
    public void dispose() {

        // create fragments

        for (int i = 0; i < 50; i++) {


            Projectile frag = new Fragment(3f, owner);
            float power = 50f;
            
            //frag.setTTL(200);

            frag.pos.set(pos);
            frag.vel.set(vel);
            frag.dir.set(dir);

            double spreadAngle = Math.PI;
            if (shapedExplosion) {
                spreadAngle /=4;
            }

            float fi_min = (float) (dir.angleRad() - spreadAngle);
            float fi_max = (float) (dir.angleRad() + spreadAngle);


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
