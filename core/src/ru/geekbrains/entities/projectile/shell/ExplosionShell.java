package ru.geekbrains.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.Projectile;
import ru.geekbrains.entities.projectile.frag.PlasmaFragment;

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
        fragTTL = 80;



        explosionPower = 12;
        shapedExplosion = false;
        color = Color.ORANGE;

        if(owner.type.contains(ObjectType.PLAYER_SHIP)) {
            fragTTL = 500;
        }
    }

    protected Projectile createFragment() {


        PlasmaFragment result = new PlasmaFragment(fragSize, fragTrailSize, new Color(0.9f, 0.5f, 0f, 0.8f), owner);
        result.type.remove(ObjectType.PLASMA_FRAG);
        result.damage = 0.1f;
        result.color = new Color(0.9f, 0.5f, 0f, 0.8f);
        result.explosionRadius = 0f;
        return result;

    }
}
