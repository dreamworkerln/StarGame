package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class PlasmaFragment extends Fragment {

    public PlasmaFragment(float height, GameObject owner) {
        super(height, owner);
    }

    public PlasmaFragment(float height, float trailRadius, Color color, GameObject owner) {
        super(height, trailRadius, color, owner);
    }

    @Override
    protected void postConstruct() {

        super.postConstruct();

        type.add(ObjectType.PLASMA_FRAG);

        explosionRadius = radius * 4;

        penetration = 1f;
        damage = 0.5f;
    }
}


