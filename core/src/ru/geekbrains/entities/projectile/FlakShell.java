package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;

public class FlakShell extends Shell {

    protected int fragCount;
    float explosionPower;
    long fragTTL;
    float fuseMultiplier;
    boolean shapedExplosion;

    public FlakShell(float height, GameObject owner) {
        super(height, owner);
    }

    public FlakShell(float height, float trailRadius, Color color, GameObject owner) {
        super(height,trailRadius, color, owner);
    }

    protected  Projectile createFragment() {

        return  new Fragment(2,  owner);

    }


    @Override
    protected void postConstruct() {

        super.postConstruct();

        type.add(ObjectType.FLAK_SHELL);

        mass = 0.02f;
        //mass = 1;
        explosionRadius = radius * 6;

        setMaxHealth(2.1f);
        fragCount = 100;
        fragTTL = 150;
        fuseMultiplier = 0.5f;
        explosionPower = 3;
        shapedExplosion = true;

        damage = 0.5f;
        penetration = 0.1f;




    }



    @Override
    public void dispose() {



        // create fragments
        for (int i = 0; i < fragCount; i++) {


            Projectile frag = createFragment();

            //frag.setTTL(200);

            frag.pos.set(pos);
            frag.vel.set(vel);
            frag.dir.set(dir);
            frag.owner = owner;

            double fromAn;
            double toAn;


            if (shapedExplosion) {

                fromAn = Math.PI / 3;
                toAn = Math.PI / 3;
            }
            else {

                fromAn = 0;
                toAn = Math.PI * 2;

            }



            //fromAn = 0;
            //toAn = 2 * Math.PI;


            float fi_min = (float) (dir.angleRad() - fromAn);
            float fi_max = (float) (dir.angleRad() + toAn);




            float r = (float) ThreadLocalRandom.current().nextDouble(explosionPower - explosionPower *0.2f, explosionPower);
            //float r = (float) ThreadLocalRandom.current().nextDouble(0, explosionPower);
            //float r = (float) ThreadLocalRandom.current().nextGaussian()*explosionPower*1.0f /*+ explosionPower*/;
            //float r = explosionPower;
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

            frag.setTTL(ThreadLocalRandom.current().nextLong(fragTTL,fragTTL + fragTTL/2));
//            if (frag.isEmpOrdinance) {
//                frag.setTTL(frag.getTTL()/4);
//            }


            GameScreen.addObject(frag);
        }

        super.dispose();
    }
}
