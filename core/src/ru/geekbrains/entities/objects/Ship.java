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

    protected Map<CompNames,ShipComponent> componentList = new HashMap<>();
    protected Map<CompNames, WeaponSystem> weaponList = new HashMap<>();


    public Ship(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.SHIP);

        setMaxFuel(150f);
        fuel = 100;
        mass = 1;

        healthRegenerationCoefficient = 0.0003f;
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

        // regenerating health
        if (health < maxHealth) {
            health += healthGeneration;
        }

    }

    @Override
    public void rotate() {
        super.rotate();

        //rotate weapons(turrets, cannons in towers) with ship
        for (WeaponSystem ws : weaponList.values()) {
            ws.rotate();
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

    public GunSystem getGun() {
        return (GunSystem) weaponList.get(CompNames.GUN);
    }

    public WeaponSystem getLauncher() {
        return weaponList.get(CompNames.LAUNCHER);
    }

    public AntiLauncherSystem getAntiLauncher() {
        return (AntiLauncherSystem)weaponList.get(CompNames.ANTI_LAUNCHER);
    }




}
