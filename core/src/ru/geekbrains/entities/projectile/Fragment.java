package ru.geekbrains.entities.projectile;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class Fragment extends Projectile{

    public Fragment(float height, GameObject owner) {
        super(height, owner);

        type.add(ObjectType.FRAG);
        mass = 0.005f;

        setMaxHealth(0.002f);
        damage = 0.1f;

        //damage = 1;
    }
}
