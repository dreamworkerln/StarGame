package ru.dreamworkerln.stargame.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.projectile.Projectile;
import ru.dreamworkerln.stargame.entities.projectile.frag.ExplosionFragment;
import ru.dreamworkerln.stargame.entities.projectile.frag.Fragment;

public class ExplosionShell extends FlakShell {

    public ExplosionShell(float height, GameObject owner) {
        super(height, owner);
    }

    public ExplosionShell(float height, float trailRadius, Color color, GameObject owner) {
        super(height, trailRadius, color, owner);
    }


    @Override
    protected void postConstruct() {

        super.postConstruct();

        type.add(ObjectType.EXPLOSION_FLAK_SHELL);

        fragSize = 2;
        fragTrailSize = 1f;
        damage = 0;
        penetration = 0f;
        fragCount = 30;
        fragTTL = 50;



        explosionPower = 12;
        shapedExplosion = false;
        sphereExplosion = true;
        color = Color.ORANGE;

//        if(owner.getClass() == PlayerShip.class) {
//
//            fragSize = 4;
//            fragCount = 10000;
//            explosionPower = 100;
//            fragTTL = 5000;
//            shapedExplosion = false;
//            sphereExplosion = false;
//        }

    }


    protected Projectile createFragment() {

        Fragment result;
//        if(owner.getClass() == PlayerShip.class) {
//            result = new Fragment(fragSize, new Color(0.9f, 0.5f, 0f, 0.8f), owner);
//            result.color = new Color(0.9f, 0.5f, 0f, 1f);
//            result.setMass(result.getMass()*50);
//            result.penetration = 1;
//            result.damage = 0.3f;
//        }
        //else {
            result = new ExplosionFragment(fragSize, fragTrailSize, new Color(0.9f, 0.5f, 0f, 0.8f), owner);
            result.color = new Color(0.9f, 0.5f, 0f, 0.8f);

        //}
        return result;

    }
}
