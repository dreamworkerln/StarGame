package ru.geekbrains.entities.projectile;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class Fragment extends Projectile  {


    public Fragment(float height, GameObject owner) {
        super(height, owner);

        postConstruct();



    }

    public Fragment(float height, float trailRadius, GameObject owner) {
        super(height, trailRadius, owner);

        postConstruct();
    }



    private void postConstruct() {

        type.add(ObjectType.FRAG);
        mass = 0.001f;
        setMaxHealth(0.002f);
        damage = 0.015f;

    }


}
