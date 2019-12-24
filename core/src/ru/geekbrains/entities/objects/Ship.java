package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public abstract class Ship extends DrivenObject {

    public Gun gun;


    public Ship(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        setMaxFuel(150f);
        fuel = 100;

        fuelConsumption = 10f;

        this.type.add(ObjectType.SHIP);

        gun = new Gun(radius * 0.3f, this);

        setMaxHealth(3f);
        
        damage = 4f;
    }

    @Override
    public void update(float dt) {

        super.update(dt);

        if (fuel < maxFuel) {
            fuel += 0.05;
        }

    }

    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);

    }


    @Override
    public void dispose() {
        super.dispose();
    }




}
