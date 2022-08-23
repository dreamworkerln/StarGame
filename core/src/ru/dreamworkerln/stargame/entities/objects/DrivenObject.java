package ru.dreamworkerln.stargame.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import com.badlogic.gdx.utils.OrderedMap;
import ru.dreamworkerln.stargame.entities.equipment.BPU;
import ru.dreamworkerln.stargame.entities.equipment.CompNames;
import ru.dreamworkerln.stargame.entities.equipment.ForceShield;
import ru.dreamworkerln.stargame.entities.equipment.interfaces.AntiLauncherSystem;
import ru.dreamworkerln.stargame.entities.equipment.interfaces.GunSystem;
import ru.dreamworkerln.stargame.entities.equipment.interfaces.WeaponSystem;
import ru.dreamworkerln.stargame.entities.particles.ParticleObject;
import ru.dreamworkerln.stargame.entities.particles.SmokeTrailList;
import ru.dreamworkerln.stargame.entities.projectile.Ammo;
import ru.dreamworkerln.stargame.screen.GameScreen;
import ru.dreamworkerln.stargame.screen.Renderer;
import ru.dreamworkerln.stargame.entities.particles.SmokeTrail;
import ru.dreamworkerln.stargame.screen.RendererType;

/**
 * Object with thruster and gyrodine
 */
public abstract class DrivenObject extends GameObject implements SmokeTrailList {

    protected Map<CompNames,ShipComponent> componentList = new HashMap<>();
    protected Map<CompNames, WeaponSystem> weaponList = new HashMap<>();

    protected List<GameObject> targetList = new ArrayList<>();


    protected Long stunEnd = null; // in ticks
    boolean shouldBlowup = false; // no component recovery

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

    public boolean switchToClosestTarget =  true; // can switch to the closest target

    public float fuel = maxFuel;                   // current fuel level
    public float  fuelConsumption = 1;


    protected Vector2 enginePos = new Vector2();         // tail position
    public Vector2 engineTrailPos = new Vector2();    // tail position

    public List<SmokeTrail> smokeTrailList = new ArrayList<>();


    public SmokeTrail engineTrail;                      // trail from thruster burst
    public SmokeTrail damageBurnTrail;                  // trail from burning on damage

    //protected long avoidPlanetTick = 0;

    protected WarnReticle warnReticle;

    protected DummyObject dummy;

    // fragments fly away on explosion
    //protected int fragmentCount = 50;

    //protected boolean doAvoidPlanet = false;

    public DrivenObject(TextureRegion textureRegion, float height, GameObject owner) {
        super(owner, textureRegion, height);

        this.type.add(ObjectType.DRIVEN_OBJECT);

        engineTrail = new SmokeTrail(radius * 0.4f * aspectRatio, new Color(0.5f, 0.5f, 0.5f, 1), this);
        engineTrail.TTL = 50;
        smokeTrailList.add(engineTrail);

        damageBurnTrail = new SmokeTrail(radius * 0.4f * aspectRatio, new Color(0.3f, 0.2f, 0.2f, 1), this);
        damageBurnTrail.TTL = 50;
        damageBurnTrail.speed = 0;
        damageBurnTrail.isStatic = true;
        smokeTrailList.add(damageBurnTrail);

        setMaxRotationSpeed(0.05f);

        rendererType.add(RendererType.TEXTURE);
        rendererType.add(RendererType.SHAPE);

        warnReticle = new WarnReticle(height, null);
        dummy = new DummyObject(this);
    }


    /**
     * Perform simulation step
     *
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {


        super.update(dt);

        for (ShipComponent component : componentList.values()) {
            component.update(dt);
        }



        // auto removing destroyed targets
        if (target == null || target.readyToDispose) {
            target = null;
        }


        // guiding - controlling direction and thrust value
        guide(dt);

        // apply thruster --------------------------------------------------------------------------

        // filter bounds
        requiredThrottle = Math.min(requiredThrottle, maxThrottle);
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

        if(type.contains(ObjectType.BATTLE_SHIP)) {
            tailVec.scl(0.9f);
        }


        // tail position
        //tailPos.set(pos).add(tailVec);




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


        // врубаем обратно компоненты после EMP удара по истечении врмени на восстановление
        if (stunEnd!= null && GameScreen.INSTANCE.getTick() > stunEnd) {
            stunEnd = null;
            enableComponents(true);
        }

    }


    @Override
    public void rotate(float dt) {
        super.rotate(dt);

        //rotate weapons(turrets, cannons in towers) with ship
        for (WeaponSystem ws : weaponList.values()) {
            ws.rotate(dt);
        }
    }


    /**
     * Used only for ship-alike units
     */
    protected void selectTarget(float dt) {

        if(target != null && target.readyToDispose) {
            target = null;
        }

        // select closest(distance) target
        if(switchToClosestTarget) {
            target = null;
        }

        if(target == null) {

            // leave only ships and missiles
            Predicate<GameObject> filter = o -> o != this && o.owner != this && o.side != this.side &&
                (o.type.contains(ObjectType.SHIP) || o.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO) ||
                    o.type.contains(ObjectType.PLAYER_KERMAN));

            targetList = GameScreen.getCloseObjects(this, 4000, filter);


//        targetList.removeIf(o -> o == this || o.owner == this || o.readyToDispose || o.side == this.side ||
//            !o.type.contains(ObjectType.SHIP) && !o.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO));

            //target = null;


            if(getCourseGun() != null) {

                Ammo currentAmmo = getCourseGun().getFiringAmmo();
                float maxPrjVel = currentAmmo.getFirePower() / currentAmmo.getMass() * dt;
                NavigableMap<Float, GameObject> impactTimes = new TreeMap<>();
                targetList.forEach(obj -> {
                    BPU.GuideResult gr = pbu.guideGun(this, obj, maxPrjVel, dt);
                    impactTimes.put((float) gr.impactTime, obj);
                });


                if (impactTimes.size() > 0) {
                    if (impactTimes.firstEntry().getValue().type.contains(ObjectType.PLAYER_KERMAN)) {

                        if (!(ThreadLocalRandom.current().nextFloat() >= 0.9992)) {
                            impactTimes.remove(impactTimes.firstKey());
                        }
                    }
                    if (impactTimes.size() > 0) {
                        target = impactTimes.firstEntry().getValue();
                    }
                }
            }
            else {
                if(targetList.size() > 0) {
                    target = targetList.get(0);
                }
            }
        }
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


        tmp1.set(planet.pos).sub(pos);
        float scl = (tmp1.len() - (planet.radius + radius))/tmp1.len();
        tmp1.scl(scl);
        tmp2.set(tmp1).add(pos);
        dummy.pos.set(tmp2);

        BPU.GuideResult gr =  pbu.guideGun(this, dummy, maxPrjVel, dt);
        Float impactTime = (float)gr.impactTime;

        float angle;

        // Time needed to Rotate to avoid planet
        tmp0.set(planet.pos).sub(pos);
        angle = tmp1.set(vel).angleDeg(tmp0);
        if (angle > 0) {
            tmp1.set(vel).rotateDeg(-75).nor();   // tmp1 - targeted rotate angle
        } else {
            tmp1.set(vel).rotateDeg(75).nor();
        }

        // Время, требуемое на поворот до положения избегания падения на планету
        float rotateTime = Math.abs(dir.angleRad(tmp1))/ maxRotationSpeed * dt;

        //System.out.println("impactTime: " +  impactTime + ", rotateTime: " + rotateTime);

        if(!Float.isNaN(impactTime) && impactTime > rotateTime) {
            impactTime -= rotateTime;
        }


        //long planetAvoidImpactTickTime = (long)(mass/maxThrottle * 1500);


        float minImpactTime = 1.5f;

        if(this.type.contains(ObjectType.MISSILE)) {
            minImpactTime = 0.4f;
        }
        if(this.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) {
            minImpactTime = 1.8f;
        }
        if(this.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
            minImpactTime = 0.7f;
        }
        if(this.type.contains(ObjectType.BATTLE_SHIP)) {
            minImpactTime = 2.4f;
        }

        boolean doAvoidPlanet = !impactTime.isNaN() && impactTime >= 0 && impactTime < minImpactTime;

        /*
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
        */

        if (doAvoidPlanet) {

            // 1. Корабль летит в сторону планеты ?

            //tmp0.set(planet.pos).sub(pos); // вектор на планету

            //if (Math.abs(vel.angle(tmp0)) < 90) {

            // необходимо совершить маневр уклонения

            tmp0.set(planet.pos).sub(pos); // вектор на планету

            // слева или справа планета от вектора скорости
            angle = tmp1.set(vel).angle(tmp0);

            // планета слева от вектора скорости
            if (angle > 0) {
                guideVector.set(vel).rotate(-75).nor();   // -
            } else {
                // планета справа от вектора скорости
                guideVector.set(vel).rotate(75).nor();
            }

            //throttle = maxThrottle;

            //acquireThrottle(maxThrottle);

            if (Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed*1.5f) {
                acquireThrottle(maxThrottle);
            }
            else {
                acquireThrottle(0);
            }
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
                st.radius = radius * 0.1f;
            }
            else if (this.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) {
                st.radius = radius * 0.2f;
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


        for (ShipComponent component : componentList.values()) {
            component.draw(renderer);
        }

    }




    protected void addComponent(CompNames name, ShipComponent component) {

        componentList.put(name, component);

        if (component instanceof WeaponSystem) {
            weaponList.put(name, (WeaponSystem)component);
        }
        component.init();
    }

    /**
     * Re-init components
     */
    protected void init() {

        for (ShipComponent component : componentList.values()) {
            component.init();
        }
    }


    public GunSystem getCourseGun() {
        return (GunSystem) weaponList.get(CompNames.COURSE_GUN);
    }

    public WeaponSystem getLauncher() {
        return weaponList.get(CompNames.LAUNCHER);
    }

    public AntiLauncherSystem getAntiLauncher() {
        return (AntiLauncherSystem)weaponList.get(CompNames.ANTI_LAUNCHER);
    }


    // enable/disable components
    public void enableComponents(boolean enable) {

        if(shouldBlowup) {
            return;
        }

        for (ShipComponent component : componentList.values()) {
            component.enable(enable);
        }

        engineOnline = enable;

        maxRotationSpeed = enable ? factoryMaxRotationSpeed : 0;
        healthRegenerationCoefficient = enable ? factoryHealthRegenerationCoefficient : 0;
    }


    public void empStun(long tickCount) {

        if(stunEnd != null) {
            stunEnd += tickCount;
        }
        else {
            stunEnd = GameScreen.INSTANCE.getTick() + tickCount;
        }

        enableComponents(false);
    }

    public boolean isShouldBlowup() {
        return shouldBlowup;
    }

    @Override
    public void dispose() {

        for (ShipComponent component : componentList.values()) {
            component.dispose();
        }

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


// =======================================================================



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
        else if(owner.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) {
            thickness = 1;
        }
        else if(owner.type.contains(ObjectType.MISSILE_SHIP)) {
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


        // draw warn Marker
        if (renderer.rendererType != RendererType.SHAPE) {
            return;
        }

//            if (owner.owner == INSTANCE.playerShip) {
//                return;
//            }

        if (owner.owner == GameScreen.INSTANCE.playerShip) {
            return;
        }

        if (owner.side == ObjectSide.ALLIES) {
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
            //shape.set(ShapeRenderer.ShapeType.Line);

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


    public GameObject getTarget() {
        return target;
    }

    public void setTarget(GameObject target) {
        this.target = target;
    }


    public Map<CompNames, ShipComponent> getComponentList() {
        return componentList;
    }

    public ForceShield getShield() {
        return (ForceShield)componentList.get(CompNames.FORCESHIELD);
    }


}
