package ru.geekbrains.entities.objects;

public class Shell extends Projectile {


    public Shell(float height, GameObject owner) {
        super(height, owner);

        type.add(ObjectType.SHELL);
        mass = 0.016f;
        //mass = 1;
    }


}
