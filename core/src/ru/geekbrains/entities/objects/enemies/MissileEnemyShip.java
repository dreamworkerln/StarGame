package ru.geekbrains.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.missile.PlasmaFragMissile;
import ru.geekbrains.entities.weapons.launchers.MissileLauncher;
import ru.geekbrains.entities.weapons.gun.CourseGun;

public class MissileEnemyShip extends AbstractEnemyShip {

    float aimingShift = (float) ThreadLocalRandom.current().nextDouble(1, 2);


    public MissileEnemyShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.MISSILE_ENEMY_SHIP);

        fuelConsumption = 0.1f;
        setMaxThrottle(50f);

        healthRegenerationCoefficient = 0.003f;
        setMaxHealth(2f);
        damage = getMaxHealth();
        mass = 0.5f;

        // tuning gun
        CourseGun gun = (CourseGun)componentList.get(CompNames.COURSE_GUN);
        gun.maxGunHeat = 0;
        maxRotationSpeed = 0.07f;

        // tuning launcher
        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);
        launcher.sideLaunchCount = 1;

        launcher.ammoTypeList.clear();
        launcher.addAmmoType(() -> new PlasmaFragMissile(new TextureRegion(MissileLauncher.MISSILE_TEXTURE), 2.5f, owner));
        launcher.init();


        collisionAvoidFilter = o-> o != this && !o.readyToDispose && o.owner != this &&
            (o.type.contains(ObjectType.SHIP) || o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) || o.type.contains(ObjectType.SHELL) || o.type.contains(ObjectType.BASIC_MISSILE));


        warnReticle = new WarnReticle(height, this);

    }

    @Override
    protected void guide(float dt) {

        if (this.readyToDispose) {
            return;
        }


        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);
        CourseGun gun = (CourseGun)componentList.get(CompNames.COURSE_GUN);

        // Никуда не целимся
        guideVector.setZero();
        //launcher.stopFire();

        // Останавливаем движок
        acquireThrottle(0);


        // Уклонение от падения на планету
        avoidPlanet(dt);

        // Уклонение от столкновения
        avoidCollision(dt);




        // ЛИБО Наведение на цель ------------------------------------------------------------------------

        // Если есть цель и мы не уклоняемся от планеты (если уклоняемся, то guideVector не Zero)
        if (target != null && guideVector.isZero()) {

//            // гидродоминируем с самонаведением пушки
//
//            // скорость снаряда
//            float maxVel = gun.getPower() / gun.getFiringAmmoType().getMass() * dt;
//            BPU.GuideResult gr = pbu.guideGun(this, target, maxVel, dt);
//
//            if (!gr.guideVector.isZero()) {
//                guideVector.set(gr.guideVector.nor());
//            }
//
//            // Самонаведение не сгидродоминировало
//            if (guideVector.isZero()) {
//                guideVector.set(target.pos).sub(pos).nor();
//            }


            // целимся позади жопы цели
            tmp0.set(target.vel).scl(aimingShift);

            if (target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
                tmp0.scl(5);
            }


            guideVector.set(target.pos).sub(pos).sub(tmp0).nor();


            acquireThrottle(maxThrottle/3);


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


        // fire anyway

        //tmp3.set(dir).scl(5);

        if(target != null && !target.readyToDispose) {

            tmp0.set(pos).add(dir);
            tmp1.set(pos).sub(dir);

            float lenFront = tmp2.set(target.pos).sub(tmp0).len();
            float lenBack = tmp2.set(target.pos).sub(tmp1).len();

            launcher.reverse(lenBack < lenFront);
            launcher.startFire();
        }

    }
}
