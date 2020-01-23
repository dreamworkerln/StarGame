package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;

public class NewtonMissile extends Missile {
    public NewtonMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.GRAVITY_REPULSE_MISSILE);

        engineTrail.color = new Color(0.6f, 0.6f, 0f, 1);
        damage = 0.5f;
        explosionRadius = radius * 10;

        setMass(0.1f);
        setMaxThrottle(maxThrottle * 1.7f*2);
        //maxRotationSpeed = 0.02f;
        setMaxHealth(10*2);
        //setMaxThrottle(3);
        fuel = 1000;

        engineTrail.setRadius(2);
        damageBurnTrail.setRadius(5);

        damage = 10;
    }

    @Override
    protected void guide(float dt) {

        super.guide(dt);

        GameObject planet = GameScreen.INSTANCE.planet;


        // Уклонение от падения на планету ---------------------------------------------------------

        // 1. Корабль летит в сторону планеты ?

        tmp0.set(planet.pos).sub(pos); // вектор на планету

        float distToPlanet = tmp0.len();

        if (Math.abs(vel.angle(tmp0)) < 90) {


            // Расстояние от прямой, построенной на векторе скорости корабля до планеты
            // Минимальное сближение с планетой
            tmp0.set(pos).add(vel).nor(); // вектор прямой
            float minConvergence = Intersector.distanceLinePoint(pos.x, pos.y, tmp0.x, tmp0.y,
                    planet.pos.x,
                    planet.pos.y);


            float impactTime = distToPlanet / vel.len();

            // Если минимальное сближение меньше диаметра планеты и время сближения (меньше n)
            if (minConvergence < 2 *planet.radius &&
                    impactTime < 5 * (vel.len()/40f) &&      //6
                    distToPlanet  < 400f + planet.radius) {
                //distToPlanet  < 40f + planet.radius) {

                // необходимо совершить маневр уклонения

                tmp0.set(planet.pos).sub(pos); // вектор на планету

                // слева или справа планета от вектора скорости
                float angle = tmp1.set(vel).angle(tmp0);

                // планета слева от вектора скорости
                if (angle > 0) {
                    guideVector.set(vel).rotate(-90).nor();
                } else {
                    // планета справа от вектора скорости
                    guideVector.set(vel).rotate(90).nor();
                }

                throttle = maxThrottle;

                // Acceleration

//                if (Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {
//                    throttle = maxThrottle;
//                }
//                else {
//                    throttle = 0;
//                }

            }
        }



    }



    @Override
    public void dispose() {

        List<GameObject> targets = GameScreen.getCloseObjects(this, 400);

        targets.forEach(o -> {

            tmp1.set(o.pos).sub(pos);
            //tmp2.set(tmp1).nor().scl(o.getMass());
            tmp2.set(tmp1).nor().scl(2500000f * o.getMass() * 1/tmp1.len());
            if (ThreadLocalRandom.current().nextBoolean()) {
                tmp2.scl(-1);
            }

            o.applyForce(tmp2);
        });


        PlasmaFlakShell flakShell =  new PlasmaFlakShell(5, 1, Color.GOLD, null);
        flakShell.fragCount = 50;
        flakShell.shapedExplosion = false;
        flakShell.pos.set(pos);
        flakShell.vel.set(vel);
        flakShell.readyToDispose = true;
        flakShell.isEmpOrdinance = true;


        GameScreen.addObject(flakShell);

        super.dispose();
    }


}
