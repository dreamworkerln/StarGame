package ru.geekbrains.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.Projectile;

public class Shell extends Projectile {


    public Shell(float height, GameObject owner) {
        super(height, owner);
    }


    public Shell(float height, float trailRadius, GameObject owner) {
        super(height,trailRadius, owner);
    }

    public Shell(float height, float trailRadius, Color color, GameObject owner) {
        super(height,trailRadius, color, owner);
    }

    @Override
    protected void postConstruct() {

        type.add(ObjectType.SHELL);
        mass = 0.016f;
        //mass = 1;

        setMaxHealth(1.1f);
        damage = 1f;

        penetration = 1;
    }



}
