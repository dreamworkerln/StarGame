package ru.dreamworkerln.stargame.entities.objects.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.dreamworkerln.stargame.entities.equipment.BPU;
import ru.dreamworkerln.stargame.entities.equipment.CompNames;
import ru.dreamworkerln.stargame.entities.equipment.ForceShield;
import ru.dreamworkerln.stargame.entities.equipment.interfaces.WeaponSystem;
import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.projectile.Bullet;
import ru.dreamworkerln.stargame.entities.projectile.shell.FlakShell;
import ru.dreamworkerln.stargame.entities.projectile.shell.PlasmaFlakShell;
import ru.dreamworkerln.stargame.entities.projectile.shell.Shell;
import ru.dreamworkerln.stargame.entities.weapons.FlakCannon;
import ru.dreamworkerln.stargame.entities.weapons.Minigun;
import ru.dreamworkerln.stargame.entities.weapons.gun.CourseGun;
import ru.dreamworkerln.stargame.entities.weapons.launchers.MissileLauncher;

public class BattleShip extends AbstractAIShip {

    public BattleShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.BATTLE_SHIP);
        avoidWallCoeff = 2;

        setMass(10f);
        setMaxHealth(200);
        healthGeneration *= 0.5;

        setMaxFuel(10000f);
        fuelConsumption =10;

        maxThrottle = 500f;

        damage = 5f;
        penetration = 1;
        armour = 1;
        empArmour = 0.5f;
        setMaxRotationSpeed(0.03f);

        //guideSystem = new GuideSystem(this);


        // tuning gun
        CourseGun gun = (CourseGun)componentList.get(CompNames.COURSE_GUN);
        gun.setCalibre(8);
        gun.setFireRate(0.1f);
        gun.drift = 0.03f;
        gun.burst= 2;
        gun.fireRate = 0.1f;
        gun.gunHeatingDelta = 30;
        gun.maxGunHeat = 200;
        gun.coolingGunDelta = 2f;
        gun.ammoProducer.clear();
        gun.addAmmoType(() -> {
            Shell shell = new Shell(gun.getCalibre(), gun.getCalibre()/8, owner);
            //shell.setDamage(shell.getDamage()*2);
            //shell.setMass(bullet.getMass()*2.5f);
            shell.setFirePower(shell.getFirePower()*1.2f);
            //shell.setExplosionRadius(bullet.getExplosionRadius()/3f);
            return shell;
        });


        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);
        //launcher.ammoProducer.clear();
        //launcher.addAmmoType(() -> new FastMissile(new TextureRegion(MissileLauncher.MISSILE_TEXTURE), 1.5f, owner));
        //launcher.addAmmoType(() -> new EmpMissile(new TextureRegion(MissileLauncher.MISSILE_TEXTURE), 2, owner));



        Minigun minigun = new Minigun(4, this);
        minigun.maxRange = 1000;
        minigun.maxTime = 4f;
        minigun.ammoProducer.clear();
        minigun.addAmmoType(() -> {
            Bullet bullet = new Bullet(15, owner);
            bullet.setDamage(bullet.getDamage()*8);
            bullet.setPenetration(0.5f);
            bullet.setMass(bullet.getMass()*1.5f);
            bullet.setFirePower(bullet.getFirePower()*2.5f);
            bullet.setExplosionRadius(bullet.getExplosionRadius()/3f);
            return bullet;
        });

        addComponent(CompNames.MINIGUN,minigun);

        FlakCannon flakCannon = new FlakCannon(10, this);
        flakCannon.fireRate = 0.01f;
        flakCannon.setCalibre(8);
        flakCannon.setFiringMode(FlakCannon.FiringMode.PLASMA_ONLY);
        flakCannon.ammoProducer.clear();

        flakCannon.addAmmoType(() -> {
            PlasmaFlakShell shell = new PlasmaFlakShell(flakCannon.getCalibre(), 2, Color.GOLD, owner);

            shell.setMass(shell.getMass()*3);
            shell.setFirePower(shell.getFirePower()*3.4f);
            shell.fragCount = 12;

            return shell;

        });

        flakCannon.addAmmoType(() -> {
            FlakShell shell = new FlakShell(flakCannon.getCalibre() * 1.5f, 2, Color.RED, owner);
            shell.setMass(shell.getMass()*10);
            shell.setFirePower(shell.getFirePower()*10);
            shell.shapedExplosion = false;
            shell.fragCount = 100;
            shell.setDamage(5);
            shell.setPenetration(0.1f);
            return shell;
        });
        addComponent(CompNames.FLACK_CANNON,flakCannon);


        ForceShield shield = new ForceShield(this, new Color(0.3f , 0.6f, 0.1f, 1f));
        shield.setMaxPower(shield.maxPower*3);
        addComponent(CompNames.FORCESHIELD,shield);


        //AntiMissileLauncher antiLauncher = new AntiMissileLauncher(10, this);
        //addComponent(CompNames.ANTI_LAUNCHER,antiLauncher);


        // re-init all weapons
        init();

        // I'm unstoppable!
        collisionAvoidFilter = o -> false;
    }


    @Override
    protected void guide(float dt) {

        if (this.readyToDispose) {
            return;
        }

        selectTarget(dt);


        WeaponSystem gun = weaponList.get(CompNames.COURSE_GUN);

        // Никуда не целимся
        guideVector.setZero();

        // Останавливаем движок
        acquireThrottle(0);

        // Никуда не стреляем
        gun.stopFire();

        // Уклонение от столкновения
        avoidCollision(dt);

        // Уклонение от падения на планету
        avoidPlanet(dt);




        // ЛИБО Наведение на цель ------------------------------------------------------------------------

        // Если есть цель и мы не уклоняемся (если уклоняемся, то guideVector не Zero)
        if (target != null && guideVector.isZero()) {

            // гидродоминируем с самонаведением пушки

            // скорость снаряда
            float maxVel = gun.getFiringAmmo().getFirePower() / gun.getFiringAmmo().getMass() * dt;
            BPU.GuideResult gr = pbu.guideGun(this, target, maxVel, dt);

            if (!gr.guideVector.isZero()) {
                guideVector.set(gr.guideVector.nor());
            }

            boolean stopFireCourseGun = false;
            // Самонаведение не сгидродоминировало
            if (guideVector.isZero()) {
                guideVector.set(target.pos).sub(pos).nor();
                stopFireCourseGun = true;
            }

            // Acceleration

            if(target != null && !target.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) {
                acquireThrottle(maxThrottle/3);
            }

            // Gun & launcher control

            //launcher.startFire();

            if (target != null && !stopFireCourseGun &&
                Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed*1.0f) {

                gun.startFire();
                //launcher.startFire();
            }
            else {
                gun.stopFire();
                //launcher.stopFire();
            }
        }
    }

}
