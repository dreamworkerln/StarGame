package ru.geekbrains.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.frag.EmpFragment;
import ru.geekbrains.entities.projectile.frag.Fragment;
import ru.geekbrains.entities.projectile.frag.PlasmaFragment;
import ru.geekbrains.entities.projectile.Projectile;

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

        type.add(ObjectType.PLASMA_FLAK_SHELL);

        damage = 1f;
        penetration = 1f;
        fragCount = 6;
        fragTTL = 400;
        explosionPower = 5*4;
        color = Color.GOLD;
    }

    protected Projectile createFragment() {

        PlasmaFragment result = new PlasmaFragment(2, 0.3f, owner);
        result.setMass(result.getMass()*4);

        return result;
    }
}
