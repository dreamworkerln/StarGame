package ru.geekbrains.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.math.Rect;
import ru.geekbrains.sprite.Sprite;

public class Kerbonaut {

    public static final float MAX_FUEL = 300f;       // maximum fuel tank capacity
    public static final float MAX_THRUSTER = 0.3f; // maxinum thruster engine force

    public Vector2 pos = new Vector2();             // position
    public Vector2 vel = new Vector2();             // velocity
    public Vector2 acc = new Vector2();             // acceleration
    public Vector2 thruster = new Vector2();        // thruster force
    public Vector2 mediumRes = new Vector2();       // medium resistance force
    public Vector2 force = new Vector2();           // resulting force (sum of all forces)
    public Vector2 target = new Vector2();          // координаты цели
    public Vector2 vecTarget = new Vector2();       // вектор к цели
    public float radius;                            // object radius (== halfHeight)
    public float mass = 1;                          // mass
    public float fuel = MAX_FUEL;                   // current fuel level

    private Sprite sprite;                          // displaying sprite

    private Vector2 tmp1 = new Vector2();           // buffer
    private Vector2 tmp2 = new Vector2();           // buffer

    public Kerbonaut(TextureRegion textureRegion) {

        sprite = new Sprite(textureRegion);
        sprite.setFilter();
        radius = sprite.getHalfHeight();

    }



    /**
     * Perform simulation step
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {

        //DEBUG
        //dt = 1/60f;



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
        pos.add(tmp1.scl(dt)).add(tmp2.scl(dt * dt / 2f)); // x

        // update fuel
        fuel -= (thruster.len() / MAX_THRUSTER * dt);

        // update sprite position and angle
        sprite.setPos(pos);
        sprite.setAngle(vel.angle());

        // experimental 
        //sprite.setHeightAndResize(h);
    }


    public void applyForce() {
        acc = force.scl(1/mass);
    }


    // ---------------------------------------------------------------------------------------------

    public void draw(SpriteBatch batch) {

        sprite.draw(batch);
    }

    public void setHeightAndResize(float height) {
        sprite.setHeightAndResize(height);
        radius = sprite.getHalfHeight();
    }


    public void dispose() {
        sprite.dispose();
    }


}
