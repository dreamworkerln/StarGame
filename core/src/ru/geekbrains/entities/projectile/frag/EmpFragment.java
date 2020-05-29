package ru.geekbrains.entities.projectile.frag;

import com.badlogic.gdx.graphics.Color;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class EmpFragment extends Fragment{



    public EmpFragment(float height, GameObject owner) {
        super(height, owner);
    }

    public EmpFragment(float height, float trailRadius, GameObject owner) {
        super(height, trailRadius, new Color(0.65f, 0.87f, 1, 1), owner);
    }

    public EmpFragment(float height, float trailRadius, Color color, GameObject owner) {
        super(height, trailRadius, color, owner);
    }

    @Override
    protected void postConstruct() {

        super.postConstruct();

        type.add(ObjectType.PLASMA_FRAG);

        explosionRadius = radius * 4;
        //color = new Color(0.25f, 0.57f, 1, 1);
        explosionColor = new Color(0.65f, 0.87f, 1, 0.5f);// new Color(0.25f, 0.57f, 1, 0.5f);

        penetration = 0.1f;
        damage = 0.01f;

        isEmpArmament = true;
        empDamage = 250;
    }
}
