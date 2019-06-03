package ru.geekbrains.entities.weapons;

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



    public float maxRange = 400f;

    public Minigun(float height, GameObject owner) {

        super(height, owner);

        maxRotationSpeed = 0.1f;

        radius = 50;
        calibre = 4;
        fireRate = 1;
        gunHeatingDelta = 2; // non-stop firing
        coolingGunDelta = 2;
        maxGunHeat = 200;
        //maxGunHeat = 200000000;
        power = 20;


        final double relativeAccuracy = 1.0e-12;
        final double absoluteAccuracy = 1.0e-8;

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
        if (target == null || target.type.contains(ObjectType.SHIP) ) {

            targets = GameScreen.getCloseObjects(owner, maxRange);

            // берем первую  цель (она будет ближе всего)
            // которая не является ни owner ни его снарядами,
            // Если нашли по missile приоритет огня выше

            for (GameObject o : targets) {

                if (o != owner &&
                        o.owner != owner &&
                        !o.readyToDispose &&
                         o.type.contains(ObjectType.SHIP)){

                    target = o;
                    break;
                }
            }


            for (GameObject o : targets) {

                if (o != owner &&
                        o.owner != owner &&
                        !o.readyToDispose &&
                        o.type.contains(ObjectType.MISSILE)){

                    target = o;
                    break;
                }
            }

        }



        // target out of range - reset
        if (target != null) {

            tmp0.set(target.pos).sub(pos);
            if (tmp0.len() > maxRange) {

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

        return new Bullet(calibre, owner);
    }




    private static class AimFunction implements UnivariateFunction {

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



        double[] root = new double[4];


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

        // HACKED
        //tmp2.set(target.vel).scl(-0.3f);//.scl(dt);
        //tmp0.set(target.pos).add(tmp2);
        //gf.rx = tmp0.x - owner.pos.x;
        //gf.ry = tmp0.y - owner.pos.y;



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
        for (int i = 0; i< 1000; i++) {
            try {

                double t = nonBracketing.solve(100, gf,  0, dt * i);

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
