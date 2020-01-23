package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;

import java.util.concurrent.ThreadLocalRandom;

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

        type.add(ObjectType.PLASMA_FLAK_SHELL);
        fragCount = 6;
        fragTTL = 400;
        explosionPower = 5;
    }

    protected  Projectile createFragment() {

        PlasmaFragment result;


        boolean isEmp = false;
        Color color = Color.GOLD;

        if (isEmpOrdinance && ThreadLocalRandom.current().nextFloat() > 0.7) {
            isEmp = true;
            color = new Color(0.65f, 0.87f, 1, 1);
        }

        result =  new PlasmaFragment(2, 0.3f, color,  owner);
        result.isEmpOrdinance = isEmp;
        return  result;
    }
}
