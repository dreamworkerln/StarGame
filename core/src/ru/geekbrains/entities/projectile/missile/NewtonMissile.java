package ru.geekbrains.entities.projectile.missile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

// located on carrier
public class NewtonMissile extends NewtonTorpedo {

    public NewtonMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.GRAVITY_REPULSE_MISSILE);

        armour = 1;
        engineTrail.color = new Color(0.6f, 0.6f, 0.8f, 1);
        plasmaEmpDistribution = 0f;
        setMaxThrottle(40f);
        //setMaxHealth(20f);
        setMaxRotationSpeed(0.04f);

        selfdOnProximityMiss = false;
        proximityMinDistance = 20f;
        proximityMinDistanceTime = 0.14f;

        reloadTime = 500;
    }

//    // instant death on collision
//    @Override
//    public void doDamage(float amount) {
//        super.doDamage(amount);
//
////        if (amount >=10f) {
////            readyToDispose = true;
////        }
//
//        System.out.println("amount >=10f explode!!! !!!!!");
//
//    }
}
