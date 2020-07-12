package ru.geekbrains.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.weapons.FlakCannon;
import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.entities.weapons.Minigun;

public class BattleEnemyShip extends AbstractEnemyShip {

    public BattleEnemyShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.BATTLE_ENEMY_SHIP);

        setMass(10f);
        setMaxHealth(100f);

        setMaxFuel(10000f);
        fuelConsumption /=100;

        maxThrottle = 300f;

        damage = 10f;
        penetration = 1;
        armour = 1;
        maxRotationSpeed = 0.02f;

        //guideSystem = new GuideSystem(this);


        // tuning gun
        Gun gun = (Gun)componentList.get(CompNames.GUN);
        gun.setCalibre(8);
        gun.setFireRate(0.1f);
        gun.drift = 0.03f;
        gun.burst= 2;
        gun.fireRate = 0.1f;
        gun.gunHeatingDelta = 30;
        gun.maxGunHeat = 200;
        gun.coolingGunDelta = 2f;

        Minigun minigun = new Minigun(4, this);
        addComponent(CompNames.MINIGUN,minigun);

        FlakCannon flakCannon = new FlakCannon(10, this);
        flakCannon.fireRate = 0.01f;
        flakCannon.setFiringMode(FlakCannon.FiringMode.PLASMA_ONLY);
        addComponent(CompNames.FLACK_CANNON,flakCannon);

        // I'm unstoppable!
        avoidCollisionTypesFilter = o -> true;
    }

}
