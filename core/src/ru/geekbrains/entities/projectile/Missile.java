package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class Missile extends DrivenObject {

    protected BPU pbu = new BPU();


    //AimFunction af;
    protected boolean selfdOnTargetDestroyed;
    protected boolean selfdOnNoFuel;
    protected boolean selfdOnProximityMiss;


    // минимальная дистанция сближения с целью (которая была зарегистрирована в полете)
    protected float minDistance = Float.MAX_VALUE;

    protected float distToCarrier = Float.MAX_VALUE;
    protected float distToTarget = Float.MAX_VALUE;

    // при промахе при удалении от цели
    // до этой величины происходит подрыв
    protected float proximityMissTargetDistance = Float.MAX_VALUE;

    // подрыв не призводится при расстоянии до носителя меньшим, чем это
    // (безопасная дистанция блокировки подрыва)
    protected float proximitySafeDistance = 0;



    // Производится дистанционный подрыв
    // при сокращении дистанции до цели меньше этой величины
    protected float proximityMinDistance = 0;


    public Missile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.MISSILE);


        setRadius(radius * 5); // fix issued by image aspect ratio

        mass = 0.1f;
        //maxRotationSpeed = 0.02f;
        fuel = 8;

        maxThrottle = 10f;
        throttle = maxThrottle;

        setMaxHealth(0.02f);
        damage = 4f;

        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = false;
        selfdOnProximityMiss = false;


        aspectRatio = 1;

//        final double relativeAccuracy = 1.0e-10;
//        final double absoluteAccuracy = 1.0e-8;
//
//        af =  new AimFunction();
//        nonBracketing = new BrentSolver(relativeAccuracy, absoluteAccuracy);


    }

    @Override
    protected void guide(float dt) {

        if (owner != null && !owner.readyToDispose) {
            distToCarrier = tmp0.set(owner.pos).sub(pos).len() - owner.getRadius() - radius;
        }
        else {
            distToCarrier = Float.MAX_VALUE;
        }

        if(target != null && !this.readyToDispose) {
            distToTarget = tmp0.set(target.pos).sub(pos).len() - target.getRadius() - radius;

            if (distToTarget < 0 ) {
                distToTarget = 0;
            }
        }



        if (target != null && target.readyToDispose) {
            target = null;
        }

        // self-d on target destroyed
        if (selfdOnTargetDestroyed &&
            target == null &&
            distToCarrier > proximitySafeDistance) {

                this.readyToDispose = true;

        }

        // self-d on no fuel
        if (fuel <= 0) {

            if (selfdOnNoFuel) {
                this.readyToDispose = true;
            }
        }


        // Self-d on miss target (proximity explosion)
        if (target != null && selfdOnProximityMiss) {

            if (distToTarget < minDistance) {
                minDistance = distToTarget;
            }


            // Дистанция до цели начала расти
            // Находимся от цели на расстоянии, меньшем proximityMissTargetDistance
            // находимся от носителя дальше безопасного расстояния
            if (distToTarget > (minDistance + radius + target.getRadius()) &&
                distToTarget < proximityMissTargetDistance &&
                distToCarrier > proximitySafeDistance) {

                this.readyToDispose = true;
            }
        }


        // explode on min distance to target
        // находимся от носителя дальше безопасного расстояния
        if (distToTarget < proximityMinDistance &&
            distToCarrier > proximitySafeDistance) {

            this.readyToDispose = true;
        }






        //guideVector.setZero();

        //guideVector.setZero();

        if(target != null && !this.readyToDispose) {

            // Максимальное возможное ускорение ракеты своим движком
            float maxAcc = maxThrottle / mass;


            pbu.guideMissile(this, target, maxAcc, dt);
            
            //selfGuiding(dt);

            if (!pbu.guideResult.guideVector.isZero()) {

                //tmp0.set(pbu.guideResult.guideVector.nor());
                guideVector.set(pbu.guideResult.guideVector.nor());
            }
            // Самонаведение не сгидродоминировало, наводимся по прямой
            else {
                // (только для больших ракет)
                if (this.getClass() == Missile.class /*&& age < 10000*/ ) {
                    guideVector.set(target.pos).sub(pos).nor();
                    //System.out.println(this + "   " + age);
                }
            }
        }
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


}
