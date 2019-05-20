package ru.geekbrains.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;


public class GameObject {

    public static final float MAX_FUEL = 30f;
    public static final float MAX_THRUSTER = 1000f;

    private Texture texture;

    public Vector2 pos = new Vector2();
    public Vector2 vel = new Vector2();
    public Vector2 acc = new Vector2();
    public Vector2 thruster = new Vector2(); // thruster force
    public Vector2 mediumRes = new Vector2(); // medium resistance force
    public Vector2 force = new Vector2(); // total result force
    public float radius;
    public float mass = 1;
    public float fuel = MAX_FUEL;

    private Vector2 tmp1 = new Vector2();
    private Vector2 tmp2 = new Vector2();



    public GameObject(String name) {

        this.texture = new Texture(name);
        radius = texture.getWidth() / 2f;

    }


    public void step(float dt) {

        //DEBUG
        //dt = 1/60f;

        // a = f/m;
        // v = v0 + a*t
        // x = x0 + v0*t + (a*t^2)/2

        tmp1.set(acc); // a*t
        vel.add(tmp1.scl(dt)); // v

        tmp1.set(vel); // v*t
        tmp2.set(acc); // (a*t^2)/2

        pos.add(tmp1.scl(dt)).add(tmp2.scl(dt*dt/2f)); // x


        fuel -= (thruster.len()/MAX_THRUSTER * dt);
    }


    public void draw(SpriteBatch batch) {

        float cornerX = pos.x - radius;
        float cornerY = pos.y - radius;
        batch.draw(texture, cornerX, cornerY);

    }

    public void dispose() {
        texture.dispose();
    }


    public void applyForce() {
        acc = force.scl(1/mass);
    }
}


