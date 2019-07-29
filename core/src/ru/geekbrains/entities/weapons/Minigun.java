package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.math.Intersector;


import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;


import java.util.List;

import ru.geekbrains.entities.projectile.Bullet;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Projectile;
import ru.geekbrains.screen.GameScreen;


public class Minigun extends Gun {


    // Скопировано из DrivenObject, EnemyShip
    // походу нужно множественное наследование из C++

    AimFunction gf;
    UnivariateSolver nonBracketing;

    private int step = 0;
    //private int maxStep = 100;




    public float maxRange = 350f;

    public Minigun(float height, GameObject owner) {

        super(height, owner);

        maxRotationSpeed = 0.1f;
        maxRotationSpeed = 1f;

        radius = 50;
        setCalibre(2);
        fireRate = 1f;
        gunHeatingDelta = 2; // non-stop firing
        coolingGunDelta = 2;
        maxGunHeat = 200;
        power = 20;


        final double relativeAccuracy = 1.0e-12;
        final double absoluteAccuracy = 1.0e-8;

        //final double relativeAccuracy = 1.0e-10;
        //final double absoluteAccuracy = 1.0e-8;
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



        // Если нет цели или это Ship (стрельба только по missile)  - так стрельба по кораблям и missile
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




        // Смысл - не стрелять по целям, которые не попадут в нас, без симуляции полета каждой цели.

        // Как оно работает я хз
        // Заменить всю эту хрень на проверку пересечение луча с кругом
        if (target != null) {



            tmp1.set(target.vel).scl(200); // 200 - эмпирически
            tmp2.set(owner.vel).scl(200);


            float scale = Intersector.intersectRayRay(target.pos, tmp1, owner.pos, tmp2);


            //collinear
            if (Float.isInfinite(scale) || tmp1.angle(tmp2) < 0.1f) {
                //tmp1.set(owner.vel).sub(target.vel);

                tmp0.set(owner.pos).sub(target.pos); // vec from target to owner
                tmp4.set(target.vel).sub(owner.vel);


                //float angle = Math.abs(target.vel.angle(owner.vel));
                if(Math.abs(tmp4.angle(tmp0)) <= 100) {
                    tmp3.set(owner.pos); // попал в цель
                }
                else {
                    // не попал
                    tmp3.set(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
                }

            }
            else {

                tmp3.set(tmp1).scl(scale).add(target.pos); // точа пересечения

                tmp0.set(owner.pos).sub(target.pos); // vec from ship to target
                tmp4.set(target.vel).sub(owner.vel); // relative velocity vector

                if(Math.abs(tmp4.angle(tmp0)) <= 100){

                }
                else {
                    // не попал
                    tmp3.set(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
                }
            }
            float len = tmp1.set(tmp3).sub(owner.pos).len();


            // target out of range - reset
            tmp0.set(target.pos).sub(owner.pos);

            if (tmp0.len() > maxRange || // вне радиуса действия minigun

                    // точка пересечения с центром цели лежит далеко
                    (len > maxRange)) {

                target = null;
            }
        }



        // --------------------------------------------------
        //aiming target




        guideVector.setZero();


        if(target != null) {

            tmp0.set(target.pos).sub(owner.pos);
            float len = tmp0.len();


            selfGuiding(dt);


            tmp0.set(target.pos).sub(owner.pos);


            //Нормаль к вектору корабль-цель (нормализованный)
            tmp3.set(tmp0).rotate(90).nor();

            // относительная скорость цели к кораблю
            tmp2.set(target.vel).sub(owner.vel);

            // проекция вектора скорости цели на нормаль к лучу корабль - цель
            float tmp = tmp3.dot(tmp2);

            // функция обратного числа (от расстояния корабль-цель), умноженная на константу 100 (эмпирические данные)
            float ttt = (owner.getRadius())*100/tmp0.len();

            tmp *= dt * ttt; // увеличиваем эту проекцию на ttt и умножаем на dt (переходим от скорости к расстоянию)

            tmp3.scl(tmp); // скалируем нормаль на tmp - получаем смещение но нормали за dt



            // Генерирует окружность радиусом ||tmp3|| и центром в (0,0) используя step в качестве времени
            // (Гармонические колебания)
            // step будет меняться от выстрела к выстрелу (step +=20 подобрано эмпирически)
            // пули будут лететь по синусоиде

            step +=20;
            if (step > 360) {
                step = 0;
            }

            float xx = (float) Math.cos(step*Math.PI/180.)*tmp3.len();
            float yy = (float) Math.sin(step*Math.PI/180.)*tmp3.len();


            tmp0.set(xx, yy);

            // смещаем guideVector на вектор этой окружности
            guideVector.add(tmp0);


            // Если самонаведение не осилиось
            if (guideVector.isZero()) {
                guideVector.set(target.pos).sub(pos).nor();
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
        //https://gamedev.stackexchange.com/questions/149327/projectile-aim-prediction-with-acceleration


        if (target== null || target.readyToDispose)
            return;

        // F = m*a
        // a = f / m;
        // dv = a*t
        // a = dv/t;
        // f/m = dv/t
        // dv = f/m*t - Импульс силы, деленный на массу пули



        gf.VCC = power / firingAmmoType.getMass() * dt;  // Начальная скорость пули


        // ORIGINAL
        // r =  rt - rs
        gf.rx = target.pos.x - owner.pos.x;
        gf.ry = target.pos.y - owner.pos.y;

        //  relative target velocity to object
        gf.vx = target.vel.x - owner.vel.x;
        gf.vy = target.vel.y - owner.vel.y;

        gf.ax = target.acc.x - owner.acc.x;
        gf.ay = target.acc.y - owner.acc.y;


        // Гидра доминатус !!!!


        //double tbd = 0;
        // Цикл - попытка отделить корни

        //int i_tt = 0;
        for (int i = 0; i< 100; i++) {
            try {

                // Корней нет - функция не пересекает ось Ox
                if (gf.value(0) > 0 && gf.value(dt * i*10) > 0 ||
                    gf.value(0) < 0 && gf.value(dt * i*10) < 0) {

                    continue;
                }

                double t = nonBracketing.solve(100, gf,  0, dt * i*10);

                //t -= 0.2;

                if (!Double.isNaN(t) && !Double.isInfinite(t) && t > 0) {

                    double vs_x = gf.rx / t + 0.5 * gf.ax * t + gf.vx;
                    double vs_y = gf.ry / t + 0.5 * gf.ay * t + gf.vy;

                    guideVector.set((float) vs_x, (float) vs_y);//.nor();
                    break;
                }
            }catch (Exception ignore) {}

        }


    }

}
