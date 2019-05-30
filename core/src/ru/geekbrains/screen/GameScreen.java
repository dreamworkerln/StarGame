package ru.geekbrains.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;
import com.github.varunpant.quadtree.Point;
import com.github.varunpant.quadtree.QuadTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.EnemyShip;
import ru.geekbrains.entities.objects.Shell;
import ru.geekbrains.entities.particles.Explosion;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.auxiliary.Guidance;
import ru.geekbrains.entities.objects.Planet;
import ru.geekbrains.entities.objects.PlayerShip;
import ru.geekbrains.entities.auxiliary.TrajectorySimulator;
import ru.geekbrains.math.Rect;
import ru.geekbrains.sprite.Background;
import ru.geekbrains.sprite.Reticle;
import ru.geekbrains.storage.Game;


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

        planet = new Planet(new TextureRegion(new Texture("dune.png")),100f);
        planet.pos = new Vector2(0, 0);


        target.set(500f,500f);
        reticle = new Reticle(new TextureRegion(new Texture("reticle.png")));
        reticle.setHeightAndResize(30f);

        playerShip = new PlayerShip(new TextureRegion(new Texture("ship_player.png")), 50);
        playerShip.pos = new Vector2(+400f, +400f);
        playerShip.vel = new Vector2(10f, -50f);
        playerShip.target = null;         //add target
        playerShip.guidance = Guidance.MANUAL;
        playerShip.name = "playerShip";
        //playerShip.gun.fireRate = 0.025f;
        playerShip.trajectorySim = new TrajectorySimulator(playerShip);
        gameObjects.add(playerShip);

    }


    private void update(float dt) {


        // experimental - spawnEnemyShip
        if (getTick() % 700 == 0) {
            spawnEnemyShip();
        }

        // -----------------------------------------------------------------------------------------
        // spawn new objects
        // -----------------------------------------------------------------------------------------

        for (GameObject obj: spawningObjects) {

            //  addFirst, if addLast then shells will kill self gunner ship
            //  when ship have great acceleration
            gameObjects.addFirst(obj);

            if (obj instanceof DrivenObject) {
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

        quadTree.set(planet.pos.x, planet.pos.y, planet);

        // fill quadTree with gameObjects
        // fill hittableObjects too

        // manually add planet because it's not in gameObjects
        hittableObjects.add(planet);

        for (GameObject obj : gameObjects) {

            quadTree.set(obj.pos.x, obj.pos.y, obj);

            //  add to hittableObjects only if it is ship or missile
            if (obj instanceof DrivenObject) {
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
            collisionDetection();

            // -------------------------------------------------------------------------------------

            // add obj to objectsToDelete
            if (obj.readyToDispose) {

                // removing from gameObjects
                it.remove();

                // check for create explosion
                if (obj instanceof DrivenObject) {

                    Explosion expl = new Explosion(obj);
                    particleObjects.add(expl);
                }
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


        // coordinate axis
        renderer.shape.begin();
        Gdx.gl.glLineWidth(1);
        renderer.shape.set(ShapeRenderer.ShapeType.Line);
        renderer.shape.setColor(Color.BLUE);
        renderer.shape.line(new Vector2(-1000f, 0f), new Vector2(1000, 0f));
        renderer.shape.line(new Vector2(0f, -1000f), new Vector2(0, 1000f));
        Gdx.gl.glLineWidth(1);
        renderer.shape.end();

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

        tmp0s = tmp1s.setLength(G*planet.getMass() * obj.getMass()/divider);
        obj.applyForce(tmp0s);
    }





    private void checkPlanetCollide(GameObject obj) {

        tmp1.set(planet.pos);
        tmp1.sub(obj.pos);

        if (tmp1.len() <= planet.getRadius() + obj.getRadius()) {

            // stop object
            obj.vel.setZero();

            obj.readyToDispose = true;
        }
    }

    private void collisionDetection() {

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


            Point<GameObject>[] points = quadTree.searchIntersect(x1, y1, x2, y2);
            //Arrays.sort(points);


            for(int i = 0; i < points.length; i++) {

                GameObject prj = points[i].getValue(); // projectile

                if (prj.readyToDispose)
                    continue;

                tmp1.set(prj.pos);
                tmp1.sub(tgt.pos); // vector from target to projectile

                if (prj != tgt &&
                        tmp1.len() <= tgt.getRadius() + points[i].getValue().getRadius()) {

                    if (tgt == planet) {
                        // stop projectile - fallen on planet
                        prj.vel.setZero();
                        // destroy projectile
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
            tmp1.set(MathUtils.random(-800, 800), MathUtils.random(-800, 800));
            tmp2.set(tmp1).sub(tmp0);
            tmp3.set(tmp1).sub(planet.pos);
        }
        while (tmp2.len() < 800 || tmp3.len() < 800);




        EnemyShip enemyShip = new EnemyShip(new TextureRegion(enemyShipTexture), 50);
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
            if (obj instanceof EnemyShip) {

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

        // reflected_vel=vel−2(vel⋅n)n, where n - unit normal vector
        if (obj.pos.x - obj.getRadius() < leftBound) {

            if (obj instanceof Shell) {
                obj.readyToDispose = true;
                return;
            }

            n = borderNormals.left;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.5f);
            obj.pos.x = 2 * leftBound + 2*obj.getRadius() - obj.pos.x;
        }

        if (obj.pos.x + obj.getRadius() > rightBound) {

            if (obj instanceof Shell) {
                obj.readyToDispose = true;
                return;
            }

            n = borderNormals.right;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.5f);
            obj.pos.x = 2 * rightBound - 2*obj.getRadius() - obj.pos.x;
        }

        if (obj.pos.y - obj.getRadius() < downBound) {

            if (obj instanceof Shell) {
                obj.readyToDispose = true;
                return;
            }

            n = borderNormals.down;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.5f);
            obj.pos.y = 2 * downBound + 2*obj.getRadius() - obj.pos.y;

        }

        if (obj.pos.y + obj.getRadius() > upBound) {

            if (obj instanceof Shell) {
                obj.readyToDispose = true;
                return;
            }

            n = borderNormals.up;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.5f);
            obj.pos.y = 2 * upBound - 2 *obj.getRadius() - obj.pos.y;
        }
    }



    // ---------------------------------------------------------------------------------------------


    public static void addObject(GameObject obj) {

        INSTANCE.spawningObjects.add(obj);
    }





}




