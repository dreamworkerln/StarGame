package ru.geekbrains.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;
import com.github.varunpant.quadtree.Point;
import com.github.varunpant.quadtree.QuadTree;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.EnemyShip;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.Explosion;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.Planet;
import ru.geekbrains.entities.objects.PlayerShip;
import ru.geekbrains.math.Rect;
import ru.geekbrains.sprite.Background;
import ru.geekbrains.sprite.Reticle;


public class GameScreen extends BaseScreen {

    private static Vector2 tmp0s = new Vector2();
    private static Vector2 tmp1s = new Vector2();

    public static GameScreen INSTANCE = null;

    private long tick = 0;

    /**
     * Get current value
     */
    public long getTick() {

        return tick;
    }

    /**
     * Progress updateTick
     */
    public void updateTick() {

        tick++;
    }

    public boolean isDEBUG() {
        return true;
    }


    // ---------------------------


    private Vector2 tmp0 = new Vector2();
    private Vector2 tmp1 = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();

    private PlayerShip playerShip;
    private Texture enemyShipTexture = new Texture("ship_enemy.png");



    private Background background;
    private Reticle reticle;
    public  Planet planet;

    private Set<GameObject> spawningObjects = new HashSet<>(); // objects to spawn

    private LinkedList<GameObject> gameObjects = new LinkedList<>();
    private Set<GameObject> particleObjects = new HashSet<>();

    // Список объектов, по которым можно попадать снарядами
    // Используется в quadTree в качестве целей, отсортирован по убыванию радиуса
    private ArrayList<GameObject> hittableObjects = new ArrayList<>();

    //private Map<Float,GameObject> hittableObjects = new TreeMap<>((f1, f2) -> -Float.compare(f1, f2));

    //private List<GameObject> objectsToDelete = new ArrayList<>();
    private BorderNormals borderNormals = new BorderNormals();

    private QuadTree<GameObject> quadTree;

    @Override
    public void show() {
        super.show();

        if (GameScreen.INSTANCE == null) {
            GameScreen.INSTANCE = this;
        }

//        quadTree = new QuadTree<>(- worldBounds.getHalfWidth(),- worldBounds.getHalfHeight(),
//                worldBounds.getHalfWidth(),worldBounds.getHalfHeight());

        quadTree = new QuadTree<>(-2000,-2000,2000,2000);

        background = new Background(new TextureRegion(new Texture("A_Deep_Look_into_a_Dark_Sky.jpg")));
        background.setHeightAndResize(2000f);

        planet = new Planet(new TextureRegion(new Texture("dune.png")),100f, null);
        planet.pos = new Vector2(0, 0);


        target.set(500f,500f);
        reticle = new Reticle(new TextureRegion(new Texture("reticle.png")));
        reticle.setHeightAndResize(30f);

        playerShip = new PlayerShip(new TextureRegion(new Texture("ship_player.png")), 50, null);
        playerShip.pos = new Vector2(+400f, +400f);
        playerShip.vel = new Vector2(0f, -50f);
        playerShip.target = null;         //add target
        //playerShip.guidance = Guidance.MANUAL;
        playerShip.name = "playerShip";
        //playerShip.gun.fireRate = 0.025f;
        addObject(playerShip);

    }


    private void update(float dt) {


        // experimental - spawnEnemyShip
        if (getTick() % 500 == 0) {
            spawnEnemyShip();
        }

        // -----------------------------------------------------------------------------------------
        // spawn new objects
        // -----------------------------------------------------------------------------------------

        for (GameObject obj: spawningObjects) {

            //  addFirst, if addLast then shells will kill self gunner ship
            //  when ship have great acceleration
            gameObjects.addFirst(obj);


            if (obj.type.contains(ObjectType.DRIVEN_OBJECT) ||
                    obj.type.contains(ObjectType.FORCE_SHIELD)) {
                hittableObjects.add(obj);
            }
        }
        spawningObjects.clear();
        hittableObjects.sort((o1, o2) -> -Float.compare(o1.getRadius(), o2.getRadius()));


        // -----------------------------------------------------------------------------------------
        // quadTree
        // -----------------------------------------------------------------------------------------

        quadTree.clear();
        hittableObjects.clear();

        // add planet to quadTree
        //quadTree.set(planet.pos.x, planet.pos.y, planet);

        // fill quadTree with gameObjects
        // fill hittableObjects too

        // manually add planet to hittableObjects because it's not in gameObjects
        hittableObjects.add(planet);

        for (GameObject obj : gameObjects) {

            quadTree.set(obj.pos.x, obj.pos.y, obj);

            //  add to hittableObjects only if it is ship or missile
            if (obj.type.contains(ObjectType.DRIVEN_OBJECT)) {
                hittableObjects.add(obj);
            }
        }

        // -----------------------------------------------------------------------------------------
        // update targets for enemy ships
        // -----------------------------------------------------------------------------------------

        retargetEnemyShips();


        // -----------------------------------------------------------------------------------------
        // player reticle
        // -----------------------------------------------------------------------------------------

        reticle.setPos(target);

        // -----------------------------------------------------------------------------------------
        // gameObjects
        // -----------------------------------------------------------------------------------------
        Iterator<GameObject> it = gameObjects.iterator();

        GameObject obj;

        while (it.hasNext()) {

            obj = it.next();

            // calculate gravitation force from planet
            applyPlanetGravForce(obj, planet);

            // update velocity, position, self-guiding, prepare animation, etc
            obj.update(dt);

            // check wall bouncing
            borderBounce(obj);

            // check collision
            collisionDetection(dt);

            // -------------------------------------------------------------------------------------

            // add obj to objectsToDelete
            if (obj.readyToDispose) {

                // removing from gameObjects
                it.remove();

                Explosion expl = new Explosion(obj);
                particleObjects.add(expl);



                obj.dispose();
            }
        }

        // -----------------------------------------------------------------------------------------
        // particleObjects
        // -----------------------------------------------------------------------------------------
        it = particleObjects.iterator();
        while (it.hasNext()) {

            obj = it.next();

            // update velocity, position
            obj.update(dt);

            // add obj to objectsToDelete
            if (obj.readyToDispose) {
                // removing from particleObjects
                it.remove();
                obj.dispose();
            }
        }

        // -----------------------------------------------------------------------------------------

        // increment game tick
        updateTick();
    }


    @Override
    public void render(float delta) {

        Instant inst = Instant.now();

        //DEBUG delta
        float dt = this.isDEBUG() ? 1/60f : delta;

        // perform simulation step
        update(dt);

        // -----------------------------------------------------------------------------------------

        // rendering
        super.render(dt);

        // clear screen
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        // background

        background.draw(renderer.batch);
        planet.draw(renderer);


//        // coordinate axis
//        renderer.shape.begin();
//        Gdx.gl.glLineWidth(1);
//        renderer.shape.set(ShapeRenderer.ShapeType.Line);
//        renderer.shape.setColor(Color.BLUE);
//        renderer.shape.line(new Vector2(-1000f, 0f), new Vector2(1000, 0f));
//        renderer.shape.line(new Vector2(0f, -1000f), new Vector2(0, 1000f));
//        Gdx.gl.glLineWidth(1);
//        renderer.shape.end();

        // reticle
        reticle.draw(renderer.batch);
        
        // particleObjects
        for (GameObject obj : particleObjects) {
            obj.draw(renderer);
        }

        // gameObjects
        for (GameObject obj : gameObjects) {
            obj.draw(renderer);
        }


        Duration du = Duration.between(inst, Instant.now());


        long milli = du.toMillis();

        if ( milli > (1/60d)*1000) {
            System.out.println(milli);
        }

    }




    @Override
    public void resize(Rect worldBounds) {
        super.resize(worldBounds);
    }

    @Override
    public boolean touchDown(Vector3 touch, int pointer) {
        return super.touchDown(touch, pointer);
    }

    // ---------------------------------------------------



    class BorderNormals {

        Vector2 left = new Vector2(1, 0);
        Vector2 right = new Vector2(-1, 0);
        Vector2 up = new Vector2(0, -1);
        Vector2 down = new Vector2(0, 1);
    }

    /**
     * Apply gravity force from planet to obj
     * @param obj GameObject
     * @param planet Planet
     */
    public static void applyPlanetGravForce(GameObject obj, Planet planet) {

        if (obj == planet)
            return;

        // Newton's law of universal gravitation
        // F = G * m1*m2/r^2;

        tmp1s.set(planet.pos);
        tmp1s.sub(obj.pos);
        float G = 2f;

        //float G = 0f;
        float divider = tmp1s.len2();
        // avoid division by zero 
        if (divider < 0.0001)
            divider = 0.0001f;

        tmp0s.set(tmp1s.setLength(G*planet.getMass() * obj.getMass()/divider));
        obj.applyForce(tmp0s);
    }


    private void collisionDetection(float dt) {

        // objects with greater radius goes first
        for(GameObject tgt : hittableObjects) {

            if (tgt.readyToDispose)
                continue;


            // tgt - target

            double x1,x2,y1,y2;


            x1 = tgt.pos.x - 2*tgt.getRadius();
            x2 = tgt.pos.x + 2*tgt.getRadius();
            y1 = tgt.pos.y - 2*tgt.getRadius();
            y2 = tgt.pos.y + 2*tgt.getRadius();


            // HAAACK
            if (tgt.type.contains(ObjectType.PLAYER_SHIP)) {

                PlayerShip plsp = (PlayerShip) tgt;
                x1 = tgt.pos.x - 2*plsp.shield.getRadius();
                x2 = tgt.pos.x + 2*plsp.shield.getRadius();
                y1 = tgt.pos.y - 2*plsp.shield.getRadius();
                y2 = tgt.pos.y + 2*plsp.shield.getRadius();
            }


            Point<GameObject>[] points = quadTree.searchIntersect(x1, y1, x2, y2);
            //Arrays.sort(points);


            for(int i = 0; i < points.length; i++) {

                GameObject prj = points[i].getValue(); // projectile

                if (prj.readyToDispose)
                    continue;

                tmp1.set(prj.pos).sub(tgt.pos); // vector from target to projectile

                if (tgt.type.contains(ObjectType.PLAYER_SHIP) &&
                        prj.type.contains(ObjectType.SHELL) &&
                        prj.owner != tgt) {     // щит не влияет на свои снаряды


                    PlayerShip plsp = (PlayerShip) tgt;

                    if (tmp1.len() <= plsp.shield.getRadius() + prj.getRadius() &&
                            plsp.shield.power / plsp.shield.maxPower >= 0) {

                        plsp.shield.power -= 0.1f;

                        Vector2 n = tmp3; // vector from target to projectile, normalized
                        n.set(prj.pos).sub(tgt.pos).nor();

                        // repulsing by force shield
                        tmp0.set(n.scl(plsp.shield.forceValue * plsp.shield.getRadius()/tmp1.len2()));

                        prj.applyForce(tmp0);
                        plsp.applyForce(tmp0.scl(-1));
                        n = null;



                        // абсолютно неупругое столкновение
                        // affect impact on target ship
                        // tmp2.set(prj.vel).scl(prj.getMass() / dt);
                        // plsp.applyForce(tmp2);


                    }


                }



                if (prj != tgt && tmp1.len() <= tgt.getRadius() + prj.getRadius()) {


                    if (tgt == planet) {
                        // stop projectile - fallen on planet
                        prj.vel.setZero();
                        // destroy projectile
                        prj.readyToDispose = true;
                    }
                    // damaging DrivenObject by projectile
                    else if (tgt.type.contains(ObjectType.DRIVEN_OBJECT) &&
                            prj.type.contains(ObjectType.PROJECTILE)) {

                        DrivenObject drObj = (DrivenObject) tgt;

                        if (prj.type.contains(ObjectType.SHELL)) {
                            drObj.health--;
                        }

                        if (prj.type.contains(ObjectType.BULLET)) {
                            drObj.health -= 0.05;
                        }


                        // affect impact on target ship
                        tmp2.set(prj.vel).scl(prj.getMass() / dt);
                        drObj.applyForce(tmp2);

                        prj.vel.setZero();
                        prj.readyToDispose = true;

                    }

                    else {

                        // destroy both
                        tgt.readyToDispose = true;
                        prj.readyToDispose = true;
                    }

                    // CHEATING
                    //if (tgt instanceof PlayerShip) tgt.readyToDispose = false;
                    //if (prj instanceof PlayerShip) prj.readyToDispose = false;
                }
            }
        }
    }






    @Override
    public void dispose() {

        background.dispose();
        planet.dispose();
        reticle.dispose();

        for (GameObject obj : gameObjects) {
            obj.dispose();
        }

        for (GameObject obj : hittableObjects) {
            obj.dispose();
        }

        for (GameObject obj : spawningObjects) {
            obj.dispose();
        }

        super.dispose();
    }


    private void spawnEnemyShip() {

        if (playerShip.readyToDispose) {
            tmp0.set(0, 0);
        }
        else {
            tmp0.set(playerShip.pos);
        }


        do {
            tmp1.set(MathUtils.random(-500, 500), MathUtils.random(-500, 500));
            tmp2.set(tmp1).sub(tmp0);
            tmp3.set(tmp1).sub(planet.pos);
        }
        while (tmp2.len() < 600 || tmp3.len() < 600);




        EnemyShip enemyShip = new EnemyShip(new TextureRegion(enemyShipTexture), 50, null);
        enemyShip.pos = tmp1.cpy();
        //enemyShip.target = playerShip;  //add target
        //enemyShip.gun.fireRate = 0.020f;
        //enemyShip.gun.fireRate = 0.01f;
        enemyShip.maxRotationSpeed *= 2f;
        enemyShip.name = "enemyship";

        addObject(enemyShip);
    }


    /**
     * Will add new target to EnmyShips if they didn't have one
     */
    private void retargetEnemyShips() {


        for (GameObject obj : gameObjects) {
            if (obj.type.contains(ObjectType.ENEMY_SHIP)) {

                EnemyShip ship = (EnemyShip) obj;

                if (ship.target == null) {

                    int cnt = 0;
                    do {

                        GameObject tmp;

                        int rnd = MathUtils.random(0, hittableObjects.size() - 1);
                        tmp = hittableObjects.get(rnd);


                        if (tmp != planet && tmp != ship) {
                            ship.target = tmp;
                        }


                        if (cnt++ >= 10)
                            break;
                    }
                    while (ship.target == null);

                }
            }
        }

    }




    private void borderBounce(GameObject obj) {

        // wall bouncing ----------------------------------------
        //https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector

        Vector2 n;

        // Подстраиваем левую и правую стенки игрового мира(world) под соотношенире сторон устройства
        float leftBound = worldBounds.getLeft() * aspect;
        float rightBound = worldBounds.getRight() * aspect;

        float upBound = worldBounds.getTop();
        float downBound = worldBounds.getBottom();

        // -----------------------------------------------------------------------------------------

        if (obj.pos.x - obj.getRadius() < 2f * leftBound) {
            obj.readyToDispose = true;
            return;
        }

        // reflected_vel=vel−2(vel⋅n)n, where n - unit normal vector
        if (obj.pos.x - obj.getRadius() < leftBound &&
                !(obj.type.contains(ObjectType.PROJECTILE) ||
                        obj.type.contains(ObjectType.MISSILE))) {

            n = borderNormals.left;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.5f);
            obj.pos.x = 2 * leftBound + 2*obj.getRadius() - obj.pos.x;
        }

        // -----------------------------------------------------------------------------------------

        if (obj.pos.x + obj.getRadius() > 2f * rightBound) {
            obj.readyToDispose = true;
            return;
        }

        if (obj.pos.x + obj.getRadius() > rightBound &&
                !(obj.type.contains(ObjectType.PROJECTILE) ||
                        obj.type.contains(ObjectType.MISSILE))) {

            n = borderNormals.right;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.5f);
            obj.pos.x = 2 * rightBound - 2*obj.getRadius() - obj.pos.x;
        }

        // -----------------------------------------------------------------------------------------

        if (obj.pos.y - obj.getRadius() < 2f * downBound) {
            obj.readyToDispose = true;
            return;
        }

        if (obj.pos.y - obj.getRadius() < downBound &&
                !(obj.type.contains(ObjectType.PROJECTILE) ||
                        obj.type.contains(ObjectType.MISSILE))) {

            n = borderNormals.down;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.5f);
            obj.pos.y = 2 * downBound + 2*obj.getRadius() - obj.pos.y;

        }

        // -----------------------------------------------------------------------------------------

        if (obj.pos.y + obj.getRadius() > 2f * upBound) {
            obj.readyToDispose = true;
            return;
        }

        if (obj.pos.y + obj.getRadius() > upBound &&
                !(obj.type.contains(ObjectType.PROJECTILE) ||
                        obj.type.contains(ObjectType.MISSILE))) {

            n = borderNormals.up;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.5f);
            obj.pos.y = 2 * upBound - 2 *obj.getRadius() - obj.pos.y;
        }

        // -----------------------------------------------------------------------------------------
    }



    // ---------------------------------------------------------------------------------------------


    public static void addObject(GameObject obj) {

        INSTANCE.spawningObjects.add(obj);
    }


    public static List<GameObject> getCloseObjects(GameObject target, float radius) {

        List<GameObject> result = new ArrayList<>();


        double x1,x2,y1,y2;

        x1 = target.pos.x - radius;
        x2 = target.pos.x + radius;
        y1 = target.pos.y - radius;
        y2 = target.pos.y + radius;


        Point<GameObject>[] points = INSTANCE.quadTree.searchIntersect(x1, y1, x2, y2);
        //Arrays.sort(points);

        //remove invalid

        for (Point<GameObject> p : points) {

            if (!p.getValue().readyToDispose){
                result.add(p.getValue());
            }
        }


        result.sort((p1, p2) -> {

            tmp0s.set(p1.pos).sub(target.pos);
            tmp1s.set(p2.pos).sub(target.pos);

            return Float.compare(tmp0s.len(), tmp1s.len());

        });


        return result;
    }




    public static Renderer getRenderer() {

        return INSTANCE.renderer;
    }




}




