package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Arrays;

public class Missile extends DrivenObject {


    public Missile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.MISSILE);

        mass = 0.1f;
        //maxRotationSpeed = 0.02f;
        fuel = 8;

        maxThrottle = 50f;
        throttle = maxThrottle;



        maxHealth = 0.3f;
        health = maxHealth;

        aspectRatio = 5;
        engineTrail.radius *= 2;
        damageBurnTrail.radius *= 2;
    }




    @Override
    protected void guide() {

        if (target != null && target.readyToDispose) {
            target = null;
        }

        if (target == null) {

            // self-d
            this.readyToDispose = true;
        }



        guideVector.setZero();

        selfGuiding();

        // Самонаведение не сгидродоминировало
        if (target != null && guideVector.isZero()) {

            guideVector.set(target.pos).sub(pos).nor();
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


    public void selfGuiding() {

        // Система наведения пушек и ракет(самонаведение)
        // https://gamedev.stackexchange.com/questions/149327/projectile-aim-prediction-with-acceleration


        if (target== null || target.readyToDispose)
            return;

        double ACC = maxThrottle / mass;  // Максимальное возможное ускорение объекта

        double[] root = new double[4];


        double ax, ay, vx, vy, rx, ry;

        // t - target
        // s - object


        //at      -> a
        //rt - rs -> r
        //vt - vs -> v

        ax = target.acc.x;
        ay = target.acc.y;

        // r =  rt - rs
        rx = target.pos.x - pos.x;
        ry = target.pos.y - pos.y;

        // v =  vt - vs
        vx = target.vel.x - vel.x;
        vy = target.vel.y - vel.y;

        // apply inverted object acceleration to target
        ax -= acc.x;
        ay -= acc.y;

        double ddgzt = 96 * (ax * vx + ay * vy) * (rx * vx + ry * vy);
        double zpzpzp = 64 * (rx * vx + ry * vy);


        // Гидра доминатус !!!!

        root[0] = (ax*vx + ay*vy)/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) - Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) +
                (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                        16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                        (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                        (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))/2. -
                Math.sqrt((8*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) -
                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) -
                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        ((64*Math.pow(ax*vx + ay*vy,3))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),3) + zpzpzp /(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                (64*(ax*vx + ay*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2))/
                                (4.*Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                                3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                        3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))))/2.;




        root[1] = (ax*vx + ay*vy)/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) - Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) +
                (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                        16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                        (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                        (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))/2. +
                Math.sqrt((8*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) -
                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) -
                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        ((64*Math.pow(ax*vx + ay*vy,3))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),3) + zpzpzp /(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                (64*(ax*vx + ay*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2))/
                                (4.*Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                                3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                        3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))))/2.;



        root[2] = (ax*vx + ay*vy)/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) +
                (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                        16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                        (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                        (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))/2. -
                Math.sqrt((8*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) -
                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) -
                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                        ((64*Math.pow(ax*vx + ay*vy,3))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),3) + zpzpzp /(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                (64*(ax*vx + ay*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2))/
                                (4.*Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                                3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                        3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))))/2.;



        root[3] = (ax*vx + ay*vy)/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) +
                (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                        16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                        (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                        (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))/2. +
                Math.sqrt((8*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) -
                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) -
                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                                Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) -
                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),3) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                        ((64*Math.pow(ax*vx + ay*vy,3))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),3) + zpzpzp /(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                (64*(ax*vx + ay*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2))/
                                (4.*Math.sqrt((4*Math.pow(ax*vx + ay*vy,2))/Math.pow(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2),2) + (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(Math.pow(ACC,2) - Math.pow(ax,2) - Math.pow(ay,2)) +
                                        (4*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)))/(3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))) +
                                        (Math.pow(2,0.3333333333333333)*(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt +
                                                16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2)))/
                                                (3.*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                        Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                                3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)) +
                                        Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3) +
                                                Math.sqrt(-4*Math.pow(48*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2)) - ddgzt + 16*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),2),
                                                        3) + Math.pow(1728*(Math.pow(rx,2) + Math.pow(ry,2))*Math.pow(ax*vx + ay*vy,2) + 1728*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(rx*vx + ry*vy,2) -
                                                        1152*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*(Math.pow(rx,2) + Math.pow(ry,2))*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) -
                                                        1152*(ax*vx + ay*vy)*(rx*vx + ry*vy)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2)) + 128*Math.pow(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2),3),2)),0.3333333333333333)/
                                                (3.*Math.pow(2,0.3333333333333333)*(-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))))))/2.;


        Arrays.sort(root);

        for (int i = root.length -1; i >= 0; i--) {

            double t = root[i];

            if (Double.isNaN(t) || Double.isInfinite(t) || t < 0)
                continue;

            double as_x = ax + (2 * (rx + t *vx))/(t*t);
            double as_y = ay + (2 * (ry + t*vy))/(t*t);

            guideVector.set((float)as_x, (float)as_y).nor();

            break;
        }

    }

}
