package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;

import ru.geekbrains.entities.objects.GameObject;

public class PlasmaFragment extends Fragment {
    public PlasmaFragment(float height, float trailRadius, Color color, GameObject owner) {
        super(height, trailRadius, color, owner);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();

        explosionRadius = radius * 4;

        penetration = 1f;
        damage = 0.5f;

    }
}


