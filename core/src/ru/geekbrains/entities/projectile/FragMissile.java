package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.SmokeTrail;
import ru.geekbrains.screen.GameScreen;

public class FragMissile extends Missile{

    private final int fragCount;
    protected boolean shapedExplosion = true;

    public FragMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        //mass = 0.03f;

        fuel = 12;

        damage = 1f;
        setMaxHealth(0.05f);
        boost = 700;

        maxThrottle = 15f;
        throttle = maxThrottle;

        fragCount = 500;
        //fragCount = 50;

        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = false;


        proximityMinDistance = 100;
        proximitySafeDistance = 150;
        proximityMissTargetDistance = 100;

        type.add(ObjectType.FRAGMISSILE);

        maxRotationSpeed = 0.1f;



        this.engineTrail.color = new Color(0.7f, 0.2f, 0.2f, 1);
    }


    @Override
    protected void guide(float dt) {

        // стандартное наведение
        super.guide(dt);

        
        if (target == null || target.readyToDispose) {

            // перед самоликвидацией отворачиваемся от носителя, чтоб не подоравть его случайно
            if(owner!=null && !owner.readyToDispose) {
                guideVector.set(owner.pos).sub(pos).scl(-1).nor().rotate(45);
            }

            return;
        }

        // перед подрывом разворот в сторону цели
        if (distToTarget <  proximityMinDistance*2.5  &&
                distToTarget > proximityMinDistance) {

            float maxPrjVel = 500;  // Задаем начальную скорость "тестовой" пули
            pbu.guideMissile(this, target, maxPrjVel, dt);

            tmp0 = pbu.guideResult.guideVector.nor();

            if (tmp0.isZero()) {
                tmp0.set(target.pos).sub(pos).nor();
            }
            guideVector.set(tmp0);


//            // get results
//
//            Float impactTime = (float)pbu.guideResult.impactTime;
//
//            if (!impactTime.isNaN() && impactTime >= 0) {
//                impactTimes.put(impactTime, pbu.guideResult.clone());
//            }





            

        }
        // отключено
        // взвод направленного подрыва
        // находимся от носителя дальше безопасного расстояния
        else if (distToTarget < proximityMinDistance &&
                 distToCarrier > proximitySafeDistance) {

            //shapedExplosion = true;
            //readyToDispose = true;
        }

    }



    @Override
    public void dispose() {

        

        float power = 10f;
        //float power = 100f;

        Fragment trash = new Fragment(4f, true, owner);
        trash.setMass(fragCount*trash.getMass()); // намного больше изначальной массы ракеты
        trash.pos.set(pos);
        trash.vel.set(vel);
        trash.dir.set(dir);
        trash.damage = 1;
        trash.owner = owner;


        GameScreen.addObject(trash);

        // create fragments
        for (int i = 0; i < fragCount; i++) {


            Projectile frag = new Fragment(2f, owner);
            //Projectile frag = new Fragment(2f, true, owner);

            //frag.setTTL(200);

            frag.pos.set(pos);
            frag.vel.set(vel);
            frag.dir.set(dir);
            frag.owner = owner;

            double fromAn;
            double toAn;

            //shapedExplosion = false;


            if (shapedExplosion) {
                fromAn = Math.PI / 6;
                toAn = Math.PI / 6;
            } else {
                fromAn = 0;
                toAn = 2 * Math.PI;
            }

            float fi_min = (float) (dir.angleRad() - fromAn);
            float fi_max = (float) (dir.angleRad() + toAn);




            float r = (float) ThreadLocalRandom.current().nextDouble(power - power*0.1f, power);
            //float r = (float) ThreadLocalRandom.current().nextGaussian()*power*0.05f + power;
            //float r = power;
            float fi;

            try {

                fi = (float) ThreadLocalRandom.current().nextDouble(fi_min, fi_max);
            }
            catch(Exception e) {
                System.out.println(dir);
                System.out.println(fi_min);
                System.out.println(fi_max);
                System.out.println(e);
                // гениально
                throw e;
            }


            float x = (float) (r * Math.cos(fi));
            float y = (float) (r * Math.sin(fi));

            tmp0.set(x, y); // force
            frag.applyForce(tmp0);          // apply force applied to frag
            trash.applyForce(tmp0.scl(-1));

            frag.setTTL(ThreadLocalRandom.current().nextLong(400,600));
            GameScreen.addObject(frag);
        }

        super.dispose();
    }




}
