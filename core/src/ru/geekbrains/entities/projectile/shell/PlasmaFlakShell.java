package ru.geekbrains.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.frag.PlasmaFragment;
import ru.geekbrains.entities.projectile.Projectile;

public class PlasmaFlakShell extends FlakShell {

    public float fragTrailSize;

    public PlasmaFlakShell(float height, GameObject owner) {
        super(height, owner);
    }

    public PlasmaFlakShell(float height, float trailRadius, Color color, GameObject owner) {
        super(height, trailRadius, color, owner);
    }


    @Override
    protected void postConstruct() {

        super.postConstruct();

        type.add(ObjectType.PLASMA_FLAK_SHELL);

        firePower = 150;

        fragSize = 2.1f;
        fragTrailSize = 0.5f;
        damage = 1f;
        penetration = 1f;
        fragCount = 6;
        fragTTL = 400;
        explosionPower = 5*4;
        fragTrailSize = fragSize * 0.4f;
        color = Color.GOLD;
    }

    protected Projectile createFragment() {

        PlasmaFragment result = new PlasmaFragment(fragSize, 0.5f, owner);
        result.setMass(result.getMass()*4);

        return result;
    }
}
