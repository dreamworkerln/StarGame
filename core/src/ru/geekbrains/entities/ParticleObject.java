package ru.geekbrains.entities;


import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Object without world interaction
 */
public abstract class ParticleObject extends GameObject {

    public ParticleObject(float radius) {
        super(radius);
    }
}

