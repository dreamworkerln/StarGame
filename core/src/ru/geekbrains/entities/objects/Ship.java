package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.screen.Renderer;

public abstract class Ship extends DrivenObject {

    public Gun gun;


    public Ship(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.SHIP);

        gun = new Gun(radius * 0.3f, this);
    }

    @Override
    public void update(float dt) {

        gun.update(dt);

        super.update(dt);
    }

    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);

        gun.draw(renderer);
    }
}
