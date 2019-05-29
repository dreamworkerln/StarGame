package ru.geekbrains.entities.particles;


import ru.geekbrains.entities.objects.GameObject;

/**
 * Object without world interaction
 */
public abstract class ParticleObject extends GameObject {

    public ParticleObject(float radius) {
        super(radius);
    }
}

