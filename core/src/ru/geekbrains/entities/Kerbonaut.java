package ru.geekbrains.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.sprite.Sprite;

public class Kerbonaut {

    public static final float MAX_FUEL = 100000f;        // maximum fuel tank capacity
    public static final float MAX_THRUSTER = 200f;      // maximum thruster engine force
    public static final float MAX_ROTATION_SPEED = 0.05f; // maximum rotation speed

    public Vector2 dir = new Vector2();             // direction
    public Vector2 pos = new Vector2();             // position
    public Vector2 vel = new Vector2();             // velocity
    public Vector2 acc = new Vector2();             // acceleration
    //trash
    public Vector2 tailVec = new Vector2();         // vector of tail
    public Vector2 tailPos = new Vector2();         // tail position

    //public Vector2 thruster = new Vector2();        // thruster force
    //public Vector2 mediumRes = new Vector2();       // medium resistance force

    protected Vector2 tmpForce = new Vector2();     // tmp force
    public Vector2 force = new Vector2();           // resulting force (sum of all forces)
    public Vector2 target = new Vector2();          // координаты цели
    public Vector2 vecTarget = new Vector2();       // вектор к цели
    public float radius;                            // object radius (== halfHeight)
    public float mass = 1;                          // mass


    //public float angle = 0;                         // angle
    public float momentInertia = 1;                 // moment of inertia

    public float fuel = MAX_FUEL;                   // current fuel level

    private Sprite sprite;                          // displaying sprite

    private Vector2 tmp1 = new Vector2();           // buffer
    private Vector2 tmp2 = new Vector2();           // buffer

    private SmokeTrail smokeTrail = new SmokeTrail();

    public Kerbonaut(TextureRegion textureRegion) {

        sprite = new Sprite(textureRegion);
        sprite.setFilter();
        radius = sprite.getHalfHeight();
        dir.set(1, 0);

    }



    /**
     * Perform simulation step
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {

        //DEBUG
        dt = 1/60f;

        // vector from pos to target
        vecTarget.set(target).sub(pos);

        // rotation dynamics -----------------------------------------------------------------------

        // angle between direction and vecTarget
        float targetAngle = dir.angleRad(vecTarget);

        float doAngle = Math.min(Math.abs(targetAngle), MAX_ROTATION_SPEED);

        if (targetAngle < 0)
            doAngle =-doAngle;
        dir.rotateRad(doAngle);


        // movement dynamics -----------------------------------------------------------------------

        // apply medium resistance (atmosphere) - proportionally speed
        tmpForce.set(vel);
        // medium resistance ~ vel -
        //obj.mediumRes.scl(-0.001f * (obj.vel.len() + 1000));
        //scale
        tmpForce.scl(-0.1f);

        force.add(tmpForce);


        // -----------------------------------------------------------------------------------

        // Kerbonaut jetpack thruster force
        tmpForce.set(dir);

        // turn thruster on only if distance to target greater than kerb.radius / 4f
        // and rocket fuel is available
        if (vecTarget.len() > radius / 4f && fuel > 0) {
            tmpForce.setLength(Kerbonaut.MAX_THRUSTER);
        }
        else {
            tmpForce.setLength(0);
        }

        // calc result force
        force.add(tmpForce);

        // update fuel
        fuel -= (tmpForce.len() / MAX_THRUSTER * dt);

        // set kerbonaut direction
        //dir.set(tmpForce).nor();

        // tail vector
        tailVec.set(dir);
        tailVec.scl(-radius);

        // tail position
        tailPos.set(pos).add(tailVec);

        // add smoke trail particle
        if (fuel > 0 && tmpForce.len() > 0) {
            smokeTrail.add(tailPos, dir, vel);
        }

        // calc resulting acceleration
        applyForce();

        // =========================================================================================

        // a - current acceleration,
        // v0, x0 - initial speed and coordinates
        // v, x - new speed and coordinates

        // a = f/m;    - Second Newton law
        // v = v0 + a*t
        // x = x0 + v0*t + (a*t^2)/2

        tmp1.set(acc); // a*t

        // update velocity
        vel.add(tmp1.scl(dt)); // v

        tmp1.set(vel); // v*t
        tmp2.set(acc); // (a*t^2)/2

        // update position
        pos.add(tmp1.scl(dt)).add(tmp2.scl(dt * dt / 2f));

        // =========================================================================================

        // update sprite position and angle
        sprite.setPos(pos);
        sprite.setAngle(dir.angle());

        smokeTrail.update(dt);
    }




    // ---------------------------------------------------------------------------------------------






    public void applyForce() {
        acc = force.scl(1/mass);
    }


    // ---------------------------------------------------------------------------------------------

    public void draw(Renderer renderer) {

        smokeTrail.draw(renderer.shape);
        sprite.draw(renderer.batch);


    }

    public void setHeightAndResize(float height) {
        sprite.setHeightAndResize(height);
        radius = sprite.getHalfHeight();
    }


    public void dispose() {
        sprite.dispose();
    }


}
