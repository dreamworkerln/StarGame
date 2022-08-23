package ru.dreamworkerln.stargame.entities.objects.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.dreamworkerln.stargame.entities.equipment.CompNames;
import ru.dreamworkerln.stargame.entities.equipment.ForceShield;
import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.projectile.missile.EmpMissile;
import ru.dreamworkerln.stargame.entities.projectile.missile.Missile;
import ru.dreamworkerln.stargame.entities.weapons.launchers.MissileLauncher;
import ru.dreamworkerln.stargame.entities.weapons.gun.CourseGun;

public class MainShip extends AbstractAIShip {

    public MainShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        setMaxFuel(300f);
        fuelConsumption /=2;

        //setMaxHealth(4.5f);
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



        ForceShield shield = new ForceShield(this, new Color(0.6f , 0.5f, 0.1f, 1f));
        addComponent(CompNames.FORCESHIELD,shield);

        // re-init all weapons
        init();

        collisionAvoidFilter = o -> o != this && !o.readyToDispose && (o.type.contains(ObjectType.SHIP) && o.side == this.side
                || o.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO));

//        collisionAvoidFilter = o-> o != this && !o.readyToDispose && o.owner != this &&
//                (o.type.contains(ObjectType.SHIP) || o.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO) || o.type.contains(ObjectType.SHELL) || o.type.contains(ObjectType.BASIC_MISSILE));


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

