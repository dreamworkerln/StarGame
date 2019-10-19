package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class AntiMissile extends Missile {

    public AntiMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.ANTIMISSILE);

        mass = 0.01f;
        fuel = 4f;

        maxThrottle = 3f;
        throttle = maxThrottle;

        setMaxHealth(0.01f);
        damage = 0.5f;


        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = true;

        proximityMissTargetDistance = 50;

    }

    @Override
    public void update(float dt) {
        super.update(dt);

        
    }

//    @Override
//    protected void guide(float dt) {
//        super.guide(dt);
//
//        if (vel.len() > 700) {
//            throttle = maxThrottle / 2;
//        }
//        else {
//            throttle = maxThrottle;
//        }
//
//    }

}
