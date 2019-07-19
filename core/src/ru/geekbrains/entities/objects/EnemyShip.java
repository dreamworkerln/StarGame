package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import java.util.Arrays;

import ru.geekbrains.entities.weapons.Minigun;
import ru.geekbrains.entities.weapons.MissileLauncher;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public class EnemyShip extends Ship {

    //static Vector2 tmp0 = new Vector2();
    //static Vector2 tmp1 = new Vector2();


    public MissileLauncher launcher;


    Minigun.AimFunction gf;
    UnivariateSolver nonBracketing;

    public Minigun minigun;




    //GuideSystem guideSystem;

    //private boolean avoidPlanetModeOn = false;

    //Vector2 avoidVector = new Vector2(); // вектор

    public EnemyShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.ENEMY_SHIP);

        //guideSystem = new GuideSystem(this);

        gun.fireRate = 0.01f;

        //gun.fireRate = 0.1f;
        //gun.fireRate = 0.02f;
        //gun.maxGunHeat = 170;
        //gun.gunHeatingDelta = 65;
        //gun.coolingGunDelta = 2f;


        launcher = new MissileLauncher(10, this);

        //launcher.fireRate = 0.02f;
        launcher.sideLaunchCount = 2;

        final double relativeAccuracy = 1.0e-6;
        final double absoluteAccuracy = 1.0e-4;
        gf =  new Minigun.AimFunction();
        nonBracketing = new BrentSolver(relativeAccuracy, absoluteAccuracy);


        //minigun = new Minigun(4, this);


    }


    @Override
    public void update(float dt) {

        super.update(dt);

        launcher.update(dt);

        //minigun.update(dt);

    }


    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);

        //minigun.draw(renderer);
    }

    @Override
    protected void guide(float dt) {

        GameObject planet = GameScreen.INSTANCE.planet;

        // Не уклоняемся от планеты
        //avoidPlanetModeOn = false;

        // Никуда не целимся
        guideVector.setZero();

        // Останавливаем движок
        throttle = 0;

        // Никуда не стреляем
        gun.stopFire();




        // Уклонение от падения на планету ---------------------------------------------------------

        // 1. Корабль летит в сторону планеты ?

        tmp0.set(planet.pos).sub(pos); // вектор на планету

        float distToPlanet = tmp0.len();

        if (Math.abs(vel.angle(tmp0)) < 90) {


            // Расстояние от прямой, построенной на векторе скорости корабля до планеты
            // Минимальное сближение с планетой
            tmp0.set(pos).add(vel).nor(); // вектор прямой
            float minConvergence = Intersector.distanceLinePoint(pos.x, pos.y, tmp0.x, tmp0.y,
                    planet.pos.x,
                    planet.pos.y);


            float impactTime = distToPlanet / vel.len();

            // Если минимальное сближение меньше диаметра планеты и время сближения (меньше n)
            if (minConvergence < 2 *planet.radius &&
                    impactTime < 6 &&
                    distToPlanet  < 400f + planet.radius) {
                    //distToPlanet  < 40f + planet.radius) {

                // необходимо совершить маневр уклонения

                tmp0.set(planet.pos).sub(pos); // вектор на планету

                // слева или справа планета от вектора скорости
                float angle = tmp1.set(vel).angle(tmp0);

                // планета слева от вектора скорости
                if (angle > 0) {
                    guideVector.set(vel).rotate(-90).nor();
                } else {
                    // планета справа от вектора скорости
                    guideVector.set(vel).rotate(90).nor();
                }

                // Acceleration

                if (Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {
                    throttle = maxThrottle;
                }
                else {
                    throttle = 0;
                }

            }
        }


        // ЛИБО Наведение на цель ------------------------------------------------------------------------

        // Если есть цель и мы не уклоняемся от планеты
        if (target != null && guideVector.isZero()) {


            if (tmp0.set(target.pos).sub(pos).len() > 150) {

                // гидродоминируем с самонаведением от ракет
                selfGuiding(dt);
            }

            // Самонаведение не сгидродоминировало
            if (guideVector.isZero()) {

                guideVector.set(target.pos).sub(pos).nor();
            }


            // Acceleration

            throttle = maxThrottle * 1f;

            // Gun control

            if (target != null &&
                    Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {

                gun.startFire();
                launcher.startFire();
            }
            else {
                gun.stopFire();
                launcher.stopFire();
            }
        }

        // -----------------------------------------------------------------------------------------


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

    public void selfGuiding(float dt) {

        // Система наведения для пушки
        //https://gamedev.stackexchange.com/questions/149327/projectile-aim-prediction-with-acceleration


        if (target == null || target.readyToDispose)
            return;

        // F = m*a
        // a = f / m;
        // dv = a*t
        // a = dv/t;
        // f/m = dv/t
        // dv = f/m*t - Импульс силы, деленный на массу пули



        gf.VCC = gun.power / gun.firingAmmoType.getMass() * dt;  // Начальная скорость снаряда пушки



        // ORIGINAL
        // r =  rt - rs
        gf.rx = target.pos.x + -  pos.x;
        gf.ry = target.pos.y +  - pos.y;

        //  relative target velocity to object
        gf.vx = target.vel.x - vel.x;
        gf.vy = target.vel.y - vel.y;

        gf.ax = target.acc.x - acc.x;
        gf.ay = target.acc.y - acc.y;


        //int i_tt = 0;
        for (int i = 0; i< 100; i++) {
            try {

                // Корней нет - функция не пересекает ось Ox
                if (gf.value(0) > 0 && gf.value(dt * i*10) > 0 ||
                        gf.value(0) < 0 && gf.value(dt * i*10) < 0) {

                    continue;
                }

                double t = nonBracketing.solve(100, gf,  0, dt * i*10);

                if (!Double.isNaN(t) && !Double.isInfinite(t) && t > 0) {

                    double vs_x = gf.rx / t + 0.5 * gf.ax * t + gf.vx;
                    double vs_y = gf.ry / t + 0.5 * gf.ay * t + gf.vy;

                    guideVector.set((float) vs_x, (float) vs_y).nor();
                    break;
                }
            }catch (Exception ignore) {}

        }

    }





}

