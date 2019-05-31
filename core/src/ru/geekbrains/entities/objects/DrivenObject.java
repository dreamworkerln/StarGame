package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import ru.geekbrains.screen.Renderer;
import ru.geekbrains.entities.particles.SmokeTrail;

/**
 * Object with thruster and gyrodine
 */
public abstract class DrivenObject extends GameObject {


    public float maxFuel = 100000f;         // maximum fuel tank capacity
    public float maxThrottle = 50f;        // maximum thruster engine force
    public float maxRotationSpeed = 0.05f; // maximum rotation speed

    //public Guidance guidance = Guidance.AUTO;

    public GameObject target;                       // цель

    protected Vector2 guideVector = new Vector2(); // вектор куда нужно целиться

    public float health;                       // текущий запас прочности корпуса(health)
    public int maxHealth = 3;               // максимальный запас прочности корпуса(health)

    protected float throttle = 0;                   // current throttle

    public float fuel = maxFuel;                   // current fuel level

    protected Vector2 enginePos = new Vector2();         // tail position
    protected Vector2 engineTrailPos = new Vector2();    // tail position

    public List<SmokeTrail> smokeTrailList = new ArrayList<>();


    private SmokeTrail engineTrail;                      // trail from thruster burst
    private SmokeTrail damageBurnTrail;                  // trail from burning on damage
    
    public DrivenObject(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.DRIVEN_OBJECT);

        health = maxHealth;

        engineTrail = new SmokeTrail(radius * 0.4f, new Color(0.5f,0.5f,0.5f,1), owner);
        smokeTrailList.add(engineTrail);

        damageBurnTrail = new SmokeTrail(radius * 0.4f, new Color(0.3f,0.2f,0.2f,1), owner);
        damageBurnTrail.speed = 0;
        damageBurnTrail.TTL = 100;
        smokeTrailList.add(damageBurnTrail);
    }


    /**
     * Perform simulation step
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {

        // auto removing destroyed targets
        if (target == null ||  target.readyToDispose) {
            target = null;
        }


        // guiding - controlling direction and thrust value
        guide();

        // apply thruster --------------------------------------------------------------------------

        // check fuel
        if (fuel <= 0) {
            throttle = 0;
        }
        else {
            // update fuel
            fuel -= (throttle / maxThrottle * dt);
        }

        // apply throttle
        force.add(tmp1.set(dir).nor().scl(throttle));

        // tail vector
        tailVec.set(dir);
        tailVec.scl(-radius);

        // tail position
        tailPos.set(pos).add(tailVec);


        // engine burst pos
        tmp1.set(tailVec).scl(1.3f);
        enginePos.set(pos).add(tmp1);

        // smoke trace pos
        tmp1.set(tailVec).scl(1.7f);
        engineTrailPos.set(pos).add(tmp1);

        // add smoke trail particle
        if (throttle > 0) {
            engineTrail.add(engineTrailPos, dir, vel, throttle / maxThrottle);
        }


        if (health < maxHealth) {
            damageBurnTrail.add(pos, dir, vel, (maxHealth - health) / maxHealth);
        }

        //damageBurnTrail.add(pos, dir, vel, 1);

        //damageBurnTrail.add(pos, dir, vel, 1);


        // -----------------------------------------------------------------------------------------

        // ~~~~~~~~~~~~~~
        super.update(dt);
        // ~~~~~~~~~~~~~~

        // -----------------------------------------------------------------------------------------

        for (SmokeTrail st : smokeTrailList) {
            st.update(dt);
        }


        // exploding if no health
        if (health <0 ) {
            readyToDispose = true;
        }

        // auto-repair
        if (health < maxHealth) {
            health += 0.001;
        }


    }


    protected abstract void guide();

    // ---------------------------------------------------------------------------------------------



    @Override
    public void setRadius(float radius) {
        this.radius = radius;

        for (SmokeTrail st : smokeTrailList) {

            st.radius = radius;

        }
    }

    public List<SmokeTrail> getSmokeTrailList() {
        return smokeTrailList;
    }

    @Override
    public void draw(Renderer renderer) {

        // render smoke before ship
        engineTrail.draw(renderer);


        // render ship
        super.draw(renderer);

        // render engine burst
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        renderer.shape.begin();
        renderer.shape.set(ShapeRenderer.ShapeType.Filled);

        renderer.shape.setColor(1f, 0.8f, 0.2f, 1);
        renderer.shape.circle(enginePos.x, enginePos.y, radius * 0.3f * (throttle/maxThrottle));

        Gdx.gl.glLineWidth(1);
        renderer.shape.end();


        // render damage burn after ship
        damageBurnTrail.draw(renderer);

    }


    @Override
    public void dispose() {

        // do not dispose engineTrail and damageBurnTrail
        // they will be owned by explosion

        engineTrail = null;
        damageBurnTrail = null;

        pos = null;
        vel = null;
        acc = null;
        mass = 0;



    }
}
