package ru.dreamworkerln.stargame.entities.projectile;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;

public class Bullet extends Projectile {


    public Bullet(float height, GameObject owner) {
        super(height, owner);
    }

    public Bullet(float height, float trailRadius, GameObject owner) {
        super(height,trailRadius, owner);
    }

    @Override
    protected void postConstruct() {

        type.add(ObjectType.BULLET);
        mass = 0.001f;
        firePower = 20;

        setMaxHealth(0.01f);
        damage = 0.015f;

        penetration = 0.5f;
        explosionRadius = 1.5f *radius;
    }

}
