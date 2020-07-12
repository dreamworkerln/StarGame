package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.equipment.interfaces.AntiLauncherSystem;
import ru.geekbrains.entities.equipment.interfaces.GunSystem;
import ru.geekbrains.entities.equipment.interfaces.WeaponSystem;
import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.screen.Renderer;

public abstract class Ship extends DrivenObject {




    public Ship(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.SHIP);

        setMaxFuel(150f);
        fuel = 100;
        mass = 1;

        healthRegenerationCoefficient = 0.0003f;
        setMaxHealth(3f);
        
        damage = getMaxHealth();
        armour = 1;
        penetration = 1;


        addComponent(CompNames.GUN, new Gun(radius * 0.3f, this));
    }

    @Override
    public void update(float dt) {

        super.update(dt);


        // regenerating fuel
        if (fuel < maxFuel) {
            fuel += fuelGeneration;
        }

        // regenerating health
        if (health < maxHealth) {
            health += healthGeneration;
        }

    }


    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);
    }


    @Override
    public void dispose() {

        for (ShipComponent component : componentList.values()) {
            component.dispose();
        }
        super.dispose();
    }


    // ------------------------------------------------







}
