package ru.geekbrains.entities.projectile.missile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;

public class Missile extends AbstractMissile {


    public Missile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.BASIC_MISSILE);
        type.add(ObjectType.HE_MISSILE);

        mass = 0.04f;
        fuel = 30;
        explosionRadius = radius * 3;
        setMaxThrottle(4f);
        setMaxHealth(0.02f);
        maxRotationSpeed =  0.05f;
        damage = 5f;
        penetration = 1;
    }


/*
    private static class AimFunction implements UnivariateFunction {

        public double rx, ry, vx, vy, ax, ay, ACC;


        public AimFunction() {

        }

        public double value(double t) {

            double result = 4*Math.pow(rx,2) + 4*Math.pow(ry,2) + (-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(t,4) + 4*Math.pow(t,3)*(ax*vx + ay*vy) +
                    8*t*(rx*vx + ry*vy) + 4*Math.pow(t,2)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2));

            return result;
        }
    }
    */

/*

    public void selfGuiding(float dt) {

        // Система наведения пушек и ракет(самонаведение)
        // https://gamedev.stackexchange.com/questions/149327/projectile-aim-prediction-with-acceleration
        // Здесь вместо начальной скорости используется вектор ускорения = const


        if (target== null || target.readyToDispose)
            return;

        af.ACC = maxThrottle / mass;  // Максимальное возможное ускорение объекта




        // t - target
        // s - object


        //at      -> a
        //rt - rs -> r
        //vt - vs -> v



        // r =  rt - rs
        af.rx = target.pos.x - pos.x;
        af.ry = target.pos.y - pos.y;

        // v =  vt - vs
        af.vx = target.vel.x - vel.x;
        af.vy = target.vel.y - vel.y;

        // apply inverted object acceleration to target
        af.ax = target.acc.x - acc.x;
        af.ay = target.acc.y - acc.y;

//        try {
//            Thread.sleep(5);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        for (int i = 0; i< 100; i++) {
            try {

                // Корней нет - функция не пересекает ось Ox
                if (af.value(0) > 0 && af.value(dt * i*10) > 0 ||
                        af.value(0) < 0 && af.value(dt * i*10) < 0) {

                    continue;
                }

                // Для ракет сделаем погрешность в вычислениях побольше
                double t = nonBracketing.solve(100, af,  0, dt * i*10);

                if (!Double.isNaN(t) && !Double.isInfinite(t) && t > 0) {

                    double vs_x = af.rx / t + 0.5 * af.ax * t + af.vx;
                    double vs_y = af.ry / t + 0.5 * af.ay * t + af.vy;

                    guideVector.set((float) vs_x, (float) vs_y).nor();
                    break;
                }
            }
            catch (Exception ignore) {}

        }
    }
    */


    /*
    @Override
    public void dispose() {

        float explosionPower = 5f;

        float fragCount = 10;


        if (type.contains(ObjectType.ANTIMISSILE)) {
            fragCount = 3;
        }

        // create fragments
        for (int i = 0; i < fragCount; i++) {


            Bullet bl = new Bullet(1, owner);

            //frag.setTTL(200);

            bl.pos.set(pos);
            bl.vel.set(vel);
            bl.dir.set(dir);
            bl.owner = owner;

            double fromAn;
            double toAn;

            fromAn = 0;
            toAn = 2 * Math.PI;


            float fi_min = (float) (dir.angleRad() - fromAn);
            float fi_max = (float) (dir.angleRad() + toAn);

            float r = (float) ThreadLocalRandom.current().nextDouble(0, explosionPower);
            float fi;

            try {

                fi = (float) ThreadLocalRandom.current().nextDouble(fi_min, fi_max);
            }
            catch(Exception e) {
                System.out.println(dir);
                System.out.println(fi_min);
                System.out.println(fi_max);
                System.out.println(e);
                // гениально
                throw e;
            }


            float x = (float) (r * Math.cos(fi));
            float y = (float) (r * Math.sin(fi));

            tmp0.set(x, y); // force
            bl.applyForce(tmp0);          // apply force applied to frag
            //bl.applyForce(tmp0.scl(-1));

            bl.setTTL(ThreadLocalRandom.current().nextLong(400,600));
            GameScreen.addObject(bl);
        }
        super.dispose();
    }
    */
}
