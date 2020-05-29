package ru.geekbrains.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.WeaponSystem;
import ru.geekbrains.entities.objects.enemies.AbstractEnemyShip;
import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.entities.weapons.MissileLauncher;

public class SmallEnemyShip extends AbstractEnemyShip {

    float tmp  = (float) ThreadLocalRandom.current().nextDouble(1, 2);


    public SmallEnemyShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        fuelConsumption = 4f;
        setMaxThrottle(20f);
        setMaxFuel(100f);
        setMaxHealth(2f);
        mass = 0.5f;

        type.add(ObjectType.SMALL_ENEMY_SHIP);

        // tuning gun
        Gun gun = (Gun)componentList.get(CompNames.GUN);
        gun.maxGunHeat = 0;
        //maxRotationSpeed = 1;

        // tuning launcher
        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);
        launcher.sideLaunchCount = 1;
    }

    @Override
    protected void guide(float dt) {

        if (this.readyToDispose) {
            return;
        }


        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);
        Gun gun = (Gun)componentList.get(CompNames.GUN);

        // Никуда не целимся
        guideVector.setZero();
        launcher.stopFire();

        // Останавливаем движок
        //throttle = 0;



        // Уклонение от падения на планету
        avoidPlanet(dt);

        // ЛИБО Наведение на цель ------------------------------------------------------------------------

        // Если есть цель и мы не уклоняемся от планеты (если уклоняемся, то guideVector не Zero)
        if (target != null && guideVector.isZero()) {

            // гидродоминируем с самонаведением пушки

            // скорость снаряда
            float maxVel = gun.getPower() / gun.getFiringAmmoType().getMass() * dt;
            pbu.guideGun(this, target, maxVel, dt);

            if (!pbu.guideResult.guideVector.isZero()) {
                guideVector.set(pbu.guideResult.guideVector.nor());
            }

            // Самонаведение не сгидродоминировало
            if (guideVector.isZero()) {
                guideVector.set(target.pos).sub(pos).nor();
            }


            tmp0.set(target.vel).scl(tmp);
            guideVector.set(target.pos).sub(pos).sub(tmp0).nor();


            throttle = maxThrottle;

            tmp0.set(pos).add(dir);
            tmp1.set(pos).sub(dir);

            float lenFront = tmp2.set(target.pos).sub(tmp0).len();
            float lenBack =  tmp2.set(target.pos).sub(tmp1).len();

            launcher.reverse(lenBack < lenFront);

            launcher.startFire();


//            tmp0.set(dir).scl(-1);
//            if (Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed ||
//                Math.abs(tmp0.angleRad(guideVector) ) < maxRotationSpeed*3 ) {
//
//                //gun.startFire();
//                launcher.startFire();
//            }
//            else {
//                //gun.stopFire();
//                launcher.stopFire();
//            }
        }
    }
}
