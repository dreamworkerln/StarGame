package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.math.Intersector;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import java.util.List;

import ru.geekbrains.entities.objects.Bullet;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Projectile;
import ru.geekbrains.screen.GameScreen;


public class Minigun extends Gun {


    // Скопировано из DrivenObject, EnemyShip
    // походу нужно множественное наследование из C++

    AimFunction gf;
    UnivariateSolver nonBracketing;



    public float maxRange = 350f;

    public Minigun(float height, GameObject owner) {

        super(height, owner);

        //maxRotationSpeed = 0.1f;
        maxRotationSpeed = 0.1f;
        maxRotationSpeed = 1f;

        radius = 50;
        setCalibre(2);
        fireRate = 1f;
        gunHeatingDelta = 2; // non-stop firing
        coolingGunDelta = 2;
        maxGunHeat = 200;
        //maxGunHeat = 200000000;
        power = 20;


        //final double relativeAccuracy = 1.0e-12;
        //final double absoluteAccuracy = 1.0e-8;

        final double relativeAccuracy = 1.0e-6;
        final double absoluteAccuracy = 1.0e-4;
        gf =  new AimFunction();
        nonBracketing = new BrentSolver(relativeAccuracy, absoluteAccuracy);

    }


    @Override
    public void update(float dt) {


        if (target != null && target.readyToDispose) {
            target = null;
        }

        // --------------------------------------------------
        // getting target

        List<GameObject> targets;



        // Если нет цели или это Ship (перенацеливание на missile)
        if (/*target == null || target.type.contains(ObjectType.SHIP)*/true ) {

            targets = GameScreen.getCloseObjects(owner, maxRange);

            // берем первую  цель (она будет ближе всего)
            // которая не является ни owner ни его снарядами,
            // Если нашли по missile приоритет огня выше

            for (GameObject o : targets) {

                if (o != owner &&
                        o.owner != owner &&
                        !o.readyToDispose && o.type.contains(ObjectType.SHIP)
                ){

                    target = o;
                    break;
                }
            }


            // переключаемся на missile/более близкую missile
            for (GameObject o : targets) {

                if (o != owner &&
                        o.owner != owner &&
                        !o.readyToDispose &&
                        o.type.contains(ObjectType.MISSILE)
                        //(o.type.contains(ObjectType.MISSILE) ||
                        //                o.pos.dst(owner.pos) < target.pos.dst(owner.pos))
                ){
                    target = o;
                    break;
                }
            }

        }




        if (target != null) {


            // Не стрелять по целям, которые не попадут в нас.
            tmp1.set(target.vel).scl(100);
            tmp2.set(owner.vel).scl(100);
            // target and ship velocities intersects in (ship radius) (scaled by 1000)
            float scale = Intersector.intersectRayRay(target.pos, tmp1, owner.pos, tmp2);


            //collinear
            if (Float.isInfinite(scale)) {
                //tmp1.set(owner.vel).sub(target.vel);
                float angle = Math.abs(target.vel.angle(owner.vel));
                if(angle < 90) {
                    tmp3.set(owner.pos); // попал в цель
                }
            }else {

                tmp3.set(tmp1).scl(scale).add(target.pos); // точа пересечения 

//                // оба вектора скорости сонаправлены точки встречи
//                if(Math.abs(target.vel.angle(tmp3)) < 90 &&
//                   Math.abs(owner.vel.angle(tmp3)) < 90) {
//                }
                tmp0.set(owner.pos).sub(target.pos); // vec from target to owner
                tmp4.set(target.vel).sub(owner.vel);

                if(Math.abs(tmp4.angle(tmp0)) <= 120){

                }
                else {
                    // не попал
                    tmp3.set(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
                }



            }
            float len = tmp1.set(tmp3).sub(owner.pos).len();


            // target out of range - reset
            tmp0.set(target.pos).sub(pos);

            if (tmp0.len() > maxRange || // вне радиуса действия minigun
                //scale < 0 ||
                /*len > owner.getRadius() * 100f*/
               len > maxRange) {

                target = null;
            }
        }



        // --------------------------------------------------
        //aiming target




        guideVector.setZero();


        if(target != null) {

            selfGuiding(dt);
            // Если самонаведение не осилиось
            if (guideVector.isZero()) {
                // guideVector.set(target.pos).sub(pos).nor();
            }
        }


        // Auto fire control
        if (target != null &&
                Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {

            startFire();
        }
        else {
            stopFire();
        }

        super.update(dt);
    }



    @Override
    protected void rotateGun() {

        // ToDo: перенести в GameObject.update()
        // rotation dynamics --------------------------------
        // Aiming
        if (!guideVector.isZero()) {

            // angle between direction and guideVector
            float guideAngle = dir.angleRad(guideVector);

            float doAngle = Math.min(Math.abs(guideAngle), maxRotationSpeed);

            if (guideAngle < 0) {
                doAngle = -doAngle;
            }
            dir.rotateRad(doAngle);
        }
    }

    @Override
    protected Projectile createProjectile() {

        return new Bullet(getCalibre(), owner);
    }




    public static class AimFunction implements UnivariateFunction {

        public double rx, ry, vx, vy, ax, ay, VCC;


        public AimFunction() {}

        public double value(double t) {

            double result = Math.pow(rx,2) + Math.pow(ry,2) + ((Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(t,4))/4. +
                    Math.pow(t,3)*(ax*vx + ay*vy) + 2*t*(rx*vx + ry*vy) +
                    Math.pow(t,2)*(ax*rx + ay*ry - Math.pow(VCC,2) + Math.pow(vx,2) + Math.pow(vy,2));

            return result;
        }
    }



    public void selfGuiding(float dt) {

        // Система наведения для  minigun


        if (target== null || target.readyToDispose)
            return;

        // F = m*a
        // a = f / m;
        // dv = a*t
        // a = dv/t;
        // f/m = dv/t
        // dv = f/m*t - Импульс силы, деленный на массу пули



        gf.VCC = power / firingAmmoType.getMass() * dt;  // Начальная скорость пули



        //double[] root = new double[4];


        // s - object
        // t - target


        //at      -> a
        //rt - rs -> r
        //vt - vs -> v



        //ax = 0;
        //ay = 0;

        // ORIGINAL
        // r =  rt - rs
        gf.rx = target.pos.x + - owner.pos.x;
        gf.ry = target.pos.y +  - owner.pos.y;

        //  relative target velocity to object
        gf.vx = target.vel.x - owner.vel.x;
        gf.vy = target.vel.y - owner.vel.y;

        gf.ax = target.acc.x - owner.acc.x;
        gf.ay = target.acc.y - owner.acc.y;


        // Testing

//        rx = 56;
//        ry = -860;
//
//        vx = 0;
//        vy = 50;
//
//        ax = 0;
//        ay = 0;
//
//        VCC = 333;


        // Гидра доминатус !!!!



        //System.out.println("START: " + Instant.now());

        //double tbd = 0;
        // Цикл - попытка отделить корни

        //int i_tt = 0;
        for (int i = 0; i< 100; i++) {
            try {

                double t = nonBracketing.solve(100, gf,  0, dt * i*10);

                if (!Double.isNaN(t) && !Double.isInfinite(t) && t > 0) {

                    double vs_x = gf.rx / t + 0.5 * gf.ax * t + gf.vx;
                    double vs_y = gf.ry / t + 0.5 * gf.ay * t + gf.vy;

                    guideVector.set((float) vs_x, (float) vs_y).nor();
                    break;
                }
            }catch (Exception ignore) {}

        }

        //System.out.println("iterations: " +i_tt);
        //System.out.println("END: " + Instant.now());



//        Arrays.sort(root);
//
//        for (int i = root.length -1; i >= 0; i--) {
//
//            double t = root[i];
//
//            if (Double.isNaN(t) || Double.isInfinite(t) || t < 0)
//                continue;
//
//            // {rx/t + 0.5 ax t + vx, ry/t + 0.5 ay t + vy}
//
//            double vs_x = rx/t + 0.5 * ax * t + vx;
//            double vs_y = ry/t + 0.5 * ay * t + vy;
//
//            guideVector.set((float)vs_x, (float)vs_y).nor();
//
//            break;
//        }

    }

}
