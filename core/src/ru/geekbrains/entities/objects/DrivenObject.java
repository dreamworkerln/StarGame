package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.particles.ParticleObject;
import ru.geekbrains.entities.particles.SmokeTrailList;
import ru.geekbrains.entities.projectile.missile.AbstractMissile;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.entities.particles.SmokeTrail;
import ru.geekbrains.screen.RendererType;

import static ru.geekbrains.screen.GameScreen.INSTANCE;

/**
 * Object with thruster and gyrodine
 */
public abstract class DrivenObject extends GameObject implements SmokeTrailList {

    protected BPU pbu = new BPU();

    public float throttle = 0;                   // current throttle
    public float maxFuel = 100000f;        // maximum fuel tank capacity
    public float fuelGeneration;
    public float maxThrottle = 50f;        // maximum thruster engine force
    public float throttleStep = 10;
    public float requiredThrottle;
    public  boolean engineOnline = true;

    //public Guidance guidance = Guidance.AUTO;

    public GameObject target;                       // цель

    public float fuel = maxFuel;                   // current fuel level
    public float  fuelConsumption = 1;


    protected Vector2 enginePos = new Vector2();         // tail position
    public Vector2 engineTrailPos = new Vector2();    // tail position

    public List<SmokeTrail> smokeTrailList = new ArrayList<>();


    public SmokeTrail engineTrail;                      // trail from thruster burst
    public SmokeTrail damageBurnTrail;                  // trail from burning on damage

    protected long avoidPlanetTick = 0;

    protected WarnReticle warnReticle;

    //protected boolean doAvoidPlanet = false;

    public DrivenObject(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.DRIVEN_OBJECT);

        engineTrail = new SmokeTrail(radius * 0.4f * aspectRatio, new Color(0.5f, 0.5f, 0.5f, 1), this);
        engineTrail.TTL = 50;
        smokeTrailList.add(engineTrail);

        damageBurnTrail = new SmokeTrail(radius * 0.4f * aspectRatio, new Color(0.3f, 0.2f, 0.2f, 1), this);
        damageBurnTrail.TTL = 50;
        damageBurnTrail.speed = 0;
        damageBurnTrail.isStatic = true;
        smokeTrailList.add(damageBurnTrail);

        maxRotationSpeed = 0.05f;

        rendererType.add(RendererType.TEXTURE);
        rendererType.add(RendererType.SHAPE);

        warnReticle = new WarnReticle(height, null);
    }


    /**
     * Perform simulation step
     *
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {


        // ~~~~~~~~~~~~~~
        super.update(dt);
        // ~~~~~~~~~~~~~~

        // auto removing destroyed targets
        if (target == null || target.readyToDispose) {
            target = null;
        }


        // guiding - controlling direction and thrust value
        guide(dt);

        // apply thruster --------------------------------------------------------------------------

        // filter bounds
        requiredThrottle = requiredThrottle > maxThrottle ? maxThrottle : requiredThrottle;
        requiredThrottle = requiredThrottle < 0 ? 0 : requiredThrottle;


        if (throttle < requiredThrottle) {
            throttle += Math.min(maxThrottle/throttleStep, requiredThrottle - throttle);
        }
        else if (throttle > requiredThrottle) {
            throttle -= Math.min(maxThrottle/throttleStep, throttle - requiredThrottle);
        }

        // гасим движок
        if (requiredThrottle == 0 && throttle <= throttleStep*1.5f) {
            throttle = 0;
        }

        // отключаем движок
        if (!engineOnline) {
            throttle = 0;
        }


        // check fuel
        if (fuel <= 0) {
            throttle = 0;
        } else {
            // update fuel
            fuel -= (throttle / maxThrottle * fuelConsumption * dt);
        }

        // apply throttle
        force.add(tmp1.set(dir).nor().scl(throttle));

        // tail vector
        tailVec.set(dir);
        tailVec.scl(-radius * aspectRatio);

        // tail position
        tailPos.set(pos).add(tailVec);


        // engine burst pos
        tmp1.set(tailVec).scl(1.3f);
        enginePos.set(pos).add(tmp1);

        // smoke trace pos
        tmp1.set(tailVec).scl(1.7f);
        engineTrailPos.set(pos).add(tmp1);

        engineTrail.setTrailPos(engineTrailPos);
        damageBurnTrail.setTrailPos(pos);


        for (SmokeTrail st : smokeTrailList) {
            st.update(dt);
        }

        // add smoke trail particle
        if (throttle > 0) {
            engineTrail.add(throttle / maxThrottle);
        }

        // add damage rail particle
        if (health < getMaxHealth()) {
            damageBurnTrail.add((getMaxHealth() - health) / maxHealth);
        }

        warnReticle.update(dt);
    }





    protected abstract void guide(float dt);


    // Уклонение от падения на планету
    protected void avoidPlanet(float dt) {

        //boolean result = false; // avoid maneuver is required

        if (this.readyToDispose) {
            return;
        }


        GameObject planet = GameScreen.INSTANCE.planet;

        float maxPrjVel = vel.len();  // Задаем начальную скорость "тестовой" пули
        BPU.GuideResult gr =  pbu.guideGun(this, planet, maxPrjVel, dt);
        Float impactTime = (float)gr.impactTime;

        //float maxTime = doAvoidPlanet ? planetAvoidImpactTime * 2f : planetAvoidImpactTime;

        //doAvoidPlanet = !impactTime.isNaN() && impactTime >= 0 && impactTime < maxTime;

        long planetAvoidImpactTickTime = (long)(mass/maxThrottle * 2500);


        float minImpactTime = 2f;
        if(this.type.contains(ObjectType.BASIC_MISSILE)) {
            minImpactTime = 1.5f;
        }

        boolean doAvoidPlanet = !impactTime.isNaN() && impactTime >= 0 && impactTime < minImpactTime;

        // set trigger on
        if (doAvoidPlanet && avoidPlanetTick == 0) {
            avoidPlanetTick = GameScreen.INSTANCE.getTick() + planetAvoidImpactTickTime;
        }

        if (avoidPlanetTick > GameScreen.INSTANCE.getTick()) {
            doAvoidPlanet = true;
        }
        else {// set trigger off
            avoidPlanetTick = 0;
        }


        if (doAvoidPlanet) {

            // 1. Корабль летит в сторону планеты ?

            //tmp0.set(planet.pos).sub(pos); // вектор на планету

            //if (Math.abs(vel.angle(tmp0)) < 90) {

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

            //throttle = maxThrottle;

            acquireThrottle(maxThrottle);

//            if (Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed*1.5f) {
//                acquireThrottle(maxThrottle);
//            }
//            else {
//                acquireThrottle(0);
//            }
            //result = true;
            //}
        }




        //return result;
    }


    // ---------------------------------------------------------------------------------------------



    @Override
    public void setRadius(float radius) {
        this.radius = radius;
        this.explosionRadius = radius * 2;

        for (SmokeTrail st : smokeTrailList) {

            st.radius = radius * 0.4f;

            if (this.type.contains(ObjectType.MISSILE)) {
                st.radius = 0.5f;
            }


        }
    }

    public void setMaxThrottle(float value) {
        maxThrottle = value;
        throttle = maxThrottle;
    }

    public void setMaxFuel(float value) {
        maxFuel = value;
        fuel = value;
        fuelGeneration =  maxFuel/2500;
        fuelConsumption = maxFuel/19;
    }

    protected void acquireThrottle(float value) {
        requiredThrottle = value;
    }


    @Override
    public List<SmokeTrail> removeSmokeTrailList() {

        List<SmokeTrail> result = smokeTrailList;
        smokeTrailList = new ArrayList<>();
        return result;
    }

    @Override
    public void stop() {

        for (SmokeTrail trail : smokeTrailList) {
            trail.stop();
        }
    }

    @Override
    public void draw(Renderer renderer) {

        super.draw(renderer);

        if (renderer.rendererType!=RendererType.SHAPE) {
            return;
        }

        // render smoke before ship
        engineTrail.draw(renderer);


        // render ship
        //super.draw(renderer);

        // render engine burst
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        //renderer.shape.begin();
        renderer.shape.set(ShapeRenderer.ShapeType.Filled);

        renderer.shape.setColor(1f, 0.8f, 0.2f, 1);

        renderer.shape.circle(enginePos.x, enginePos.y,
            radius * aspectRatio * 0.3f * (throttle/maxThrottle));


        Gdx.gl.glLineWidth(1);
        //renderer.shape.end();


        // render damage burn after ship
        damageBurnTrail.draw(renderer);



        // Рисуем перекрестье на цели
        warnReticle.draw(renderer);

    }


    @Override
    public void dispose() {

        // do not dispose engineTrail and damageBurnTrail
        // they will be owned by explosion

//        for (SmokeTrail st : smokeTrailList) {
//            st.dispose();
//        }

//        engineTrail = null;
//        damageBurnTrail = null;
//
//        pos = null;
//        vel = null;
//        acc = null;
//        mass = 0;

        warnReticle = null;
        super.dispose();
    }



    protected static class WarnReticle extends ParticleObject {

        private float thickness;

        public WarnReticle(float height, GameObject owner) {
            super(height, owner);

            if(owner == null) {
                return;
            }

            thickness = 0;
            if(owner.type.contains(ObjectType.PLASMA_FRAG_MISSILE)) {
                thickness = 3;
            }
            else if(owner.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
                thickness = 1;
            }
            else if( owner.type.contains(ObjectType.MISSILE_ENEMY_SHIP)) {
                thickness = 2.0f;
            }
        }


        @Override
        public void update(float dt) {

            if(owner == null) {
                return;
            }

            pos = owner.pos;
        }

        @Override
        public void draw(Renderer renderer) {

            if(owner == null) {
                return;
            }

            super.draw(renderer);


            if (renderer.rendererType != RendererType.SHAPE) {
                return;
            }

            if (owner.owner == INSTANCE.playerShip) {
                return;
            }

            ShapeRenderer shape = renderer.shape;

            if (owner.type.contains(ObjectType.MISSILE)) {


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
                Gdx.gl.glLineWidth(thickness);
                shape.flush();
            }
            else {

                float drawRadius = owner.getRadius() * 3f;

                Gdx.gl.glLineWidth(1);
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shape.set(ShapeRenderer.ShapeType.Line);
                shape.set(ShapeRenderer.ShapeType.Line);

                shape.setColor(1f, 1f, 1f, 0.5f);

                tmp0.set(pos).sub(0, drawRadius);
                tmp1.set(pos).add(drawRadius, 0);
                shape.line(tmp0, tmp1);

                tmp0.set(pos).add(drawRadius, 0);
                tmp1.set(pos).add(0, drawRadius);
                shape.line(tmp0, tmp1);

                tmp0.set(pos).add(0, drawRadius);
                tmp1.set(pos).sub(drawRadius, 0);
                shape.line(tmp0, tmp1);

                tmp0.set(pos).sub(drawRadius, 0);
                tmp1.set(pos).sub(0, drawRadius);
                shape.line(tmp0, tmp1);
                Gdx.gl.glLineWidth(thickness);
                shape.flush();
            }

        }
    }





}
