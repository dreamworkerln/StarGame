package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class PlasmaFlakShell extends FlakShell {
    public PlasmaFlakShell(float height, GameObject owner) {
        super(height, owner);
    }

    public PlasmaFlakShell(float height, float trailRadius, Color color, GameObject owner) {
        super(height, trailRadius, color, owner);
    }

    @Override
    protected void postConstruct() {

        super.postConstruct();

        mass = 0.03f;

        type.add(ObjectType.PLASMA_FLAK_SHELL);
        fragCount = 5;
        fragTTL = 400;
        explosionPower = 5;
    }

    protected  Projectile createFragment() {

        return  new PlasmaFragment(2, 0.5f, Color.GOLD,  owner);

    }
}
