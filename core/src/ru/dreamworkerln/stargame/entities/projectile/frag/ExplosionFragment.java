package ru.dreamworkerln.stargame.entities.projectile.frag;

import com.badlogic.gdx.graphics.Color;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;

public class ExplosionFragment extends Fragment {

    public ExplosionFragment(float height, GameObject owner) {
        super(height, owner);
    }

    public ExplosionFragment(float height, float trailRadius, GameObject owner) {
        super(height, trailRadius, Color.GOLD, owner);
    }

    public ExplosionFragment(float height, float trailRadius, Color traceColor, GameObject owner) {
        super(height, trailRadius, traceColor, owner);
    }

    @Override
    protected void postConstruct() {

        super.postConstruct();

        type.add(ObjectType.EXPLOSION_FRAG);
        explosionRadius = 0;

    }
}
