package ru.geekbrains.entities.projectile;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Projectile;

public class Shell extends Projectile {


    public Shell(float height, GameObject owner) {
        super(height, owner);

        type.add(ObjectType.SHELL);
        mass = 0.016f;
        //mass = 1;

        setMaxHealth(1.1f);
        damage = 1f;
    }


}
