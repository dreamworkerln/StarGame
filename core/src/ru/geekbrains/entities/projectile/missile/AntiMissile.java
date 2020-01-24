package ru.geekbrains.entities.projectile.missile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.missile.Missile;

public class AntiMissile extends Missile {

    float maxRotationSpeedOld;

    public AntiMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.ANTIMISSILE);

        explosionRadius = radius * 2;

        mass = 0.01f;
        fuel = 4f;

        maxThrottle = 3f;
        throttle = maxThrottle;

        setMaxHealth(0.01f);
        damage = 0.5f;
        boost = 600f;


        selfdOnTargetDestroyed = true;
        canRetarget = false;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = true;

        proximityMissMinGateDistance = 500;
        proximityMissMaxSelfdDistance = 10;

        penetration = 0.1f;

        maxRotationSpeedOld = maxRotationSpeed;

    }

    @Override
    public void update(float dt) {
        super.update(dt);
    }

    @Override
    protected void guide(float dt) {
        super.guide(dt);



        if (pbu.guideResult != null) {


            // включаем режим маневрирования маневровыми движками
            if (pbu.guideResult.impactTime < 0.6f) {
//                // angle between direction and guideVector
//                float guideAngle = dir.angleRad(guideVector);
//                float doAngle = Math.min(Math.abs(guideAngle), maxRotationSpeed);


                maxRotationSpeed = (float) (2 * Math.PI);
                throttle = maxThrottle/6;



            }
            else {

                maxRotationSpeed = maxRotationSpeedOld;
                throttle = maxThrottle;
            }

        }



        /*
        // search new target
        List<GameObject> targets = GameScreen.getCloseObjects(this, this.radius*5);

        // leave only OWNER_MISSILES in targets;
        //ToDO: implement friend or foe radar recognition system
        // Or all will fire to enemy ships only
        targets.removeIf(t -> (!t.type.contains(ObjectType.MISSILE)));

        //targets.removeIf(t -> (t.owner != this.owner || !t.type.contains(ObjectType.MISSILE) || t.type.contains(ObjectType.ANTIMISSILE) ));

        if(targets.size()>0) {

            GameObject t = targets.get(0);

            tmp0.set(t.pos).sub(pos).nor();

            float r1 = vel.angle(tmp0);

            if (r1 >= 0 && r1 < 45) {
                guideVector.set(tmp0).rotate90(1);
            }
            else if (r1 < 0 && r1 > -45) {
                guideVector.set(tmp0).rotate90(-1);
            }
        }
        */

    }

}
