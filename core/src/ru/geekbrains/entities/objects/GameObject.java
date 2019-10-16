package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashSet;
import java.util.Set;

import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;
import ru.geekbrains.sprite.Sprite;

/**
 * Movable game object with inertia
 */
public abstract class GameObject implements Disposable, PhysicalInfo {

    protected Set<RendererType> rendererType = new HashSet<>();

    public String name = "";

    public GameObject owner;

    public Set<ObjectType> type = new HashSet<>();

    protected Sprite sprite = null;                 // displaying sprite (if have one)

    public Vector2 dir = new Vector2();             // direction
    public Vector2 pos = new Vector2();             // position
    public Vector2 vel = new Vector2();             // velocity
    public Vector2 acc = new Vector2();             // acceleration
    //trash
    protected Vector2 tailVec = new Vector2();      // vector of tail
    protected Vector2 tailPos = new Vector2();      // tail position
    protected float aspectRatio = 1;

    protected Vector2 tmpForce = new Vector2();     // tmp force
    protected Vector2 force = new Vector2();          // resulting force (sum of all forces)
    protected float radius;                         // object radius (== halfHeight)
    protected float mass = 1;                          // mass
    //public float momentInertia = 1;               // moment of inertia


    public boolean readyToDispose = false;            // object ready to dispose

    protected Vector2 tmp0 = new Vector2();           // buffer
    protected Vector2 tmp1 = new Vector2();           // buffer
    protected Vector2 tmp2 = new Vector2();           // buffer
    protected Vector2 tmp3 = new Vector2();           // buffer
    protected Vector2 tmp4 = new Vector2();           // buffer

    protected float health;                       // текущий запас прочности корпуса(health)
    protected float maxHealth = 0;               // максимальный запас прочности корпуса(health)

    public float damage = 0;




    /**
     * Constructor with sprite
     * @param textureRegion texture
     * @param height resize texture to specified height
     */
    public GameObject(TextureRegion textureRegion, float height, GameObject owner) {

        this.type.add(ObjectType.OBJECT);

        this.radius = height / 2f;
        this.owner = owner;
        sprite = new Sprite(textureRegion);
        sprite.setFilter();
        sprite.setHeightAndResize(2*radius);
        //radius = sprite.getHalfHeight();
        dir.set(1, 0);

        rendererType.add(RendererType.TEXTURE);
    }


    /**
     * Constructor without sprite - using ShapeRenderer to draw particles
     * @param radius
     */
    public GameObject(float height, GameObject owner) {

        this.type.add(ObjectType.OBJECT);
        this.owner = owner;
        this.radius = height / 2f;
        dir.set(1, 0);

        rendererType.add(RendererType.SHAPE);
    }

    public GameObject(GameObject owner) {

        this.owner = owner;
        this.radius =  owner.radius;
        dir.set(1, 0);

        rendererType.add(RendererType.SHAPE);

    }


    /**
     * Perform simulation step
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {

        // auto removing destroyed targets
        if (owner == null ||  owner.readyToDispose) {
            owner = null;
        }

        // exploding if no health
        if (health < 0 ) {
            readyToDispose = true;
        }



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
        acc.set(force.scl(1/mass));


        // calc velocity and position
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

        // clearing force to be ready for next iteration
        force.setZero();

        // -----------------------------------------------------------------------------------------


        // update sprite position and angle
        if (rendererType.contains(RendererType.TEXTURE)) {

            sprite.setPos(pos);
            sprite.setAngle(dir.angle());
        }
    }




    // ---------------------------------------------------------------------------------------------


    /**
     * Apply force to object
     */
    public void applyForce(Vector2 f) {

        force.add(f);
    }

//    public void clearForce() {
//
//        force.setZero();
//    }


    // ---------------------------------------------------------------------------------------------

    public void draw(Renderer renderer) {

        if (rendererType.contains(RendererType.TEXTURE)) {
            sprite.draw(renderer.batch);
        }
    }

    @Override
    public void dispose() {

        if (rendererType.contains(RendererType.TEXTURE)) {
            sprite.dispose();
        }

    }

    // ---------------------------------------------------------------------------------------------

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    //    public void explode() {
//
//        if (exploded)
//            return;
//
//        exploded = true;
//
//        // stop object
//        vel.setZero();
//
//        explosion = new Explosion(pos, radius * 3);
//        deathCounter = 300;
//    }


    public float getMaxHealth() {
        return maxHealth;
    }



    public void setMaxHealth(float maxHealth) {

        this.maxHealth = maxHealth;
        health = maxHealth;
    }



    public void doDamage(float amount) {

        health -= amount;

        // exploding if no health
        if (health <= 0 ) {
            readyToDispose = true;
        }
    }



}
