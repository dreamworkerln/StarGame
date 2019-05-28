package ru.geekbrains.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.sprite.Sprite;
import ru.geekbrains.storage.Game;

public class GameObject {



    public Vector2 dir = new Vector2();             // direction
    public Vector2 pos = new Vector2();             // position
    public Vector2 vel = new Vector2();             // velocity
    public Vector2 acc = new Vector2();             // acceleration
    //trash
    protected Vector2 tailVec = new Vector2();         // vector of tail
    protected Vector2 tailPos = new Vector2();         // tail position

    protected Vector2 tmpForce = new Vector2();     // tmp force
    public Vector2 force = new Vector2();           // resulting force (sum of all forces)
    protected float radius;                            // object radius (== halfHeight)
    public float mass = 1;                          // mass
    public float momentInertia = 1;                 // moment of inertia

    public boolean exploded = false;                  // object exploded
    public boolean readyToDispose = false;            // object ready to dispose

    protected Sprite sprite;                          // displaying sprite

    protected Explosion explosion;                    // explosion animation

    long deathCounter = -1;


    protected Vector2 tmp1 = new Vector2();           // buffer
    protected Vector2 tmp2 = new Vector2();           // buffer

    public GameObject(TextureRegion textureRegion, float height) {

        sprite = new Sprite(textureRegion);
        sprite.setFilter();
        sprite.setHeightAndResize(height);
        radius = sprite.getHalfHeight();
        dir.set(1, 0);
    }



    /**
     * Perform simulation step
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {

        //DEBUG delta
        dt = Game.INSTANCE.isDEBUG() ? 1/60f : dt;


        // apply medium resistance (atmosphere) - proportionally speed -----------------------------
        tmpForce.set(vel);
        // medium resistance ~ vel -
        //obj.mediumRes.scl(-0.001f * (obj.vel.len() + 1000));
        //scale
        tmpForce.scl(-0.1f);

        // disable atmosphere
        //force.add(tmpForce);

        // -----------------------------------------------------------------------------------------

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



        if (!exploded) {
            // update position
            pos.add(tmp1.scl(dt)).add(tmp2.scl(dt * dt / 2f));
        }
        // stay on place if exploded




        // =========================================================================================

        // update sprite position and angle
        sprite.setPos(pos);
        sprite.setAngle(dir.angle());

        // -----------------------------------------------------------------------------------------

        // countdown to removal from world
        if (deathCounter >= 0) {
            deathCounter--;

            if (deathCounter == 0) {
                readyToDispose = true;
            }

        }
        // --------------------------------

        if (exploded)
            explosion.update(dt);
    }




    // ---------------------------------------------------------------------------------------------






    public void applyForce() {

        acc = force.scl(1/mass);
    }


    // ---------------------------------------------------------------------------------------------

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }


    // ---------------------------------------------------------------------------------------------

    public void draw(Renderer renderer) {

        if (!exploded) {
            sprite.draw(renderer.batch);
        }
        else {
            explosion.draw(renderer.shape);
        }

    }

//    public void setHeightAndResize(float height) {
//        sprite.setHeightAndResize(height);
//        radius = sprite.getHalfHeight();
//    }


    public void dispose() {
        sprite.dispose();
    }


    public void explode() {

        if (exploded)
            return;

        exploded = true;

        // stop object
        vel.setZero();

        explosion = new Explosion(pos, radius * 3);
        deathCounter = 300;
    }
}
