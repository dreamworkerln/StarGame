package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Planet extends GameObject {

    public Planet(TextureRegion textureRegion, float height) {
        super(textureRegion, height);

        mass = 1000000f;
    }
}
