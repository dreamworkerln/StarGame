package ru.dreamworkerln.stargame.entities.projectile.frag;

import com.badlogic.gdx.graphics.Color;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;

public class PlasmaFragment extends Fragment {

    public PlasmaFragment(float height, GameObject owner) {
        super(height, owner);
    }

    public PlasmaFragment(float height, float trailRadius, GameObject owner) {
        super(height, trailRadius, Color.GOLD, owner);
    }

    public PlasmaFragment(float height, float trailRadius, Color traceColor, GameObject owner) {
        super(height, trailRadius, traceColor, owner);
    }

    @Override
    protected void postConstruct() {

        super.postConstruct();

        type.add(ObjectType.PLASMA_FRAG);

        //color = Color.GOLD;

        explosionRadius = radius * 4;

        penetration = 1f;
        damage = 0.5f;
    }
}


