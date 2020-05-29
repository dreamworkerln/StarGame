package ru.geekbrains.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.weapons.Gun;

public class MainEnemyShip extends AbstractEnemyShip {







    public MainEnemyShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        setMaxFuel(300f);

        type.add(ObjectType.MAIN_ENEMY_SHIP);

        //guideSystem = new GuideSystem(this);

        //gun.fireRate = 1f;
        //gun.coolingGunDelta = 40;

        // tuning gun
        Gun gun = (Gun)componentList.get(CompNames.GUN);
        gun.setFireRate(0.01f);
        maxRotationSpeed *= 2f;

        //gun.fireRate = 0.1f;
        //gun.fireRate = 0.02f;
        //gun.maxGunHeat = 170;
        //gun.gunHeatingDelta = 65;
        //gun.coolingGunDelta = 2f;
        //minigun = new Minigun(4, this);


    }





    @Override
    public void dispose() {

        super.dispose();
    }



/*

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
        gf.rx = target.pos.x -  pos.x;
        gf.ry = target.pos.y  - pos.y;

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

    }*/





}

