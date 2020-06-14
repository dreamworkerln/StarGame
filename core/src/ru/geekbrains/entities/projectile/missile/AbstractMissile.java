package ru.geekbrains.entities.projectile.missile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.ParticleObject;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

import static ru.geekbrains.screen.GameScreen.INSTANCE;

public class AbstractMissile extends DrivenObject {

    //protected BPU pbu = new BPU();


    //AimFunction af;
    protected boolean selfdOnTargetDestroyed;
    protected boolean selfdOnNoTargetAvailable;
    protected boolean canRetarget;
    protected boolean selfdOnNoFuel;
    protected boolean selfdOnProximityMiss;

    protected boolean avoidPlanet;

    protected WarnReticle warnReticle;
    public float warnReticleWidth;




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
    protected float proximityMinDistance = 0;

    // Скорость пробного снаряда для определения времени столкновения
    protected float proximityMinDistanceVel = 100;

    // Производится дистанционный подрыв при сокращении времени столкновения с целью меньше этой величины
    protected float proximityMinDistanceTime = 1;


    // наводится ли по прямой, если по pbu наведение невозможно
    protected boolean directGuiding = true;

    protected int retargetCount = 0;



    // rocket launching boost thrust
    public float boost;

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

        warnReticle = new WarnReticle(height, this);
        warnReticleWidth = 1;


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
            List<GameObject> targets = GameScreen.getCloseObjects(this, 2000);
            impactTimes.clear();

            // leave only BASIC_ENEMY_SHIP in targets;
            //ToDO: implement friend or foe radar recognition system
            // Or all will fire to enemy ships only
            targets.removeIf(t -> (!t.type.contains(ObjectType.SHIP) && !t.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) ||
                t.readyToDispose ||
                t == this ||
                owner!=null && (t == owner || t.owner == owner));



            for (GameObject trg : targets) {

                float maxPrjVel = proximityMinDistanceVel;  // Задаем начальную скорость "тестовой" пули
                BPU.GuideResult gr = pbu.guideGun(this, trg, maxPrjVel, dt);

                // get results

                Float impactTime = (float)gr.impactTime;

                if (!impactTime.isNaN() && impactTime >= 0) {
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


        // explode on min distance to target
        // находимся от носителя дальше безопасного расстояния
        if (target != null && !this.readyToDispose &&
            distToTarget < proximityMinDistance &&
            distToCarrier > proximitySafeDistance) {

            float maxVel = proximityMinDistanceVel;
            BPU.GuideResult gr = pbu.guideGun(this, target, maxVel, dt);

            if (gr.impactTime < proximityMinDistanceTime)  {
                this.readyToDispose = true;
            }

//            if (tmp0.isZero()) {
//                tmp0.set(target.pos).sub(pos).nor();
//            }
//            guideVector.set(tmp0);


        }






        //guideVector.setZero();

        //guideVector.setZero();

        if(target != null && !this.readyToDispose) {

            acquireThrottle(maxThrottle);

            // Максимальное возможное ускорение ракеты своим движком
            float maxAcc = maxThrottle / mass;

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


    protected static class WarnReticle extends ParticleObject {

        WarnReticle(float height, GameObject owner) {
            super(height, owner);
        }


        @Override
        public void update(float dt) {

            pos = owner.pos;

        }

        @Override
        public void draw(Renderer renderer) {
            super.draw(renderer);


            if (renderer.rendererType!= RendererType.SHAPE) {
                return;
            }

            if (owner.owner == INSTANCE.playerShip) {
                return;
            }

            ShapeRenderer shape = renderer.shape;

            float drawRadius = owner.getRadius() * 3f;

            Gdx.gl.glLineWidth(1);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shape.set(ShapeRenderer.ShapeType.Line);

            shape.setColor(1f, 1f, 1f, 0.5f);
            shape.circle(pos.x, pos.y, drawRadius);

            tmp0.set(pos).sub(drawRadius, drawRadius);
            tmp1.set(tmp0).set(pos).add(drawRadius, drawRadius);
            shape.line(tmp0, tmp1);

            tmp0.set(pos).sub(-drawRadius, drawRadius);
            tmp1.set(tmp0).set(pos).add(-drawRadius, drawRadius);
            shape.line(tmp0, tmp1);
            Gdx.gl.glLineWidth(((AbstractMissile)owner).warnReticleWidth);
            shape.flush();

        }
    }

}
