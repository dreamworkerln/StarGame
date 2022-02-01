package ru.dreamworkerln.stargame.entities.projectile.missile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;

public class AntiMissile extends AbstractMissile {

    float currentMaxRotationSpeed;

    public AntiMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.ANTIMISSILE);

        explosionRadius = radius * 2;

        mass = 0.01f;
        fuel = 5f;
        throttleStep = 1;

        setMaxThrottle(3.5f);

        setMaxHealth(0.01f);
        damage = 0.5f;
        firePower = 200f;

        setMaxRotationSpeed(0.03f);
        currentMaxRotationSpeed = maxRotationSpeed;


        selfdOnTargetDestroyed = true;
        canRetarget = false;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = true;
        avoidPlanet = false;        

        proximityMissMinGateDistance = 500;
        proximityMissMaxSelfdDistance = 10;

        penetration = 0.1f;
        directGuiding = false;

    }

    @Override
    public void update(float dt) {
        super.update(dt);
    }

    @Override
    protected void guide(float dt) {
        super.guide(dt);

        if (target == null) {
            return;
        }


        if (guideResult != null) {

            tmp0.set(target.vel);
            float relVel = tmp1.set(vel).sub(tmp0).len();


            // включаем режим маневрирования маневровыми движками
            // Если скоро столкновение и скоросить сближения велика
            // (или наведение отъехало)
            if(Double.isNaN(guideResult.impactTime) || guideResult.impactTime < 0.7f && relVel > tmp0.len() * 2) {

                //currentMaxRotationSpeed = (float) (2 * Math.PI);
                //currentMaxRotationSpeed = 0.03f;
                acquireThrottle(maxThrottle/2);

            }
            else {
                currentMaxRotationSpeed = maxRotationSpeed;
                acquireThrottle(maxThrottle);
            }




//            // включаем режим маневрирования маневровыми движками
//            // Если скоро столкновение и скоросить сближения велика
//            //if (guideResult.impactTime < 0.7f && relVel > tmp0.len() * 2) {
//
//            if (guideResult.impactTime < 0.7f) {
//                currentMaxRotationSpeed = (float) (2 * Math.PI);
//
//                //currentMaxRotationSpeed = 0.03f;
//                acquireThrottle(maxThrottle/5);
//
//            }
//            else {
//                currentMaxRotationSpeed = maxRotationSpeed;
//                acquireThrottle(maxThrottle);
//            }

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
