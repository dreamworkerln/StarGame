package ru.geekbrains.entities.projectile;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

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

        setMaxHealth(0.01f);
        damage = 0.015f;

        penetration = 1f;
        explosionRadius = 1.5f *radius;
    }


}
