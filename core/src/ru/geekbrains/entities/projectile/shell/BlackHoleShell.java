package ru.geekbrains.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.Projectile;
import ru.geekbrains.entities.projectile.frag.EmpFragment;
import ru.geekbrains.entities.projectile.frag.Fragment;
import ru.geekbrains.entities.projectile.frag.PlasmaFragment;
import ru.geekbrains.screen.GameScreen;

public class BlackHoleShell extends Shell {

    float clockwise;

    public BlackHoleShell(float height, GameObject owner) {
        super(height, owner);
    }

    public BlackHoleShell(float height, float trailRadius, GameObject owner) {
        super(height, trailRadius, owner);
    }

    public BlackHoleShell(float height, float trailRadius, Color color, GameObject owner) {
        super(height, trailRadius, color, owner);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();

        type.add(ObjectType.BLACKHOLE_SHELL);
        color = Color.BLACK;
        explosionColor = Color.BLACK;
        explosionRadius = 0;

        TTL = 230;
        damage = 0.1f;
        mass = 1;
        setMaxHealth(Integer.MAX_VALUE);
        clockwise  = (float) (ThreadLocalRandom.current().nextBoolean() ? Math.PI / 2f : Math.PI / -2f);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if(age < 50) {
            return;
        }

        List<GameObject> targets = GameScreen.getCloseObjects(this, 400);



        if (age >= 50 && age < 110) {



            targets.forEach(o -> {

                if (o == this) return;

                tmp1.set(o.pos).sub(pos);
                //tmp2.set(tmp1).nor().scl(o.getMass());
                tmp2.set(tmp1).nor().scl(-100000f * o.getMass() * 1 / tmp1.len());
                o.applyForce(tmp2);

                tmp3.set(o.pos).sub(pos);
                tmp4.set(tmp3).rotateRad(clockwise).nor().scl((float) Math.sqrt(tmp3.len()) * 0.03f);   //tmp4.set(tmp3).rotateRad((float) Math.PI / 2f).nor().scl((float) Math.sqrt(tmp3.len()) * 0.04f);
                o.applyForce(tmp4);


            });
        }

//        else if (age >= 105 && age < 200) {
//
//
//            targets.forEach(o -> {
//
//                if (o == this) return;
//
//                tmp1.set(o.pos).sub(pos);
//                //tmp2.set(tmp1).nor().scl(o.getMass());
//                tmp2.set(tmp1).nor().scl(-100000f * o.getMass() * 1 / tmp1.len());
//                o.applyForce(tmp2);
//
////                tmp3.set(o.pos).sub(pos);
////                tmp4.set(tmp3).rotateRad(clockwise).nor().scl((float) Math.sqrt(tmp3.len()) * 0.03f);   //tmp4.set(tmp3).rotateRad((float) Math.PI / 2f).nor().scl((float) Math.sqrt(tmp3.len()) * 0.04f);
////                o.applyForce(tmp4);
//
//
//            });
//        }

        else if (age >= 110 && age < 200) {



            targets.forEach(o -> {

                if (o == this) return;

                tmp1.set(o.pos).sub(pos);
                //tmp2.set(tmp1).nor().scl(o.getMass());
                tmp2.set(tmp1).nor().scl(-100000f * o.getMass() * 1 / tmp1.len());
                o.applyForce(tmp2);


            });
        }

        else if (age >= 200 && age < 230) {

            // SFX
            if (age == 200) {
                radius = 1;
                Fragment frag =  new Fragment(10,  null);
                frag.color = Color.BLACK;
                frag.pos.set(pos);
                frag.vel.set(vel);
                frag.explosionColor = new Color(0.3f, 0.3f, 0.7f, 0.4f);
                frag.explosionRadius = 60;
                frag.setTTL(1);
                GameScreen.addObject(frag);
            }




            targets.forEach(o -> {

                if (o == this) return;

                tmp1.set(o.pos).sub(pos);
                //tmp2.set(tmp1).nor().scl(o.getMass());
                tmp2.set(tmp1).nor().scl(100000f * o.getMass() * 1 / tmp1.len());
                o.applyForce(tmp2);


            });
        }



//        else if (age > 100) {
//
//            targets.forEach(o -> {
//
//                if (o == this) return;
//
//                tmp3.set(o.pos).sub(pos);
//                tmp4.set(tmp3).rotateRad((float) Math.PI / 2f).nor().scl((float) Math.sqrt(tmp3.len())*0.1f);
//                o.applyForce(tmp4);
//
//
//            });
//        }
    }








    @Override
    public void dispose() {

        super.dispose();
    }
}
