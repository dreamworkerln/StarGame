package ru.geekbrains.entities.weapons;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.DummyObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Planet;
import ru.geekbrains.entities.projectile.Bullet;
import ru.geekbrains.entities.projectile.Projectile;
import ru.geekbrains.screen.GameScreen;


public class Minigun extends Gun {

    private static Sound minigunFire;
    private static boolean minigunPlaying = false;


    // Скопировано из DrivenObject, EnemyShip
    // походу нужно множественное наследование из C++


    private int step = 0;
    //private int maxStep = 100;

    // Цели, отсортированные по времени попадания в корабль
    private NavigableMap<Float, BPU.GuideResult> impactTimes = new TreeMap<>();

    private NavigableMap<Float, GameObject> impactTimesCalculated = new TreeMap<>();

    private List<GameObject> targetList = new ArrayList<>();

    // Цели, отсортированные по времени попадания в корабль
    //private NavigableMap<Float, GameObject> distances = new TreeMap<>();

    public float maxRange = 500;
    public float maxTime = 2f;

    static {
        minigunFire = Gdx.audio.newSound(Gdx.files.internal("vulcan.mp3"));
    }

    public Minigun(float height, GameObject owner) {

        super(height, owner);

        isModule = true;

        maxRotationSpeed = 0.1f;
        maxRotationSpeed = 1f;

        radius = 50;
        setCalibre(2);
        fireRate = 1f;
        gunHeatingDelta = 2; // non-stop firing
        coolingGunDelta = 2;
        maxGunHeat = 200;
        power = 20;


//        final double relativeAccuracy = 1.0e-12;
//        final double absoluteAccuracy = 1.0e-8;
//
//        gf =  new AimFunction();
//        nonBracketing = new BrentSolver(relativeAccuracy, absoluteAccuracy);
    }


    @Override
    public void update(float dt) {

        super.update(dt);

//
        // --------------------------------------------------


        impactTimes.clear();
        impactTimesCalculated.clear();
        targetList.clear();

        // getting target
        if (owner != null && !owner.readyToDispose) {
            targetList = GameScreen.getCloseObjects(owner, maxRange);
        }


        for (GameObject o : targetList) {

            if (o == owner ||o.owner == owner ||o.readyToDispose) {
                continue;
            }

            if ( (!o.type.contains(ObjectType.MISSILE) &&
                    !o.type.contains(ObjectType.SHIP)) ) {

                continue;
            }

            //tmp1.set(o.pos).sub(owner.pos);

            if (!owner.readyToDispose) {
                float maxPrjVel = power / firingAmmoType.getMass() * dt;  // Задаем начальную скорость пули
                pbu.guideGun(owner, o, maxPrjVel, dt);
            }
            // get results

            Float impactTime = (float)pbu.guideResult.impactTime;

            if (!impactTime.isNaN() && impactTime >= 0 && impactTime <= maxTime) {

                impactTimes.put(impactTime, pbu.guideResult.clone());
            }
        }



        // calculating in simulator


        DummyObject ship  = new DummyObject(owner);
        Planet planet = GameScreen.INSTANCE.planet;


        int iterationCount = 300;

        for (Map.Entry<Float, BPU.GuideResult> entry : impactTimes.entrySet()) {

            ship.setRadius(owner.getRadius());
            ship.setMass(owner.getMass());
            ship.dir.set(owner.dir);
            ship.pos.set(owner.pos);
            ship.vel.set(owner.vel);
            ship.acc.set(owner.acc);

            GameObject tgt = entry.getValue().target;
            DummyObject missile = new DummyObject(tgt);
            missile.setMass(entry.getValue().target.getMass());
            missile.dir.set(tgt.dir);
            missile.pos.set(tgt.pos);
            missile.vel.set(tgt.vel);
            missile.acc.set(tgt.acc);


            for (int i = 0; i <  iterationCount; i++) {


                // calculate gravitation force from planet
                GameScreen.applyPlanetGravForce(ship, planet);
                GameScreen.applyPlanetGravForce(missile, planet);

                // update aceleration, velocity, position
                ship.update(dt);
                missile.update(dt);

                // check collision ship to planet
                tmp0.set(planet.pos).sub(ship.pos);
                if (tmp0.len() <= planet.getRadius() + ship.getRadius()) {
                    break;
                }

                // check collision missile to planet
                tmp0.set(planet.pos).sub(missile.pos);
                if (tmp0.len() <= planet.getRadius() + missile.getRadius()) {
                    break;
                }

                // check collision missile to ship  (-50%)
                tmp0.set(ship.pos).sub(missile.pos);
                if (tmp0.len() <= (ship.getRadius() + missile.getRadius())*1.7f) {

                    impactTimesCalculated.put(i*dt, tgt);
                    break;
                }


            }
        }


        //impactTimesCalculated.entrySet().removeIf(entry -> entry.getValue().readyToDispose);


        target = null;
        guideVector.setZero();
        if (impactTimesCalculated.size() > 0) {
            target = impactTimesCalculated.firstEntry().getValue();
        }

        // ---------------------------------------------------------------

        if (target != null && !target.readyToDispose) {

            float maxPrjVel = power / firingAmmoType.getMass() * dt;
            pbu.guideGun(owner, target, maxPrjVel, dt);
            guideVector.set(pbu.guideResult.guideVector);

            // Делаем разброс
            // -----------------------------------------------------------

            // вектор пушка(корабль) - цель
            tmp0.set(target.pos).sub(owner.pos);

            //Нормаль к вектору корабль-цель (нормализованный)
            tmp3.set(tmp0).rotate(90).nor();

            // относительная скорость цели к кораблю
            tmp2.set(target.vel).sub(owner.vel);

            // проекция вектора скорости цели на нормаль к лучу корабль - цель
            float tmp = tmp3.dot(tmp2);

            // функция обратного числа (от расстояния корабль-цель), умноженная на константу 100 (эмпирические данные)

            float z = tmp0.len();
            float ttt;

            if (z > owner.getRadius() * 2f) {
                ttt = (owner.getRadius()) * 50 / z;     // was 100
            } else {         //if (z <= owner.getRadius() * 2f)
                ttt = (owner.getRadius()) * 50 / z;      // was 50
            }

            //ttt = (owner.getRadius()) * 50 / tmp0.len();

            tmp *= dt * ttt;
            //tmp *= dt * ttt * 1.5 + 0.5; // увеличиваем эту проекцию на ttt и умножаем на dt (переходим от скорости к расстоянию)

            tmp3.scl(tmp); // скалируем нормаль на tmp - получаем смещение по нормали за dt


            // Генерирует окружность радиусом ||tmp3|| и центром в (0,0) используя step в качестве времени
            // (Гармонические колебания)
            // step будет меняться от выстрела к выстрелу (step +=20 подобрано эмпирически)
            // пули будут лететь по синусоиде

            step += 30;
            if (step > 360) {
                step = 0;
            }

            //System.out.println(tmp3.len());
            //tmp3.scl(1.5f);

//            //минимальный разброс
//            if (tmp3.len() < 5  && z > owner.getRadius() * 2) {
//                tmp3.setLength(5);
//            }
//
//            //минимальный разброс
//            if (tmp3.len() < 5  && z <= owner.getRadius() * 2) {
//                tmp3.setLength(7);
//            }

            if (z > owner.getRadius() * 4) {

                if (tmp3.len() < 5) {
                    tmp3.setLength(5);
                }
            }
            else {

                if (tmp3.len() < 10) {
                    tmp3.setLength(10);
                }

            }

            //максимальный разброс
            if (tmp3.len() > 50) {
                tmp3.setLength(50);
            }

            //System.out.println("z: " + z + " owner.getRadius():" + owner.getRadius() +"    " + tmp3.len());

            float xx = (float) Math.cos(step*Math.PI/180.)*tmp3.len();
            float yy = (float) Math.sin(step*Math.PI/180.)*tmp3.len();

            tmp0.set(xx, yy);

            //System.out.println(tmp3.len());
            //System.out.println(guideVector);


            guideVector.add(tmp0);


//            // Если самонаведение не осилиось
//            if (guideVector.isZero()) {
//                guideVector.set(target.pos).sub(pos).nor();
//            }
//            else {
//
//
//                // смещаем guideVector на вектор этой окружности
//                guideVector.add(tmp0);
//            }
        }












//        // фильтруем impactTimesCalculated,
//        // если в списке только ракеты - стреляем по первой
//        // если ракет нет а есть корабль - стреляем по первому кораблю
//        // но по факту получается по последнему - хрен с ним, надо по нормальному создавать индекс по типу цели
//
//        for (Map.Entry<Float, GameObject> entry : impactTimesCalculated.entrySet()) {
//
////            if(entry.getValue() == null ||
////                    entry.getValue().readyToDispose) {
////                continue;
////            }
//
//            if (entry.getValue().type.contains(ObjectType.MISSILE) ||
//                entry.getValue().type.contains(ObjectType.SHIP)) {
//
//                target = entry.getValue();
//                guideVector.set(entry.getValue());
//            }
//
//            if (target != null &&
//                    target.type.contains(ObjectType.MISSILE)) {
//
//                break;
//            }
//        }



        // --------------------------------------------------
        //aiming target



        // Auto fire control
        if (target != null && !target.readyToDispose &&
                Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {

            startFire();

        }
        else {
            stopFire();
        }
    }



    @Override
    protected void rotateObject() {

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
    public void startFire() {

        super.startFire();



//        try {
//            Thread.sleep(50);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        if (this.getClass() == Minigun.class && !minigunPlaying) {
            minigunPlaying = true;
            minigunFire.loop(0.3f);
        }
    }


    @Override
    public void stopFire() {

        super.stopFire();

        if (this.getClass() == Minigun.class && minigunPlaying) {
            minigunPlaying = false;
            minigunFire.stop();
        }
    }

    @Override
    protected Projectile createProjectile() {
        return new Bullet(calibre,   owner);
    }




    @Override
    public void dispose() {

        stopFire();
        super.dispose();
    }


    /*

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
    */


/*

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
                    impactTime = t;

                    break;
                }
            }catch (Exception ignore) {}

        }


    }
*/

}
