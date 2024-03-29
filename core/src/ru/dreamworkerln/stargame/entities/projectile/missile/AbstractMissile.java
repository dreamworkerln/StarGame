package ru.dreamworkerln.stargame.entities.projectile.missile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import ru.dreamworkerln.stargame.entities.equipment.BPU;
import ru.dreamworkerln.stargame.entities.objects.DrivenObject;
import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.projectile.Ammo;
import ru.dreamworkerln.stargame.screen.GameScreen;

public class AbstractMissile extends DrivenObject implements Ammo {

    // rocket launching boost thrust
    public float firePower;


    //AimFunction af;
    protected boolean selfdOnTargetDestroyed;
    protected boolean selfdOnNoTargetAvailable;
    protected boolean canRetarget;
    protected boolean selfdOnNoFuel;
    protected boolean selfdOnProximityMiss;

    protected boolean avoidPlanet;

    // минимальная дистанция сближения с целью (которая была зарегистрирована в полете)
    protected float minDistance = Float.MAX_VALUE;

    protected float distToCarrier = Float.MAX_VALUE;
    protected float distToTarget = Float.MAX_VALUE;

    // при промахе при удалении от цели
    // до этой величины происходит подрыв

    // При приближении к цели до этой величины взводится самоуничтожение
    protected float proximityMissMinGateDistance = Float.MAX_VALUE;

    // При удалении от цели в взведеном состоянии на эту величину, производится подрыв
    protected float proximityMissMaxSelfdDistance = 1;

    // подрыв не призводится при расстоянии до носителя меньшим, чем это (дистанция блокировки подрыва до носителя)
    protected float proximitySafeDistance = 0;



    // Начинаются вычисления на дистанционный подрыв при сокращении дистанции до цели меньше этой величины
    // при времени пробного снаряда, меньшего proximityMinDistanceTime производится подрыв
    protected float proximityMinGateDistance = 0;

    // Скорость пробного снаряда для определения времени столкновения
    //protected float proximityMinDistanceVel = 100;

    // Производится дистанционный подрыв при сокращении времени столкновения с целью меньше этой величины
    protected float proximityMinDistanceTime = 1;

    // Производится дистанционный подрыв при сокращении расстояния с целью меньше этой величины
    protected float proximityMinDistance = 0;


    // наводится ли по прямой, если по pbu наведение невозможно
    protected boolean directGuiding = true;

    protected int retargetCount = 0;

    // time needed to prepare missile to launch itself (extends missile launcher self reload time)
    protected int reloadTime = 0;

    // когда последний раз стреляли данным типом боеприпаса
    protected long lastFired = Long.MIN_VALUE;


    // список целей для перенацеливания
    NavigableMap<Float, BPU.GuideResult> impactTimes = new TreeMap<>();

    // текущий результат наведения ракеты на цель
    BPU.GuideResult guideResult;



    public AbstractMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        type.add(ObjectType.MISSILE);

        setRadius(radius * 5); // fix issued by image aspect ratio
        aspectRatio = 1;
        throttleStep = 5;

        firePower = 300;

        //mass = 0.04f;
        //maxRotationSpeed = 0.02f;
        //fuel = 18;

        //boost = 300f;



        //setMaxThrottle(4f);
        //setMaxHealth(0.02f);
        //damage = 5f;

        selfdOnTargetDestroyed = false;
        selfdOnNoTargetAvailable = true;
        canRetarget = true;
        selfdOnNoFuel = true;
        selfdOnProximityMiss = false;
        avoidPlanet = true;
    }


    @Override
    protected void guide(float dt) {

        if (this.readyToDispose) {
            return;
        }

        if (target != null && target.readyToDispose) {
            target = null;
        }


        // EXPERIMENTAL RETARGETING
        if (target == null &&
            canRetarget &&
            retargetCount < Integer.MAX_VALUE) {

            retargetCount ++;

            // search new target
            Predicate<GameObject> filter = t-> (t.type.contains(ObjectType.SHIP) || t.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) &&
                !t.readyToDispose &&
                t != this &&
                t.side != side &&
                (owner == null || (t != owner && t.owner != owner && t.side != owner.side));

            List<GameObject> targets = GameScreen.getCloseObjects(this, 4000, filter);
            impactTimes.clear();


            // leave only BASIC_ENEMY_SHIP in targets;
            //ToDO: implement friend or foe radar recognition system
            // Or all will fire to enemy ships only
//            targets.removeIf(t -> (!t.type.contains(ObjectType.SHIP) && !t.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) ||
//                t.readyToDispose ||
//                t == this ||
//                t.side == side ||
//                owner!=null && (t == owner || t.owner == owner || t.side == owner.side));



            for (GameObject trg : targets) {

                //float maxPrjVel = proximityMinDistanceVel;  // Задаем начальную скорость "тестовой" пули
                float maxPrjVel = vel.len();
                BPU.GuideResult gr = pbu.guideGun(this, trg, maxPrjVel, dt);

                // get results

                float impactTime = (float)gr.impactTime;

                if (!Float.isNaN(impactTime) && impactTime >= 0) {
                    impactTimes.put(impactTime, gr);
                }


            }

            if (impactTimes.size() > 0) {

                minDistance = Float.MAX_VALUE;
                target = impactTimes.firstEntry().getValue().target;
            }
            else if (targets.size() > 0) {

                minDistance = Float.MAX_VALUE;
                target = targets.get(0);

            }
        }

        // END RETARGETING -------------------------


        if (owner != null && !owner.readyToDispose) {
            distToCarrier = tmp0.set(owner.pos).sub(pos).len() - owner.getRadius() - radius;
        }
        else {
            distToCarrier = Float.MAX_VALUE;
        }


        if(target != null && !this.readyToDispose) {
            distToTarget = tmp0.set(target.pos).sub(pos).len() - target.getRadius() - radius;

            if (distToTarget < 0 ) {
                distToTarget = 0;
            }

            // calc new minDistance
            if (distToTarget < minDistance) {
                minDistance = distToTarget;
            }
        }



        // self -d no targets available
        if (selfdOnNoTargetAvailable && target == null) {

            acquireThrottle(0);
            if (distToCarrier > proximitySafeDistance) {
                this.readyToDispose = true;
            }
        }



        // target destroyed - self-d on
        if (selfdOnTargetDestroyed && target == null) {

            acquireThrottle(0);
            if (distToCarrier > proximitySafeDistance) {
                this.readyToDispose = true;
            }
        }

        // no fuel - self-d
        if (fuel <= 0) {

            if (selfdOnNoFuel) {
                this.readyToDispose = true;
            }
        }



        // Self-d on miss target (proximity explosion)
        if (target != null && selfdOnProximityMiss) {

            if (minDistance < proximityMissMinGateDistance &&
                distToTarget - minDistance > proximityMissMaxSelfdDistance &&
                distToCarrier > proximitySafeDistance) {

                this.readyToDispose = true;
            }
        }



//        // Self-d on miss target (proximity explosion)
//        if (target != null && selfdOnProximityMiss) {
//
//            // Промах по цели - дистанция до цели начала расти
//            // Находимся от цели на расстоянии, меньшем proximityMissTargetDistance
//            // находимся от носителя дальше безопасного расстояния
//            if (distToTarget > (minDistance + radius + target.getRadius()) &&
//                    distToTarget < proximityMissTargetDistance &&
//                    distToCarrier > proximitySafeDistance) {
//
//                this.readyToDispose = true;
//            }
//        }


        // explode on min impact time to target
        // находимся от носителя дальше безопасного расстояния
        if (target != null && !this.readyToDispose &&
            distToTarget < proximityMinGateDistance &&
            distToCarrier > proximitySafeDistance) {

            //float maxVel = proximityMinDistanceVel;
            float maxVel = vel.len();
            BPU.GuideResult gr = pbu.guideGun(this, target, maxVel, dt);

            if (gr.impactTime < proximityMinDistanceTime)  {


                if(this instanceof NewtonTorpedo) {
                    System.out.println("explode on min impact time !!!!!");
                }

                this.readyToDispose = true;
            }

//            if (tmp0.isZero()) {
//                tmp0.set(target.pos).sub(pos).nor();
//            }
//            guideVector.set(tmp0);


        }



//        if(this instanceof NewtonTorpedo) {
//            System.out.println(proximityMinDistance);
//        }


        // explode on min distance to target
        // находимся от носителя дальше безопасного расстояния
        if (target != null && !this.readyToDispose &&
            distToTarget < proximityMinDistance &&
            distToCarrier > proximitySafeDistance) {

           // if(this instanceof NewtonTorpedo) {
           //     System.out.println("explode on min distance to target !!!!!");
           //     System.out.println(distToTarget);
           // }

            this.readyToDispose = true;
        }






        //guideVector.setZero();

        //guideVector.setZero();

        if(target != null && !this.readyToDispose) {

            acquireThrottle(maxThrottle);

            // Максимальное возможное ускорение ракеты своим движком с текущей тягой
            float maxAcc = throttle / mass;

            
            // trash code (перенести это все в класс предка?)
            // противоракета может специально сбрасывать тягу при подлете для более точного наведения
            // но в вычислениях PBU надо учитывать максимально возможную а не теущую тягу
            if(this.getClass() == AntiMissile.class) {
                maxAcc = maxThrottle / mass;
            }

            guideResult = pbu.guideMissile(this, target, maxAcc, dt);

            //selfGuiding(dt);

            if (!guideResult.guideVector.isZero()) {
                //tmp0.set(pbu.guideResult.guideVector.nor());
                guideVector.set(guideResult.guideVector.nor());
            }
            else if (directGuiding) {
                guideVector.set(target.pos).sub(pos).nor();
            }

        }
        else {
            acquireThrottle(0);
        }

        if(avoidPlanet) {
            avoidPlanet(dt);
        }
    }



    public float getFirePower() {
        return firePower;
    }


    public void setFirePower(float firePower) {
        this.firePower = firePower;
    }

//    @Override
//    public void copy(GameObject source, GameObject dest) {
//        super.copy(source, dest);
//
//        ((AbstractMissile)dest).firePower= ((AbstractMissile)source).firePower;
//    }

    @Override
    public float getMaxThrottle() {
        return maxThrottle;
    }

    @Override
    public int getReloadTime() {
        return reloadTime;
    }

    public void setReloadTime(int reloadTime) {
        this.reloadTime = reloadTime;
    }

    @Override
    public long getLastFired() {
        return lastFired;
    }

    @Override
    public void setLastFired(long lastFired) {
        this.lastFired = lastFired;
    }
}
