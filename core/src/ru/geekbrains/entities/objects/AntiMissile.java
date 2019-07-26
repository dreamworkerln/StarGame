package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AntiMissile extends Missile {

    public AntiMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.ANTIMISSILE);

        mass = 0.01f;
        fuel = 3f;

        maxThrottle = 5f;
        throttle = maxThrottle;
        maxHealth = 0.04f;
        health = maxHealth;

        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = true;
        selfdOnMiss = true;

    }
}
