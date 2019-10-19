package ru.geekbrains.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.DummyObject;
import ru.geekbrains.entities.objects.EnemyShip;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.particles.Explosion;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.Planet;
import ru.geekbrains.entities.objects.PlayerShip;
import ru.geekbrains.entities.particles.Message;
import ru.geekbrains.entities.projectile.Missile;
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
    private GameObject dummy;

    private int enemyShipsToSpawn = 0;

    private PlayerShip playerShip;
    private Texture enemyShipTexture = new Texture("ship_enemy.png");



    private Background background;
    private Reticle reticle;
    public  Planet planet;

    private Set<GameObject> spawningObjects = new HashSet<>(); // objects to spawn

    private LinkedList<GameObject> gameObjects = new LinkedList<>();

    private Set<GameObject> particleObjects = new HashSet<>();

    private Set<GameObject> explosionObjects = new HashSet<>();

    // Список объектов, по которым можно попадать снарядами
    // Используется в quadTree в качестве целей, отсортирован по убыванию радиуса
    private ArrayList<GameObject> hittableObjects = new ArrayList<>();

    //private Map<Float,GameObject> hittableObjects = new TreeMap<>((f1, f2) -> -Float.compare(f1, f2));

    //private List<GameObject> objectsToDelete = new ArrayList<>();
    private BorderNormals borderNormals = new BorderNormals();

    private QuadTree<GameObject> quadTree;

    private boolean win = false;

    private Music music;
    private Sound expl01;
    private Sound expl02;
    private Sound bigExpl;
    private Sound flak;
    private Sound metalHit;




    private int ENEMY_RESPAWN_TIME;
    private int ENEMIES_COUNT_IN_WAVE;

    @Override
    public void show() {
        super.show();

        if (GameScreen.INSTANCE == null) {
            GameScreen.INSTANCE = this;
        }


        quadTree = new QuadTree<>(-4000,-4000,4000,4000);

        background = new Background(new TextureRegion(new Texture("A_Deep_Look_into_a_Dark_Sky.jpg")));
        background.setHeightAndResize(2050f);

        planet = new Planet(new TextureRegion(new Texture("dune.png")),100f, null);
        planet.pos = new Vector2(0, 0);


        target.set(500f,500f);
        reticle = new Reticle(new TextureRegion(new Texture("reticle.png")));
        reticle.setHeightAndResize(30f);

        playerShip = new PlayerShip(new TextureRegion(new Texture("ship_player.png")), 50, null);
        playerShip.pos = new Vector2(400f, 400f);
        playerShip.vel = new Vector2(0f, -100f);
        playerShip.target = null;         //add target
        //playerShip.guidance = Guidance.MANUAL;
        playerShip.name = "playerShip";
        //playerShip.gun.fireRate = 0.025f;

        //playerShip.vel = new Vector2(+100f, 0f);



        addObject(playerShip);



        Message msg = new Message("New objectives: survive till warp engine have been repaired.");
        particleObjects.add(msg);

//        Тесты для CIWS minigun

//        Missile missile = new Missile(new TextureRegion(new Texture("M-45_missile2.png")), 2, null);
//        missile.pos.set(200,385);
//        missile.vel.set(400,-10);
//        missile.dir.set(-1,-1);
//        missile.target = playerShip;
//        addObject(missile);
//
//
//        Missile missile = new Missile(new TextureRegion(new Texture("M-45_missile2.png")), 2, null);
//        missile.pos.set(0,330);
//        missile.vel.set(300,5);
//        missile.dir.set(-1,0).nor();
//        missile.target = playerShip;
//        addObject(missile);


        music = Gdx.audio.newMusic(Gdx.files.internal("Valves (remix) - Tiberian Sun soundtrack.mp3"));

        music.setVolume(1f);
        music.play();


        expl01 = Gdx.audio.newSound(Gdx.files.internal("expl01.mp3"));
        expl02 = Gdx.audio.newSound(Gdx.files.internal("expl02.mp3"));
        flak = Gdx.audio.newSound(Gdx.files.internal("flak.mp3"));
        bigExpl = Gdx.audio.newSound(Gdx.files.internal("big_expl2.mp3"));
        metalHit = Gdx.audio.newSound(Gdx.files.internal("IMPACT CAN METAL HIT RING 01.mp3"));

        // DIFFICULTY LEVEL ------------------------------------------------------------------------
        getDifficultyLevel();


        // -----------------------------------------------------------------------------------------
    }




    private void update(float dt) {

        // spawnEnemyShip
        if (getTick() % ENEMY_RESPAWN_TIME == 0) {
            enemyShipsToSpawn += ENEMIES_COUNT_IN_WAVE;
        }

        if (enemyShipsToSpawn> 0)  {
            spawnEnemyShip();
        }



        // -----------------------------------------------------------------------------------------
        // spawn new objects
        // -----------------------------------------------------------------------------------------

        // ToDo: check
        for (GameObject obj: spawningObjects) {

            //  addFirst, if addLast then shells will kill self gunner ship
            //  when ship have great acceleration
            gameObjects.addFirst(obj);


//            if (obj.type.contains(ObjectType.DRIVEN_OBJECT) ||
//                    obj.type.contains(ObjectType.FORCE_SHIELD)) { // haack for forceshield
//                hittableObjects.add(obj);
//            }
        }
        spawningObjects.clear();
//        hittableObjects.sort((o1, o2) -> -Float.compare(o1.getRadius(), o2.getRadius()));


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

        // ToDo: check
        // sort that bigger objects goes first
        hittableObjects.sort((o1, o2) -> -Float.compare(o1.getRadius(), o2.getRadius()));

        // -----------------------------------------------------------------------------------------
        // update targets for enemy ships
        // -----------------------------------------------------------------------------------------

        retargetEnemyShips();


        // -----------------------------------------------------------------------------------------
        // player reticle
        // -----------------------------------------------------------------------------------------

        reticle.setPos(target);



        // check collision
        collisionDetection(dt);


        // check out of bounds ------------------------------------------------

        for (GameObject obj : gameObjects) {
            // removing player ship
            if (!obj.readyToDispose &&
                    obj.pos.len() > GameScreen.INSTANCE.worldBounds.getWidth()*2) {

                obj.readyToDispose = true;
            }
        }
        // ----------------------------------------------------------------------

        // -----------------------------------------------------------------------------------------
        // gameObjects
        // -----------------------------------------------------------------------------------------

        Iterator<GameObject> it = gameObjects.iterator();
        GameObject obj;

        while (it.hasNext()) {

            obj = it.next();

            if (!obj.readyToDispose) {


                // calculate gravitation force from planet
                applyPlanetGravForce(obj, planet);

                // update velocity, position, self-guiding, prepare animation, etc
                obj.update(dt);

                // check wall bouncing
                borderBounce(obj);
            }
            // -------------------------------------------------------------------------------------

            // add obj to objectsToDelete
            else {

                // removing from gameObjects
                it.remove();

                playExplosionSound(obj, null);

                // do not explode fragments
                if (!obj.type.contains(ObjectType.PLANET)) {
                    Explosion expl = new Explosion(obj);
                    //particleObjects.add(expl);
                    explosionObjects.add(expl);
                }



                // call object destructor
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
        // explosionObjects
        // -----------------------------------------------------------------------------------------
        it = explosionObjects.iterator();
        while (it.hasNext()) {

            obj = it.next();

            // update velocity, position
            obj.update(dt);

            // add obj to objectsToDelete
            if (obj.readyToDispose) {
                // removing from explosionObjects
                it.remove();
                obj.dispose();
            }
        }








        // -----------------------------------------------------------------------------------------

        // increment game tick
        updateTick();


        checkWin();
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
        //super.render(dt);

        // clear screen
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        // RENDER ALL BATCH TEXTURE OBJECTS ------------------------------------


        renderer.rendererType = RendererType.TEXTURE;

        renderer.batch.begin();

        background.draw(renderer);
        planet.draw(renderer);
        reticle.draw(renderer);

        // gameObjects
        for (GameObject obj : gameObjects) {
            obj.draw(renderer);
        }

        renderer.batch.end();

        // RENDER ALL SHAPE OBJECTS ------------------------------------

        renderer.rendererType = RendererType.SHAPE;

        renderer.shape.begin();

        // gameObjects
        for (GameObject obj : gameObjects) {
            obj.draw(renderer);
        }

        // particleObjects
        for (GameObject obj : particleObjects) {
            obj.draw(renderer);
        }

        // explosionObjects
        for (GameObject obj : explosionObjects) {
            obj.draw(renderer);
        }





        renderer.shape.end();

        // ---------------------------------------------------------------------------------------


        Duration du = Duration.between(inst, Instant.now());


        long milli = du.toMillis();

        // data processing (root finding) real-time violation
        if ( milli > (1/60d)*1000) {
            System.out.println("TIME ERR >> " + milli);
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
        if (divider < 0.0000001)
            divider = 0.0000001f;

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


            x1 = tgt.pos.x - 2.1*tgt.getRadius();
            x2 = tgt.pos.x + 2.1*tgt.getRadius();
            y1 = tgt.pos.y - 2.1*tgt.getRadius();
            y2 = tgt.pos.y + 2.1*tgt.getRadius();


            // HAAACK for shield
            if (tgt.type.contains(ObjectType.PLAYER_SHIP)) {

                PlayerShip plsp = (PlayerShip) tgt;
                x1 = tgt.pos.x - 2.2*plsp.shield.getRadius();
                x2 = tgt.pos.x + 2.2*plsp.shield.getRadius();
                y1 = tgt.pos.y - 2.2*plsp.shield.getRadius();
                y2 = tgt.pos.y + 2.2*plsp.shield.getRadius();
            }




            List<Point<GameObject>> points = quadTree.searchIntersect2(x1, y1, x2, y2);

            for(int i = 0; i < points.size(); i++) {


                GameObject prj = points.get(i).getValue(); // projectile (may be DRIVEN_OBJECT)

                // уничтоженный объект не взаимодействует с другими, сам с собой тоже (в матрице по диагонали нули)
                if (prj.readyToDispose || tgt.readyToDispose  || tgt == prj)
                    continue;

                tmp1.set(prj.pos).sub(tgt.pos); // vector from target to projectile

                // FORCE SHIELD REPULSING
                if (tgt.type.contains(ObjectType.PLAYER_SHIP) &&
                        (prj.type.contains(ObjectType.PROJECTILE)
                                /*|| prj.type.contains(ObjectType.DRIVEN_OBJECT)*/) &&

                        (prj.owner != tgt || prj.type.contains(ObjectType.FRAG))


                ) {     // щит не влияет на свои снаряды


                    PlayerShip plsp = (PlayerShip) tgt;

                    if (tmp1.len() <= plsp.shield.getRadius() + prj.getRadius()) {

                        Vector2 n = tmp3; // vector from target to projectile, normalized
                        n.set(prj.pos).sub(tgt.pos).nor();


                        // Силовое поле щита имеет потенциал Const/r,
                        // Соответственно сила поля, действующая на prj равна -n*prj.mass*Const*/r^2
                        // Как гравитационное поле, но со знаком "-"
                        tmp0.set(n.scl(prj.getMass()*plsp.shield.forceValue * plsp.shield.getRadius()/tmp1.len2()));
                        n = null;

                        // dA = m*E.dx // dx - ?

                        // dx = (x0=0) + (V0*t=0) + (a*t^2)/2

                        // a = tmp0/m = E

                        tmp3.set(tmp0).scl(1/prj.getMass());  // = E  напряженность поля щита

                        // dA = m * (tmp3)^2 * t^2/2; // работа щита на перемещение prj за dt

                        float dA =  (float)(tmp3.len2() * prj.getMass() * dt*dt*0.5);

                        //System.out.println(dA);

                        // Если поле может совершить эту работу (хватает запасенной энергии)
                        // repulsing by force shield
                        if (plsp.shield.power > dA) {


                            // отражаем снаряд
                            prj.applyForce(tmp0);
                            // 3 закон Ньютона - отражаем корабль
                            plsp.applyForce(tmp0.scl(-1));

                            // depleting power shield
                            plsp.shield.power -= dA;

                            //System.out.println(dA);
                        }

                        // абсолютно неупругое столкновение
                        // affect impact on target ship
                        // tmp2.set(prj.vel).scl(prj.getMass() / dt);
                        // plsp.applyForce(tmp2);
                    }
                }



                if (tmp1.len() <= tgt.getRadius() + prj.getRadius()) {

                    if (tgt == planet) {
                        // stop projectile - fallen on planet
                        prj.vel.setZero();
                        // destroy projectile (or driven object)
                        prj.readyToDispose = true;
                    }
                    else {

                        hitLogger(tgt, prj);
                        hitLogger(prj, tgt);


                        // повреждаем цель
                        tgt.doDamage(prj.damage);
                        // повреждаем снаряд
                        prj.doDamage(tgt.damage);

                        playExplosionSound(prj, tgt);

                    }



//                    // damaging DrivenObject by projectile
//                    else if (tgt.type.contains(ObjectType.DRIVEN_OBJECT) &&
//                            (prj.type.contains(ObjectType.PROJECTILE) || prj.type.contains(ObjectType.ANTIMISSILE))) {
//
//                        DrivenObject drObj = (DrivenObject) tgt;
//
//
//                        if (tgt.type.contains(ObjectType.PLAYER_SHIP)) {
//                            System.out.println("Player hitted by: " + prj.getClass().getSimpleName());
//                        }
//
//                        if (prj.type.contains(ObjectType.SHELL)) {
//                            drObj.health--;
//                        }
//
//                        if (prj.type.contains(ObjectType.ANTIMISSILE)) {
//                            drObj.health--;
//                        }
//
//                        if (prj.type.contains(ObjectType.BULLET)) {
//                            drObj.health -= 0.05;
//                        }
//
//                        // affect impact on target ship
//                        tmp2.set(prj.vel).scl(prj.getMass() / dt);
//                        drObj.applyForce(tmp2);
//
//                        prj.vel.setZero();
//                        prj.readyToDispose = true;
//
//                    }
/*                    // handle DRIVEN_OBJECT collision
                    else if (tgt.type.contains(ObjectType.DRIVEN_OBJECT) &&
                            (prj.type.contains(ObjectType.PROJECTILE) || prj.type.contains(ObjectType.ANTIMISSILE))) {

                        if (tgt.type.contains(ObjectType.PLAYER_SHIP)) {
                            System.out.println("Player killed by: " + prj.getClass().getSimpleName());
                        }

                        if (prj.type.contains(ObjectType.PLAYER_SHIP)) {
                            System.out.println("Player killed by: " + tgt.getClass().getSimpleName());
                        }

                        // destroy both
                        tgt.readyToDispose = true;
                        prj.readyToDispose = true;
                    }*/
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
            tmp0.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        }
        else {
            tmp0.set(playerShip.pos);
        }

        int cnt = 0;
        int nearCount;
        boolean foundPlace = true;

        do {

            nearCount =  0;

            float r = MathUtils.random(250, 600);
            float fi = MathUtils.random(0, (float) (2*Math.PI));

            float x = (float)(r * Math.cos(fi));
            float y = (float)(r * Math.sin(fi));


            tmp1.set(x,y);
            tmp2.set(tmp1).sub(tmp0);

            dummy = new DummyObject(10,null);
            dummy.pos.set(tmp1);
            List<GameObject> list = getCloseObjects(dummy, 200);


            for (int i = 0; i < list.size(); i++) {

                if (list.get(i).type.contains(ObjectType.DRIVEN_OBJECT)) {

                    nearCount = 1;
                    break;
                }
            }



            if (cnt++ >= 10) {
                foundPlace = false;
                break;
            }
        }
        while (tmp2.len() < 400 || nearCount > 0); // 500  - расстояние до корабля игрока



        if (foundPlace) {

            enemyShipsToSpawn--;

            EnemyShip enemyShip = new EnemyShip(new TextureRegion(enemyShipTexture), 50, null);
            enemyShip.pos = tmp1.cpy();
            enemyShip.name = "enemyship";

            addObject(enemyShip);
        }
    }


    /**
     * Will add new target to EnmyShips if they didn't have one
     */
    private void retargetEnemyShips() {


        for (GameObject obj : hittableObjects) {
            if (obj.type.contains(ObjectType.ENEMY_SHIP)) {

                EnemyShip ship = (EnemyShip) obj;

                if (ship.target == null) {

                    int cnt = 0;
                    do {

                        GameObject tmp;

                        int rnd = MathUtils.random(0, hittableObjects.size() - 1);
                        tmp = hittableObjects.get(rnd);


                        if (!tmp.readyToDispose &&
                                tmp != planet &&
                                tmp != ship && // self
                                tmp.type.contains(ObjectType.SHIP)) {

                            ship.target = tmp;
                        }

                        // Switch target to player ship only
                        if (!playerShip.readyToDispose) {
                            ship.target = playerShip;
                        }


                        if (cnt++ >= 100)
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
                obj.type.contains(ObjectType.SHIP)) {

            n = borderNormals.left;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.4f);
            obj.pos.x = 2 * leftBound + 2*obj.getRadius() - obj.pos.x;
        }

        // -----------------------------------------------------------------------------------------

        if (obj.pos.x + obj.getRadius() > 2f * rightBound) {
            obj.readyToDispose = true;
            return;
        }

        if (obj.pos.x + obj.getRadius() > rightBound &&
                obj.type.contains(ObjectType.SHIP)) {

            n = borderNormals.right;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.4f);
            obj.pos.x = 2 * rightBound - 2*obj.getRadius() - obj.pos.x;
        }

        // -----------------------------------------------------------------------------------------

        if (obj.pos.y - obj.getRadius() < 2f * downBound) {
            obj.readyToDispose = true;
            return;
        }

        if (obj.pos.y - obj.getRadius() < downBound &&
                obj.type.contains(ObjectType.SHIP)) {

            n = borderNormals.down;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.4f);
            obj.pos.y = 2 * downBound + 2*obj.getRadius() - obj.pos.y;

        }

        // -----------------------------------------------------------------------------------------

        if (obj.pos.y + obj.getRadius() > 2f * upBound) {
            obj.readyToDispose = true;
            return;
        }

        if (obj.pos.y + obj.getRadius() > upBound &&
                obj.type.contains(ObjectType.SHIP)) {

            n = borderNormals.up;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.vel.scl(0.4f);
            obj.pos.y = 2 * upBound - 2 *obj.getRadius() - obj.pos.y;
        }


    }



    // -----------------------------------------------------------------------------------------


    private void playExplosionSound(GameObject obj, GameObject target) {

        if (obj.type.contains(ObjectType.SHIP)) {

            expl01.play(1f);
        }
        else if (obj.type.contains(ObjectType.MISSILE) &&
                !obj.type.contains(ObjectType.ANTIMISSILE)&&
                !obj.type.contains(ObjectType.FRAGMISSILE)) {

            expl02.play(0.4f);
        }
        else if (obj.type.contains(ObjectType.FRAGMISSILE)) {
            bigExpl.play(1f);
        }
        else if (obj.type.contains(ObjectType.ANTIMISSILE)) {
            flak.play(0.3f);
        }
        else if (target != null &&
                (obj.type.contains(ObjectType.SHELL)/*||
                 obj.type.contains(ObjectType.FRAG)*/) &&
                target.type.contains(ObjectType.SHIP)) {

            metalHit.play();
        }




    }


    // -----------------------------------------------------------------------------------------

    private void checkWin() {


        // -----------------------------------------------------------------------------------------
        // game objective completed
        // -----------------------------------------------------------------------------------------
        if (!playerShip.readyToDispose && !win && !music.isPlaying()) {



            Message msg = new Message("Objectives completed. Leaving area.");
            particleObjects.add(msg);
            music = null;

            // haaaack - make player ship invincible while warp jumping
            gameObjects.remove(playerShip);
            hittableObjects.remove(playerShip);

            gameObjects.remove(playerShip.shield);
            hittableObjects.remove(playerShip.shield);

            particleObjects.add(playerShip);

            win = true;
        }

        // override manual throttle level
        if (win) {
            playerShip.maxThrottle = 500;
            playerShip.throttle = playerShip.maxThrottle;
        }

        // removing player ship
        if (!playerShip.readyToDispose &&
                playerShip.pos.len() > 2000) {

            playerShip.readyToDispose = true;
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


        // https://github.com/varunpant/Quadtree
        // Примеры как использовать - там же в tests

        List<Point<GameObject>> points = INSTANCE.quadTree.searchIntersect2(x1, y1, x2, y2);

        //remove invalid
        for (Point<GameObject> p : points) {

            if (!p.getValue().readyToDispose){
                result.add(p.getValue());
            }
        }


        //System.out.println("Before result.sort");
        result.sort((p1, p2) -> {

            tmp0s.set(p1.pos).sub(target.pos);
            tmp1s.set(p2.pos).sub(target.pos);

            return Float.compare(tmp0s.len(), tmp1s.len());

        });

        //System.out.println("Sorted.");



        return result;
    }




    public static Renderer getRenderer() {

        return INSTANCE.renderer;
    }



    private void getDifficultyLevel() {
        int rank = 1;
        try {
            FileHandle file = Gdx.files.absolute("config.ini");
            String rankString = file.readString();
            rank = Integer.parseInt(rankString.trim());
        }
        catch (Exception ignore){}

        switch (rank){

            default:
            case 1:

                // NEVER PLAYED
                ENEMY_RESPAWN_TIME = 1000;
                ENEMIES_COUNT_IN_WAVE = 1;

                break;

            case 2:

                // NOVICE
                ENEMY_RESPAWN_TIME = 500;
                ENEMIES_COUNT_IN_WAVE = 1;

                break;

            case 3:

                // EXPERIENCED
                ENEMY_RESPAWN_TIME = 700;
                ENEMIES_COUNT_IN_WAVE = 2;
                break;

            case 4:

                // SPECIALIST
                ENEMY_RESPAWN_TIME = 1200;
                ENEMIES_COUNT_IN_WAVE = 3;
                break;

            case 5:

                // IMPERIAL NAVY LIEUTENANT
                ENEMY_RESPAWN_TIME = 1500;
                ENEMIES_COUNT_IN_WAVE = 4;

                break;

            case 6:
                // IMPERIAL NAVY LORD-CAPITAN
                ENEMY_RESPAWN_TIME = 1700;
                ENEMIES_COUNT_IN_WAVE = 5;


                break;

        }
    }



    private void hitLogger(GameObject tgt, GameObject prj) {
        // logging
        if (tgt.getClass() == PlayerShip.class) {

            System.out.println("Player hitted by: " + prj.getClass().getSimpleName());

            if (prj.getClass() == Missile.class &&
                    prj.owner!= null &&
                    prj.owner.getClass() == PlayerShip.class) {

                System.out.println("Committed suicide");
            }

        }
    }


}






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



// ------------------------------------------------------------------------------------------