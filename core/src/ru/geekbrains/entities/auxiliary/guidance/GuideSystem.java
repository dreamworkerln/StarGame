package ru.geekbrains.entities.auxiliary.guidance;

import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.entities.objects.GameObject;

public class GuideSystem {

    protected GameObject owner;
    protected GameObject target;
    public Vector2 vecTarget = new Vector2();       // вектор к цели

    public GuideSystem(GameObject owner) {

        this.owner = owner;
    }


    public void guide(Vector2 target, byte mode) {



          // direction -----------------------
//
//        // vector from pos to target
//        vecTarget.set(target.pos).sub(pos);
//
//        // rotation dynamics -----------------------------------------------------------------------
//
//        // angle between direction and vecTarget
//        float targetAngle = dir.angleRad(vecTarget);
//
//        float doAngle = Math.min(Math.abs(targetAngle), maxRotationSpeed);
//
//        if (targetAngle < 0)
//            doAngle = -doAngle;
//        dir.rotateRad(doAngle);


    }
}
