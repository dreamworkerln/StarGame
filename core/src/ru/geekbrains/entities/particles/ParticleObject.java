package ru.geekbrains.entities.particles;


import ru.geekbrains.entities.objects.GameObject;

/**
 * Object without world interaction
 */
public abstract class ParticleObject extends GameObject {

    public ParticleObject(float height, GameObject owner) {
        super(owner, height);
    }

    public ParticleObject(GameObject owner) {
        super(owner);
    }
}

