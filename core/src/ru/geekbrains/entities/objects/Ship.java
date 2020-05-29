package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.screen.Renderer;

public abstract class Ship extends DrivenObject {

    public float fuelGeneration;

    protected Map<CompNames,ShipComponent> componentList = new HashMap<>();
    protected Map<CompNames,WeaponSystem> weaponList = new HashMap<>();


    public Ship(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.SHIP);

        setMaxFuel(150f);
        fuel = 100;
        mass = 1;

        fuelConsumption = 8f;
        fuelGeneration = 0.06f;

        setMaxHealth(3f);
        
        damage = 4f;
        armour = 1;
        penetration = 1;


        addComponent(CompNames.GUN, new Gun(radius * 0.3f, this));
    }

    @Override
    public void update(float dt) {

        super.update(dt);

        for (ShipComponent component : componentList.values()) {
            component.update(dt);
        }

        // regenerating fuel
        if (fuel < maxFuel) {
            fuel += fuelGeneration;
        }
    }

    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);

        for (ShipComponent component : componentList.values()) {
            component.draw(renderer);
        }

    }


    @Override
    public void dispose() {

        for (ShipComponent component : componentList.values()) {
            component.dispose();
        }
        super.dispose();
    }


    // ------------------------------------------------


    protected void addComponent(CompNames name, ShipComponent component) {

        componentList.put(name, component);

        if (component instanceof WeaponSystem) {
            weaponList.put(name, (WeaponSystem)component);
        }
    }

    public WeaponSystem getGun() {

        return weaponList.get(CompNames.GUN);
    }


}
