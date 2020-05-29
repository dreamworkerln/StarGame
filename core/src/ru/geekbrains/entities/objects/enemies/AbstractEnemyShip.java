package ru.geekbrains.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Ship;
import ru.geekbrains.entities.objects.WeaponSystem;
import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.entities.weapons.MissileLauncher;


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

}
