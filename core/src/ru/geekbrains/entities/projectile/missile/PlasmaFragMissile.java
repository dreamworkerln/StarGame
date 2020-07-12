package ru.geekbrains.entities.projectile.missile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.ParticleObject;
import ru.geekbrains.entities.projectile.Projectile;
import ru.geekbrains.entities.projectile.frag.PlasmaFragment;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public class PlasmaFragMissile extends AbstractMissile{

    private final int fragCount;
    protected boolean shapedExplosion = true;

    //static Texture missileTexture = new Texture("M-45_missile2.png");

    protected float defaultproximityMinDistance;


    public PlasmaFragMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.BASIC_MISSILE);
        type.add(ObjectType.ANTIMISSILE);

        mass = 0.08f;

        fuel = 24;

        damage = 5f;

        setMaxHealth(0.02f);
        setMaxThrottle(8f);
        boost = 700f;

        fragCount = 10;

        selfdOnTargetDestroyed = false;
        canRetarget = true;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = false;



        proximityMinDistance = 200;
        proximitySafeDistance = 150;
        //proximityMissMinGateDistance = 200;
        //proximityMissMaxSelfdDistance = 100;

        defaultproximityMinDistance = proximityMinDistance;

        type.add(ObjectType.PLASMA_FRAG_MISSILE);

        maxRotationSpeed =  0.05f;

        proximityMinDistanceTime = 0.3f;


        engineTrail.color = new Color(1f, 0.8f, 0.2f, 1);
        engineTrail.setRadius(0.8f);

        warnReticle = new DrivenObject.WarnReticle(height, this);
    }


    @Override
    protected void guide(float dt) {

        // стандартное наведение
        super.guide(dt);

        if (this.readyToDispose) {
            return;
        }

        if (target == null || target.readyToDispose) {

            // перед самоликвидацией отворачиваемся от носителя, чтоб не подоравть его случайно
            if(owner!=null && !owner.readyToDispose) {
                guideVector.set(owner.pos).sub(pos).scl(-1).nor().rotate(45);
            }
            return;
        }


        if (target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
            proximityMinDistance = 0;
        }
        else {
            proximityMinDistance = defaultproximityMinDistance;
        }




        // перед подрывом разворот в сторону цели
        if (distToTarget <  proximityMinDistance*2.5  &&
            distToTarget > proximityMinDistance) {

            float maxPrjVel = 500;  // Задаем начальную скорость "тестовой" пули
            BPU.GuideResult gr = pbu.guideMissile(this, target, maxPrjVel, dt);

            tmp0 = gr.guideVector.nor();

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
//        // отключено
//        // взвод направленного подрыва
//        // находимся от носителя дальше безопасного расстояния
//        else if (distToTarget < proximityMinDistance &&
//                 distToCarrier > proximitySafeDistance) {
//
//            //shapedExplosion = true;
//            //readyToDispose = true;
//        }

    }



    @Override
    public void dispose() {



        float power = 10f * 4;

        //PlasmaFragment trash = new PlasmaFragment(6f, 1.5f, new Color(0.3f, 0.7f, 0.3f, 1), owner);
        //trash.setMass(fragCount*trash.getMass()*5*2f); // намного больше изначальной массы ракеты
        //trash.pos.set(pos);
        //trash.vel.set(vel);
        //trash.dir.set(dir);
        //trash.damage = 1;
        //trash.owner = owner;


        //GameScreen.addObject(trash);

        // create fragments
        for (int i = 0; i < fragCount; i++) {

            Projectile frag = new PlasmaFragment(2.1f, 0.5f, new Color(1f, 0.8f, 0.2f, 1), owner);
            frag.setMass(frag.getMass()*4);



            //frag.setTTL(200);

            frag.pos.set(pos);
            frag.vel.set(vel);
            frag.dir.set(dir);
            frag.owner = owner;

            double fromAn;
            double toAn;


            if (shapedExplosion) {
                fromAn = Math.PI / 5;
                toAn = Math.PI / 5;
            } else {
                fromAn = 0;
                toAn = 2 * Math.PI;
            }

            float fi_min = (float) (dir.angleRad() - fromAn);
            float fi_max = (float) (dir.angleRad() + toAn);




            float r = (float) ThreadLocalRandom.current().nextDouble(power - power*0.1f, power);
            //float r = (float) ThreadLocalRandom.current().nextGaussian()*explosionPower*0.05f + explosionPower;
            //float r = explosionPower;
            float fi;

            try {



//                if (drift > 0) {
//                    double gs = ThreadLocalRandom.current().nextGaussian()*drift;
//                    tmp0.rotateRad((float) gs);
//                }



                if (!shapedExplosion) {
                    fi = (float) ThreadLocalRandom.current().nextDouble(fi_min, fi_max);
                }
                else {
                    fi = (float) ThreadLocalRandom.current().nextGaussian()*((fi_max - fi_min)/4f) + dir.angleRad();
                }
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
            //trash.applyForce(tmp0.scl(-1));

            frag.setTTL(ThreadLocalRandom.current().nextLong(400,600));
            GameScreen.addObject(frag);
        }

        warnReticle = null;
        super.dispose();
    }




}
