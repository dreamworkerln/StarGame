package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.SmokeTrail;
import ru.geekbrains.screen.GameScreen;

public class FragMissile extends Missile{

    private final int fragCount;
    protected boolean shapedExplosion = true;

    static Texture missileTexture = new Texture("M-45_missile2.png");



    public FragMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        //mass = 0.03f;
        mass = 0.08f;

        fuel = 24;

        damage = 1f;
        setMaxHealth(0.02f);
        boost = 700f;

        maxThrottle = 9f;
        throttle = maxThrottle;

        fragCount = 25;

        selfdOnTargetDestroyed = true;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = false;


        proximityMissMinGateDistance = 200;
        proximityMinDistance = 200;
        proximitySafeDistance = 150;
        proximityMissMaxSelfdDistance = 100;

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

        Fragment trash = new Fragment(6f, 1.5f, new Color(0.3f, 0.7f, 0.3f, 1), owner);
        trash.setMass(fragCount*trash.getMass()); // намного больше изначальной массы ракеты
        trash.pos.set(pos);
        trash.vel.set(vel);
        trash.dir.set(dir);
        trash.damage = 1;
        trash.owner = owner;


        GameScreen.addObject(trash);

        // create fragments
        for (int i = 0; i < fragCount; i++) {

            Projectile frag = new PlasmaFragment(4f, 1,new Color(1f, 0.84f, 0f, 1f),  owner);



            //frag.setTTL(200);

            frag.pos.set(pos);
            frag.vel.set(vel);
            frag.dir.set(dir);
            frag.owner = owner;

            double fromAn;
            double toAn;


            if (shapedExplosion) {
                fromAn = Math.PI / 4;
                toAn = Math.PI / 4;
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
