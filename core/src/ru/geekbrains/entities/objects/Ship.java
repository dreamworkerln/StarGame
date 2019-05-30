package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.screen.Renderer;

public abstract class Ship extends DrivenObject {

    public Gun gun;


    public Ship(TextureRegion textureRegion, float height) {
        super(textureRegion, height);

        gun = new Gun(this);
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
