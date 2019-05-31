package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Planet extends GameObject {

    public Planet(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        mass = 1000000f;
        this.type.add(ObjectType.PLANET);
    }
}
