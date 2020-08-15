package ru.geekbrains.entities.objects.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.missile.EmpMissile;
import ru.geekbrains.entities.projectile.missile.Missile;
import ru.geekbrains.entities.weapons.launchers.MissileLauncher;
import ru.geekbrains.entities.weapons.gun.CourseGun;

public class MainShip extends AbstractAIShip {







    public MainShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        setMaxFuel(300f);
        fuelConsumption /=2;

        setMaxHealth(4.5f);
        damage = getMaxHealth();

        type.add(ObjectType.MAIN_SHIP);

        //guideSystem = new GuideSystem(this);

        //gun.fireRate = 1f;


        // tuning gun
        CourseGun gun = (CourseGun)componentList.get(CompNames.COURSE_GUN);
        gun.setFireRate(0.1f);
        gun.maxGunHeat = 110;
        gun.coolingGunDelta = 1.8f;


        // tuning launcher
        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);
        launcher.addAmmoType(() -> new EmpMissile(new TextureRegion(MissileLauncher.MISSILE_TEXTURE), 2, owner));

        // re-init all weapons
        init();


        //gun.fireRate = 0.1f;
        //gun.fireRate = 0.02f;
        //gun.maxGunHeat = 170;
        //gun.gunHeatingDelta = 65;
        //gun.coolingGunDelta = 2f;
        //minigun = new Minigun(4, this);


    }

    @Override
    protected void guide(float dt) {
        super.guide(dt);


        // треш, оно должно переключаться после выстрела а не на каждый тик
        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);
        if(launcher.getCurrentAmmoType() == EmpMissile.class) {
            launcher.setCurrentAmmoType(Missile.class);
        }
        else {
            launcher.setCurrentAmmoType(EmpMissile.class);
        }

    }

    @Override
    public void dispose() {

        super.dispose();
    }







}

