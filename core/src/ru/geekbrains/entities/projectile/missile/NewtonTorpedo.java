package ru.geekbrains.entities.projectile.missile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.shell.BlackHoleBlastShell;
import ru.geekbrains.entities.projectile.shell.BlackHoleShell;
import ru.geekbrains.screen.GameScreen;

// work standalone
public class NewtonTorpedo extends AbstractMissile {


    public float plasmaEmpDistribution = 0.5f;
    public int fragCount;

    public NewtonTorpedo(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.GRAVITY_REPULSE_TORPEDO);

        selfdOnNoTargetAvailable = false;

        explosionColor = new Color(0.3f, 0.3f, 0.7f, 0.4f);
        explosionRadius = radius * 10;
        engineTrail.color = Color.GREEN;

        armour = 0.3f;
        mass = 0.3f;
        setMaxThrottle(18f);
        setMaxHealth(20f);
        setMaxRotationSpeed(0.02f);

        setMaxFuel(500);
        fuelConsumption = 1;



        //engineTrail.setRadius(2);
        //damageBurnTrail.setRadius(5);

        damage = 10f;
        fragCount = 100;
        penetration = 0.3f;

        warnReticle = new DrivenObject.WarnReticle(height, this);

        //proximityMinDistance = 0.1f;
        //proximityMinDistanceTime = 0.1f;
    }

    @Override
    protected void guide(float dt) {

        selectTarget();
        super.guide(dt);
    }

    @Override
    public void update(float dt) {

        super.update(dt);

        if (health < maxHealth/4) {

            damageBurnTrail.color = Color.RED;
            acquireThrottle(throttle / 2);
            health -= maxHealth/10000f;

        }

    }


    @Override
    public void dispose() {


        BlackHoleBlastShell flakShell =  new BlackHoleBlastShell(5, 1, Color.GOLD, null);
        flakShell.plasmaEmpDistribution = plasmaEmpDistribution;
        flakShell.pos.set(pos);
        flakShell.vel.set(vel);
        flakShell.fragCount = fragCount;
        flakShell.shapedExplosion = false;
        flakShell.isEmpArmament = true;
        flakShell.explosionPower = 10;
        flakShell.empDamage = 100;
        flakShell.setTTL(1);
        GameScreen.addObject(flakShell);



        BlackHoleShell shell =  new BlackHoleShell(10,  null);
        shell.pos.set(pos);
        shell.vel.set(vel);
        GameScreen.addObject(shell);


        super.dispose();
    }


}
