package ru.geekbrains.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Ship;
import ru.geekbrains.entities.equipment.interfaces.WeaponSystem;
import ru.geekbrains.entities.weapons.MissileLauncher;
import ru.geekbrains.screen.GameScreen;


public abstract class AbstractEnemyShip extends Ship {

    float avoidCollisionImpactTime = 1f;
    float avoidCollisionAngle = (float) ThreadLocalRandom.current().nextDouble(25, 60);
    Predicate<GameObject> avoidCollisionObjectTypes;


    protected WeaponSystem launcher;

    public AbstractEnemyShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.ENEMY_SHIP);

        // -----------------------------------------------
        MissileLauncher missileLauncher = new MissileLauncher(10, this);
        missileLauncher.sideLaunchCount = 2;
        addComponent(CompNames.LAUNCHER, missileLauncher);

        launcher = weaponList.get(CompNames.LAUNCHER);

        avoidCollisionObjectTypes = o -> o == this || o.readyToDispose || o.type.contains(ObjectType.PLAYER_SHIP) ||
                !o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) && !o.type.contains(ObjectType.SHIP);
    }

    @Override
    protected void guide(float dt) {

        if (this.readyToDispose) {
            return;
        }


        WeaponSystem gun = weaponList.get(CompNames.GUN);

        // Никуда не целимся
        guideVector.setZero();

        // Останавливаем движок
        acquireThrottle(0);

        // Никуда не стреляем
        gun.stopFire();


        // Уклонение от падения на планету
        avoidPlanet(dt);

        // Уклонение от столкновения
        avoidCollision(dt);







        // ЛИБО Наведение на цель ------------------------------------------------------------------------

        // Если есть цель и мы не уклоняемся от планеты (если уклоняемся, то guideVector не Zero)
        if (target != null && guideVector.isZero()) {

            // гидродоминируем с самонаведением пушки

            // скорость снаряда
            float maxVel = gun.getPower() / gun.getFiringAmmoType().getMass() * dt;
            BPU.GuideResult gr = pbu.guideGun(this, target, maxVel, dt);

            if (!gr.guideVector.isZero()) {
                guideVector.set(gr.guideVector.nor());
            }

            // Самонаведение не сгидродоминировало
            if (guideVector.isZero()) {
                guideVector.set(target.pos).sub(pos).nor();
            }

            // Acceleration

            acquireThrottle(maxThrottle);

            // Gun control

            if (target != null &&
                    Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed*1.5f) {

                gun.startFire();
                launcher.startFire();
            }
            else {
                gun.stopFire();
                launcher.stopFire();
            }
        }
    }



    protected void avoidCollision(float dt) {

        if (this.readyToDispose) {
            return;
        }

        List<GameObject> targetList = GameScreen.getCloseObjects(this, this.radius * 25);

        // leave only ships and missiles
        targetList.removeIf(avoidCollisionObjectTypes);

//        if(targetList.size() == 0) {
//            return;
//        }

        tmp1.setZero();

        for (GameObject o : targetList) {
            BPU.GuideResult gr = pbu.guideGun(this, o, this.vel.len(), dt);

            Float impactTime = (float)gr.impactTime;


            if (!impactTime.isNaN() && impactTime > 0 && impactTime < avoidCollisionImpactTime) {
                // normalize to 1
                tmp2.set(gr.guideVector).nor().scl(-(avoidCollisionImpactTime-impactTime)/avoidCollisionImpactTime);
                tmp1.add(tmp2);
            }

            float distance = tmp2.set(o.pos).sub(pos).len() - o.radius - this.radius;
            float minSafeDistance = (o.radius + this.radius)*4;
            if (distance < minSafeDistance) {

                // normalize to 1
                tmp1.add(tmp2.nor().scl(-(minSafeDistance - distance * 2)/minSafeDistance));

            }

        }

        // стены

        float leftBound = GameScreen.getInstance().worldBounds.getLeft() * GameScreen.getInstance().aspect;
        float rightBound = GameScreen.getInstance().worldBounds.getRight() * GameScreen.getInstance().aspect;

        float upBound = GameScreen.getInstance().worldBounds.getTop();
        float downBound = GameScreen.getInstance().worldBounds.getBottom();

        if (pos.x <= leftBound + 2*radius) {
            tmp1.add(tmp4.set(1, 0).scl(1));
        }
        if (pos.x >= rightBound - 2*radius) {
            tmp1.add(tmp4.set(-1, 0).scl(1));
        }
        if (pos.y >= upBound - 2*radius) {
            tmp1.add(tmp4.set(0, -1).scl(1));
        }
        if (pos.y <= downBound + 2*radius) {
            tmp1.add(tmp4.set(0, 1).scl(1));
        }

        if (!tmp1.isZero()) {
            //guideVector.set(tmp1).nor();


            // слева или справа
            //tmp1.set(vel).nor();
            //tmp2.set(tmp1).sub(guideVector);

            float angle = vel.angle(tmp1);
            if (angle > 0) {
                tmp1.rotate(-avoidCollisionAngle);
            } else {
                // планета справа от вектора скорости
                tmp1.rotate(avoidCollisionAngle);
            }
            acquireThrottle(maxThrottle);

            if (guideVector.isZero()) {
                guideVector.set(tmp1).nor();
            }
            else {
                guideVector.add(tmp1.scl(0.5f)).nor();
            }

        }


//        if (!tmp1.isZero()) {
//            guideVector.set(tmp1).nor();
//
//
//            // слева или справа
//            //tmp1.set(vel).nor();
//            //tmp2.set(tmp1).sub(guideVector);
//
//            float angle = vel.angle(guideVector);
//            if (angle > 0) {
//                guideVector.rotate(-avoidCollisionAngle);
//            } else {
//                // планета справа от вектора скорости
//                guideVector.rotate(avoidCollisionAngle);
//            }
//            acquireThrottle(maxThrottle);
//        }


    }


}
