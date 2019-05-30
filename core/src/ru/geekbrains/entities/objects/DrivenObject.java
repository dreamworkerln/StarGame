package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.entities.auxiliary.Guidance;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.entities.particles.SmokeTrail;

/**
 * Object with thruster and gyrodine
 */
public abstract class DrivenObject extends GameObject {


    public float maxFuel = 100000f;         // maximum fuel tank capacity
    public float maxThrottle = 50f;        // maximum thruster engine force
    public float maxRotationSpeed = 0.05f; // maximum rotation speed

    public Guidance guidance = Guidance.AUTO;

    public GameObject target;                       // цель

    protected float throttle = 0;                   // current throttle

    public float fuel = maxFuel;                   // current fuel level

    protected Vector2 enginePos = new Vector2();         // tail position
    protected Vector2 smokeTrailPos = new Vector2();         // tail position


    private ru.geekbrains.entities.particles.SmokeTrail smokeTrail;
    
    public DrivenObject(TextureRegion textureRegion, float height) {
        super(textureRegion, height);
        smokeTrail = new ru.geekbrains.entities.particles.SmokeTrail(radius / 5f);
    }


    /**
     * Perform simulation step
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {

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
        smokeTrailPos.set(pos).add(tmp1);

        // add smoke trail particle
        if (throttle > 0) {
            smokeTrail.add(smokeTrailPos, dir, vel, throttle / maxThrottle);
        }

        smokeTrail.update(dt);


        // -----------------------------------------------------------------------------------------

        // ~~~~~~~~~~~~~~
        super.update(dt);
        // ~~~~~~~~~~~~~~

        // -----------------------------------------------------------------------------------------


    }


    protected void guide() {
    }

    // ---------------------------------------------------------------------------------------------



    @Override
    public void setRadius(float radius) {
        this.radius = radius;
        smokeTrail.radius = radius;

    }

    public SmokeTrail getSmokeTrail() {
        return smokeTrail;
    }

    @Override
    public void draw(Renderer renderer) {

        // render smoke before ship
        smokeTrail.draw(renderer);

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

    }

//    @Override
//    public void explode() {
//
//        if (exploded)
//            return;
//        throttle = 0;
//
//        super.explode();
//    }
    

    @Override
    public void dispose() {

        pos = null;
        vel = null;
        acc = null;
        mass = 0;


        // do not dispose smokeTrail -
        // it will be owned by explosion
    }
}
