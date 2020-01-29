package ru.geekbrains.entities.projectile.missile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.shell.BlackHoleShell;
import ru.geekbrains.entities.projectile.shell.PlasmaFlakShell;
import ru.geekbrains.screen.GameScreen;

public class NewtonMissile extends Missile {



    public int fragCount;

    public NewtonMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.GRAVITY_REPULSE_MISSILE);

        selfdOnNoTargetAvailable = false;

        explosionColor = new Color(0.3f, 0.3f, 0.7f, 0.4f);
        explosionRadius = radius * 10;
        engineTrail.color = Color.GREEN;

        armour = 1;

        damage = 0.5f;

        setMass(0.1f);
        setMaxThrottle(6f);
        setMaxHealth(20f);
        fuel = 100;

        engineTrail.setRadius(2);
        damageBurnTrail.setRadius(5);

        damage = 10f;

        fragCount = 100;
    }


    @Override
    public void update(float dt) {
        super.update(dt);

        if (health < maxHealth/4) {


            damageBurnTrail.color = Color.RED;

            throttle = maxThrottle / 2;

        }

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
                    impactTime < 6 * (vel.len()/40f) &&      //6
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

                if (Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {
                    throttle = maxThrottle;
                }
                else {
                    throttle = 0;
                }

            }
        }



    }



    @Override
    public void dispose() {


        PlasmaFlakShell flakShell =  new PlasmaFlakShell(5, 1, Color.GOLD, null);
        flakShell.pos.set(pos);
        flakShell.vel.set(vel);
        flakShell.fragCount = fragCount;
        flakShell.shapedExplosion = false;
        flakShell.isEmpOrdinance = true;
        flakShell.explosionPower = 10;
        flakShell.empDamage = 100;
        flakShell.setTTL(1);
        GameScreen.addObject(flakShell);



        BlackHoleShell shell =  new BlackHoleShell(10,  null);
        shell.pos.set(pos);
        shell.vel.set(vel);
        GameScreen.addObject(shell);


        super.dispose();
    }


}
