package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.entities.auxiliary.guidance.GuideSystem;

public class EnemyShip extends Ship {

    GuideSystem guideSystem;

    Vector2 guideVector = new Vector2();
    Vector2 avoidVector = new Vector2();

    public EnemyShip(TextureRegion textureRegion, float height) {
        super(textureRegion, height);

        guideSystem = new GuideSystem(this);
    }


    @Override
    protected void guide() {

        boolean avoidPlanet = false;

        // avoiding falling on planet

        //Расстояние от произвольной точки p до прямой задаётся формулой
        // https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line#Vector_formulation

//        a - вектор pos к-л точки прямой
//        n- единичный вектор вдоль прямой
//
//                x= a + t n
//
//
//        p - pos точка от которой измеряется расстояние до прямой
//
//
//        len = ||(a-p) - ((a-p) . n)n||








        if (target == null) {
            gun.stopFire();
            throttle = 0;
        }
        else {

            throttle = maxThrottle;


//            // turn thruster on only if distance to target greater than kerb.radius
//            if (vecTarget.len() >= 2 * radius) {
//                throttle = maxThrottle;
//            }
//            else {
//                throttle = 0;
//            }


        }
    }
}
