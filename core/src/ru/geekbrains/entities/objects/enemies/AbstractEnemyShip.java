package ru.geekbrains.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Ship;
import ru.geekbrains.entities.equipment.interfaces.WeaponSystem;
import ru.geekbrains.entities.weapons.MissileLauncher;
import ru.geekbrains.screen.GameScreen;


public abstract class AbstractEnemyShip extends Ship {


    protected WeaponSystem launcher;

    public AbstractEnemyShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.ENEMY_SHIP);

        // -----------------------------------------------
        MissileLauncher missileLauncher = new MissileLauncher(10, this);
        missileLauncher.sideLaunchCount = 2;
        addComponent(CompNames.LAUNCHER, missileLauncher);

        launcher = weaponList.get(CompNames.LAUNCHER);
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
        throttle = 0;

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

            throttle = maxThrottle;

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
    }



    protected void avoidCollision(float dt) {

        if (this.readyToDispose) {
            return;
        }

        List<GameObject> targetList = GameScreen.getCloseObjects(this, this.radius * 50);

        // leave only ships and missiles
        targetList.removeIf(o -> o == this || o.readyToDispose ||
                o.type.contains(ObjectType.PLAYER_SHIP) ||
                !o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) && !o.type.contains(ObjectType.SHIP));

        if(targetList.size() == 0) {
            return;
        }

        BPU.GuideResult gr = pbu.guideGun(this, targetList.get(0), this.vel.len(), dt);

        Float impactTime = (float)gr.impactTime;

        if (!impactTime.isNaN() && impactTime > 0 && impactTime < 2) {
            guideVector.set(targetList.get(0).pos).sub(pos).nor().scl(-1);
        }

        if (Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {
            throttle = maxThrottle;
        }
        else {
            throttle = 0;
        }
    }

}
