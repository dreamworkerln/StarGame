package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class AntiMissile extends Missile {

    public AntiMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.ANTIMISSILE);

        mass = 0.01f;
        fuel = 3f;

        maxThrottle = 5f;
        throttle = maxThrottle;

        setMaxHealth(0.9f);
        damage = 1f;


        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = true;
        selfdOnMiss = true;

    }
}
