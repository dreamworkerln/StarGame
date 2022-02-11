package ru.dreamworkerln.stargame.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.concurrent.ThreadLocalRandom;

import ru.dreamworkerln.stargame.entities.equipment.CompNames;
import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.projectile.missile.PlasmaFragMissile;
import ru.dreamworkerln.stargame.entities.weapons.launchers.MissileLauncher;
import ru.dreamworkerln.stargame.entities.weapons.gun.CourseGun;

public class MissileShip extends AbstractAIShip {

    float aimingShift = (float) ThreadLocalRandom.current().nextDouble(1, 2);


    public MissileShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.MISSILE_SHIP);

        fuelConsumption = 0.1f;
        setMaxThrottle(50f);

        setHealthRegenerationCoefficient(0.003f);
        setMaxHealth(2f);
        damage = getMaxHealth();
        mass = 0.5f;

        // tuning gun
        CourseGun gun = (CourseGun)componentList.get(CompNames.COURSE_GUN);
        gun.maxGunHeat = 0;
        setMaxRotationSpeed(0.07f);

        // tuning launcher
        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);
        launcher.setPylonCount(1);

        launcher.ammoProducer.clear();
        launcher.addAmmoType(() -> new PlasmaFragMissile(new TextureRegion(MissileLauncher.MISSILE_TEXTURE), 2.5f, owner));

        // re-init all weapons
        init();


        collisionAvoidFilter = o-> o != this && !o.readyToDispose && o.owner != this &&
            (o.type.contains(ObjectType.SHIP) || o.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO) || o.type.contains(ObjectType.SHELL) || o.type.contains(ObjectType.BASIC_MISSILE));


        warnReticle = new WarnReticle(height, this);

    }

    @Override
    protected void guide(float dt) {

        if (this.readyToDispose) {
            return;
        }

        // выбираем цель
        selectTarget(dt);


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

            if (target.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) {
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
