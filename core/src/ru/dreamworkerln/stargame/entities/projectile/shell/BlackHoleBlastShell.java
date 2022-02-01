package ru.dreamworkerln.stargame.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import java.util.concurrent.ThreadLocalRandom;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.projectile.Projectile;
import ru.dreamworkerln.stargame.entities.projectile.frag.EmpFragment;
import ru.dreamworkerln.stargame.entities.projectile.frag.Fragment;
import ru.dreamworkerln.stargame.entities.projectile.frag.PlasmaFragment;

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
