package ru.dreamworkerln.stargame.entities.projectile.shell;

import com.badlogic.gdx.graphics.Color;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.projectile.Projectile;

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

        firePower = 230;

        setMaxHealth(0.1f);
        damage = 1f;
        penetration = 1f;
    }



}
