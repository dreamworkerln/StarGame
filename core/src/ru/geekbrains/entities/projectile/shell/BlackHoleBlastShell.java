package ru.geekbrains.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.projectile.Projectile;
import ru.geekbrains.entities.projectile.frag.EmpFragment;
import ru.geekbrains.entities.projectile.frag.Fragment;
import ru.geekbrains.entities.projectile.frag.PlasmaFragment;

public class BlackHoleBlastShell extends PlasmaFlakShell {

    public float plasmaEmpDistribution = 0.5f;

    public BlackHoleBlastShell(float height, GameObject owner) {
        super(height, owner);
    }

    public BlackHoleBlastShell(float height, float trailRadius, Color color, GameObject owner) {
        super(height, trailRadius, color, owner);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();
    }

    @Override
    protected Projectile createFragment() {

        Fragment result;

        if (isEmpArmament && ThreadLocalRandom.current().nextFloat() > plasmaEmpDistribution) {
            result =  new EmpFragment(2, 0.3f,  owner);
        }
        else {
            result =  new PlasmaFragment(2, 0.3f, owner);
        }
        return result;
    }


}
