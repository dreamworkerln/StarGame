package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;

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
        boost = 600f;


        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = true;

        proximityMissMinGateDistance = 500;
        proximityMissMaxSelfdDistance = 10;

        //maxRotationSpeed = 0.01f;

        penetration = 0.1f;

    }

    @Override
    public void update(float dt) {
        super.update(dt);
    }

    @Override
    protected void guide(float dt) {
        super.guide(dt);


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
