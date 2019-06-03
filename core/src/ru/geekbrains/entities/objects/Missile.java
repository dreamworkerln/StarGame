package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import java.util.Arrays;

public class Missile extends DrivenObject {


    AimFunction af;
    UnivariateSolver nonBracketing;


    public Missile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.MISSILE);


        setRadius(radius * 5); // fix issued by image aspect ratio

        mass = 0.1f;
        //maxRotationSpeed = 0.02f;
        fuel = 8;

        maxThrottle = 10f;
        throttle = maxThrottle;



        maxHealth = 0.1f;
        health = maxHealth;

        aspectRatio = 1;
        //engineTrail.radius *= 2;
        //damageBurnTrail.radius *= 2;



        final double relativeAccuracy = 1.0e-12;
        final double absoluteAccuracy = 1.0e-8;

        af =  new AimFunction();
        nonBracketing = new BrentSolver(relativeAccuracy, absoluteAccuracy);
    }




    @Override
    protected void guide(float dt) {

        if (target != null && target.readyToDispose) {
            target = null;
        }

        if (target == null) {

            // self-d
            this.readyToDispose = true;
        }



        guideVector.setZero();

        selfGuiding(dt);

        // Самонаведение не сгидродоминировало
        if (target != null && guideVector.isZero()) {

            //guideVector.set(target.pos).sub(pos).nor();
        }





        // ToDo: перенести в GameObject.update()
        // rotation dynamics --------------------------------
        // Aiming
        if (!guideVector.isZero()) {

            // angle between direction and guideVector
            float guideAngle = dir.angleRad(guideVector);

            float doAngle = Math.min(Math.abs(guideAngle), maxRotationSpeed);

            if (guideAngle < 0) {
                doAngle = -doAngle;
            }
            dir.rotateRad(doAngle);
        }

    }



    private static class AimFunction implements UnivariateFunction {

        public double rx, ry, vx, vy, ax, ay, ACC;


        public AimFunction() {

        }

        public double value(double t) {

            double result = 4*Math.pow(rx,2) + 4*Math.pow(ry,2) + (-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(t,4) + 4*Math.pow(t,3)*(ax*vx + ay*vy) +
                    8*t*(rx*vx + ry*vy) + 4*Math.pow(t,2)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2));

            return result;
        }
    }


    public void selfGuiding(float dt) {

        // Система наведения пушек и ракет(самонаведение)
        // https://gamedev.stackexchange.com/questions/149327/projectile-aim-prediction-with-acceleration


        if (target== null || target.readyToDispose)
            return;

        af.ACC = maxThrottle / mass;  // Максимальное возможное ускорение объекта

        double[] root = new double[4];




        // t - target
        // s - object


        //at      -> a
        //rt - rs -> r
        //vt - vs -> v



        // r =  rt - rs
        af.rx = target.pos.x - pos.x;
        af.ry = target.pos.y - pos.y;

        // v =  vt - vs
        af.vx = target.vel.x - vel.x;
        af.vy = target.vel.y - vel.y;

        // apply inverted object acceleration to target
        af.ax = target.acc.x - acc.x;
        af.ay = target.acc.y - acc.y;



        for (int i = 0; i< 1000; i++) {
            try {

                double t = nonBracketing.solve(100, af,  0, dt * i);

                if (!Double.isNaN(t) && !Double.isInfinite(t) && t > 0) {

                    double vs_x = af.rx / t + 0.5 * af.ax * t + af.vx;
                    double vs_y = af.ry / t + 0.5 * af.ay * t + af.vy;

                    guideVector.set((float) vs_x, (float) vs_y).nor();
                    break;
                }
            }catch (Exception ignore) {}

        }








    }

}
