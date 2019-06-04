package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

import ru.geekbrains.entities.weapons.MissileLauncher;
import ru.geekbrains.screen.GameScreen;

public class EnemyShip extends Ship {

    static Vector2 tmp0 = new Vector2();
    static Vector2 tmp1 = new Vector2();


    public MissileLauncher launcher;


    //GuideSystem guideSystem;

    //private boolean avoidPlanetModeOn = false;

    //Vector2 avoidVector = new Vector2(); // вектор

    public EnemyShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.ENEMY_SHIP);

        //guideSystem = new GuideSystem(this);

        gun.fireRate = 0.01f;
        //gun.maxGunHeat = 170;
        //gun.gunHeatingDelta = 65;
        //gun.coolingGunDelta = 2f;


        launcher = new MissileLauncher(10, this);
        //launcher.fireRate = 0.02f;
        launcher.fireRate = 0.003f;
        launcher.sideLaunchCount = 1;
    }


    @Override
    public void update(float dt) {

        super.update(dt);

        launcher.update(dt);

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
                selfGuiding();
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


    public void selfGuiding() {

        // Система наведения пушек и ракет(самонаведение)
        // https://gamedev.stackexchange.com/questions/149327/projectile-aim-prediction-with-acceleration

        //ToDo : посчитать наведение для пушек, убрать эту формулу(ниже)
        if (target== null || target.readyToDispose)
            return;

        //DrivenObject object,
        //GameObject target,
        //Vector2 result



        double ACC = maxThrottle / mass;  // Максимальное возможное ускорение объекта

        double[] root = new double[4];


        double ax, ay, vx, vy, rx, ry;

        // t - target
        // s - object


        //at      -> a
        //rt - rs -> r
        //vt - vs -> v

        //a = target.acc

        // костыли
        tmp0.set(target.pos);
        tmp1.set(target.vel);
        tmp1.scl(2.5f);
        tmp0.sub(tmp1);



        ax = target.acc.x;
        ay = target.acc.y;

        // r =  rt - rs
        //rx = target.pos.x - object.pos.x;
        //ry = target.pos.y - object.pos.y;

        rx = tmp0.x - pos.x;
        ry = tmp0.y - pos.y;

        // v =  vt - vs
        vx = target.vel.x - vel.x;
        vy = target.vel.y - vel.y;

        // apply inverted object acceleration to target
        ax -= acc.x;
        ay -= acc.y;

        double ddgzt = 96 * (ax * vx + ay * vy) * (rx * vx + ry * vy);
        double zpzpzp = 64 * (rx * vx + ry * vy);


        // Гидра доминатус !!!!

        // (Надо переписать - взять из Minigun, но хоть так вражеские корабли промахиваются, хрен с ним)

        root[0] = (ax*vx + ay*vy)/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) - Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) +
                (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                        16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                        (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                        (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))/2. -
                Math.sqrt((8*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) -
                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) -
                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        ((64*Math.pow(ax*vx + ay*vy,3))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),3) + zpzpzp /(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                (64*(ax*vx + ay*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2))/
                                (4.*Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                                3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                        3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))))/2.;




        root[1] = (ax*vx + ay*vy)/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) - Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) +
                (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                        16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                        (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                        (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))/2. +
                Math.sqrt((8*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) -
                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) -
                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        ((64*Math.pow(ax*vx + ay*vy,3))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),3) + zpzpzp /(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                (64*(ax*vx + ay*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2))/
                                (4.*Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                                3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                        3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))))/2.;



        root[2] = (ax*vx + ay*vy)/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) +
                (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                        16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                        (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                        (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))/2. -
                Math.sqrt((8*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) -
                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) -
                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                        ((64*Math.pow(ax*vx + ay*vy,3))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),3) + zpzpzp /(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                (64*(ax*vx + ay*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2))/
                                (4.*Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                                3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                        3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))))/2.;



        root[3] = (ax*vx + ay*vy)/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) +
                (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                        16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                        (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                        (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))/2. +
                Math.sqrt((8*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) -
                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) -
                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                        ((64*Math.pow(ax*vx + ay*vy,3))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),3) + zpzpzp /(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                (64*(ax*vx + ay*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2))/
                                (4.*Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                                3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                        3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))))/2.;


        Arrays.sort(root);

        for (int i = root.length -1; i >= 0; i--) {

            double t = root[i];

            if (Double.isNaN(t) || Double.isInfinite(t) || t < 0)
                continue;

            double as_x = ax + (2 * (rx + t *vx))/t*t; // тут ошибка в знаменателе должно быть /(t*t)
            double as_y = ay + (2 * (ry + t*vy))/t*t;  // но для наведения пушек и  так хорошо работает

            guideVector.set((float)as_x, (float)as_y).nor();

            break;
        }

    }


}

