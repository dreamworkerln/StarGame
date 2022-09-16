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
        damage = 0.011f;

        penetration = 0.3f;
        explosionRadius = 1.5f *radius;
    }

}
