package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;

public class FlakShell extends Shell {

    private int fragCount;

    public FlakShell(float height, GameObject owner) {
        super(height, owner);
    }

    public FlakShell(float height, float trailRadius, GameObject owner) {
        super(height, trailRadius, owner);
    }

    public FlakShell(float height, float trailRadius, Color color, GameObject owner) {
        super(height,trailRadius, color, owner);
    }


    @Override
    protected void postConstruct() {

        super.postConstruct();
        type.add(ObjectType.FLAK_SHELL);

        mass = 0.02f;
        //mass = 1;
        explosionRadius = radius * 3;

        setMaxHealth(2.1f);
        fragCount = 100;

        damage = 0.5f;
        penetration = 0.1f;
    }



    @Override
    public void dispose() {



        float power = 3f;

        // create fragments
        for (int i = 0; i < fragCount; i++) {


            Projectile frag = new Fragment(2,  owner);

            //frag.setTTL(200);

            frag.pos.set(pos);
            frag.vel.set(vel);
            frag.dir.set(dir);
            frag.owner = owner;

            double fromAn;
            double toAn;



            fromAn = 0;
            toAn = 2 * Math.PI;


            float fi_min = (float) (dir.angleRad() - fromAn);
            float fi_max = (float) (dir.angleRad() + toAn);




            float r = (float) ThreadLocalRandom.current().nextDouble(power - power*0.2f, power);
            //float r = (float) ThreadLocalRandom.current().nextDouble(0, power);
            //float r = (float) ThreadLocalRandom.current().nextGaussian()*power*1.0f /*+ power*/;
            //float r = power;
            float fi;

            try {

                fi = (float) ThreadLocalRandom.current().nextDouble(fi_min, fi_max);
            }
            catch(Exception e) {
                System.out.println(dir);
                System.out.println(fi_min);
                System.out.println(fi_max);
                System.out.println(e.toString());

                throw e;
            }


            float x = (float) (r * Math.cos(fi));
            float y = (float) (r * Math.sin(fi));

            tmp0.set(x, y); // force
            frag.applyForce(tmp0);          // apply force applied to frag

            frag.setTTL(ThreadLocalRandom.current().nextLong(100,150));
            GameScreen.addObject(frag);
        }

        super.dispose();
    }
}
