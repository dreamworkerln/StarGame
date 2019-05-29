package ru.geekbrains.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Object with thruster and gyrodine
 */
public class DrivenObject extends GameObject {

    public String name;

    public float maxFuel = 100000f;         // maximum fuel tank capacity
    public float maxThrottle = 50f;        // maximum thruster engine force
    public float maxRotationSpeed = 0.05f; // maximum rotation speed

    public Guidance guidance = Guidance.AUTO;

    public GameObject target;                       // цель
    public Vector2 vecTarget = new Vector2();       // вектор к цели

    protected float throttle = 0;                   // current throttle

    public float fuel = maxFuel;                   // current fuel level

    protected Vector2 enginePos = new Vector2();         // tail position
    protected Vector2 smokeTrailPos = new Vector2();         // tail position


    private SmokeTrail smokeTrail;
    
    public DrivenObject(TextureRegion textureRegion, float height) {
        super(textureRegion, height);
        smokeTrail = new SmokeTrail(radius / 5f);
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

        // if target is dead exit
        if (target == null ||  target.readyToDispose) {

            throttle = 0;
            target = null;
            return;
        }

            // direction -----------------------

            // vector from pos to target
            vecTarget.set(target.pos).sub(pos);

            // rotation dynamics -----------------------------------------------------------------------

            // angle between direction and vecTarget
            float targetAngle = dir.angleRad(vecTarget);

            float doAngle = Math.min(Math.abs(targetAngle), maxRotationSpeed);

            if (targetAngle < 0)
                doAngle = -doAngle;
            dir.rotateRad(doAngle);

            // thruster --------------------

            // GameObject thruster force


            // turn thruster on only if distance to target greater than kerb.radius
            // and rocket fuel is available
            if (vecTarget.len() >= radius) {
                throttle = maxThrottle;
            }
            else {
                throttle = 0;
            }

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
