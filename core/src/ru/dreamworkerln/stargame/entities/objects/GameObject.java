package ru.dreamworkerln.stargame.entities.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashSet;
import java.util.Set;

import ru.dreamworkerln.stargame.screen.GameScreen;
import ru.dreamworkerln.stargame.screen.Renderer;
import ru.dreamworkerln.stargame.screen.RendererType;
import ru.dreamworkerln.stargame.sprite.Sprite;

/**
 * Movable game object with inertia
 */
public abstract class GameObject implements Disposable, PhysicalInfo {

    protected Set<RendererType> rendererType = new HashSet<>();

    public long TTL;
    protected long birth;  // object birth date in ticks
    protected long age;    // object ages in game ticks


    public boolean isEmpArmament = false;

    public String name = "";

    public GameObject owner;

    public boolean isModule;

    public Set<ObjectType> type = new HashSet<>();

    public float armour = 0;
    public float empArmour = 0;
    public float penetration = 0;

    protected Sprite sprite = null;                 // displaying sprite (if have one)

    public Vector2 dir = new Vector2();             // direction
    public Vector2 pos = new Vector2();             // position
    public Vector2 vel = new Vector2();             // velocity
    public Vector2 acc = new Vector2();             // acceleration
    //trash
    protected Vector2 tailVec = new Vector2();      // vector of tail
    protected Vector2 tailPos = new Vector2();      // tail position
    protected float aspectRatio = 1;

    //protected Vector2 tmpForce = new Vector2();     // tmp force
    protected Vector2 force = new Vector2();          // resulting force (sum of all forces)
    public float radius;                         // object radius (== halfHeight)
    public float mass = 1;                          // mass
    //public float momentInertia = 1;               // moment of inertia

    public Vector2 guideVector = new Vector2(); // вектор куда нужно целиться

    protected float factoryMaxRotationSpeed = 0;
    protected float maxRotationSpeed = 0; // maximum rotation speed

    public float explosionRadius;



    public boolean readyToDispose = false;            // object ready to dispose

    protected Vector2 tmp0 = new Vector2();           // buffer
    protected Vector2 tmp1 = new Vector2();           // buffer
    protected Vector2 tmp2 = new Vector2();           // buffer
    protected Vector2 tmp3 = new Vector2();           // buffer
    protected Vector2 tmp4 = new Vector2();           // buffer
    protected Vector2 tmp5 = new Vector2();           // buffer
    protected Vector2 tmp6 = new Vector2();           // buffer

    public Color color = Color.WHITE;
    public Color explosionColor = new Color(1f, 1f, 0.2f, 1);

    protected float health;                       // текущий запас прочности корпуса(health)
    protected float maxHealth = 0;               // максимальный запас прочности корпуса(health)
    protected float healthGeneration = 0;               // регенерация здоровья
    protected float healthRegenerationCoefficient = 0;  // коэффициент регенерации
    protected float factoryHealthRegenerationCoefficient = 0;


    public float damage = 0;
    public float empDamage = 0;

    public ObjectSide side = ObjectSide.NEUTRAL;

    public boolean shouldExplode = true;

    public float angVel = 0;





    private GameObject(GameObject owner, float radius, RendererType renderType) {

        birth = GameScreen.INSTANCE.getTick();
        this.type.add(ObjectType.OBJECT);
        this.owner = owner;
        dir.set(1, 0);
        guideVector.setZero();
        this.radius = radius;
        rendererType.add(renderType);
        isModule = false;
        explosionRadius = 2 *radius;
    }

    /**
     * Constructor with sprite
     * @param height resize texture to specified height
     * @param textureRegion texture
     */
    public GameObject(GameObject owner, TextureRegion textureRegion, float height) {

        this(owner, height/2f, RendererType.TEXTURE);

        sprite = new Sprite(textureRegion);
        sprite.setFilter();
        sprite.setHeightAndResize(2*radius);
    }


    /**
     * Constructor without sprite - using ShapeRenderer to draw particles
     * @param radius
     */
    public GameObject(GameObject owner, float height) {

        this(owner, height / 2f, RendererType.SHAPE);
    }

    public GameObject(GameObject owner) {

        this(owner, owner.radius, RendererType.SHAPE);
    }

    public GameObject() {

        this(null, 1, RendererType.SHAPE);
    }


    /**
     * Perform simulation step
     * @param dt time elapsed from previous emulation step
     */
    public void update(float dt) {

        age = GameScreen.INSTANCE.getTick() - birth;

//        // auto removing destroyed targets
//        if (owner == null ||  owner.readyToDispose) {
//            owner = null;
//        }

        // exploding if no health
        if (health < 0 ) {
            readyToDispose = true;
        }


        if (!isModule) {


            // apply medium resistance (atmosphere) - proportionally speed -----------------------------
            //tmp3.set(vel);
            // medium resistance ~ vel -
            //obj.mediumRes.scl(-0.001f * (obj.vel.len() + 1000));
            //scale
            //tmp3.scl(-0.1f);

            // disable atmosphere
            //force.add(tmpForce);

            // -----------------------------------------------------------------------------------------

            // calc resulting acceleration
            acc.set(force.scl(1 / mass));


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

        }
        // module attached to it's parent body
        else {
            if (owner != null) {
                pos.set(owner.pos);
            }

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


    // rotation dynamics --------------------------------
    public void rotate(float dt) {


        if(!guideVector.isZero()){

            // angle between direction and guideVector
            float guideAngle = dir.angleRad(guideVector);

            float doAngle = Math.min(Math.abs(guideAngle), maxRotationSpeed);

            if (guideAngle > 0) {
                doAngle *=-1;
            }
            dir.rotateRad(doAngle);
        }

        if(Math.abs(angVel) > 1E-5) {
            dir.rotateRad(angVel* dt);
        }
    }


    // ---------------------------------------------------------------------------------------------

    public void draw(Renderer renderer) {

        // Do not draw out of screen
        float dx = GameScreen.INSTANCE.worldBounds.getHalfWidth()* GameScreen.INSTANCE.aspect;
        float dy = GameScreen.INSTANCE.worldBounds.getHalfHeight();

        if(Math.abs(pos.x) >  dx + dx * 0.3 || Math.abs(pos.y) >  dy + dy * 0.3) {
            return;
        }

        if (renderer.rendererType == RendererType.TEXTURE &&
            rendererType.contains(RendererType.TEXTURE)) {

            // update sprite position and angle
            sprite.setPos(pos);
            sprite.setAngle(dir.angle());
            
            sprite.draw(renderer);
        }

//        // update sprite position and angle
//        if (rendererType.contains(RendererType.TEXTURE)) {
//
//            sprite.setPos(pos);
//            sprite.setAngle(dir.angle());
//
//
//        }
    }


    public void doDamage(float amount) {

        health -= amount;

        // exploding if no health
        if (health <= 0 ) {
            readyToDispose = true;
        }
    }




    @Override
    public void dispose() {

        if (rendererType.contains(RendererType.TEXTURE)) {
            sprite.dispose();
        }
        readyToDispose = true;
    }

    // ---------------------------------------------------------------------------------------------



    public float getHealth() {
        return  health;
    }


    public void setMaxHealth(float maxHealth) {

        this.maxHealth = maxHealth;
        health = maxHealth;
        healthGeneration = maxHealth * healthRegenerationCoefficient;
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


    public float getMaxHealth() {
        return maxHealth;
    }



    public long getTTL() {
        return TTL;
    }

    public void setTTL(long TTL) {
        this.TTL = TTL;
    }



    public long getAge() {
        return age;
    }

    public float getExplosionRadius() {
        return explosionRadius;
    }

    public void setExplosionRadius(float explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    public float getArmour() {
        return armour;
    }

    public void setArmour(float armour) {
        this.armour = armour;
    }

    public float getPenetration() {
        return penetration;
    }

    public void setPenetration(float penetration) {
        this.penetration = penetration;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getHealthGeneration() {
        return healthGeneration;
    }

    public void setHealthGeneration(float healthGeneration) {
        this.healthGeneration = healthGeneration;
    }

    public float getHealthRegenerationCoefficient() {
        return healthRegenerationCoefficient;
    }

    public void setHealthRegenerationCoefficient(float healthRegenerationCoefficient) {
        this.healthRegenerationCoefficient = healthRegenerationCoefficient;
        factoryHealthRegenerationCoefficient = healthRegenerationCoefficient;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getEmpDamage() {
        return empDamage;
    }

    public void setEmpDamage(float empDamage) {
        this.empDamage = empDamage;
    }

    public boolean isEmpArmament() {
        return isEmpArmament;
    }

    public void setEmpArmament(boolean empArmament) {
        isEmpArmament = empArmament;
    }

    public float getMaxRotationSpeed() {
        return maxRotationSpeed;
    }

    public void setMaxRotationSpeed(float maxRotationSpeed) {
        this.maxRotationSpeed = maxRotationSpeed;
        this.factoryMaxRotationSpeed = maxRotationSpeed;
    }

    public Color getExplosionColor() {
        return explosionColor;
    }

    public void setExplosionColor(Color explosionColor) {
        this.explosionColor = explosionColor;
    }

    public GameObject getOwner() {
        return owner;
    }

    public void setOwner(GameObject owner) {
        this.owner = owner;
    }

    public Vector2 getDir() {
        return dir;
    }

    public void setDir(Vector2 dir) {
        this.dir = dir;
    }

    public Vector2 getPos() {
        return pos;
    }

    public void setPos(Vector2 pos) {
        this.pos = pos;
    }

    public Vector2 getVel() {
        return vel;
    }

    public void setVel(Vector2 vel) {
        this.vel = vel;
    }

    public Vector2 getAcc() {
        return acc;
    }

    public void setAcc(Vector2 acc) {
        this.acc = acc;
    }

    public Set<ObjectType> getType() {
        return type;
    }

    public ObjectSide getSide() {return side;}

    public void setSide(ObjectSide side) {this.side = side;}


}
