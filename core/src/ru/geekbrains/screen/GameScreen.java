package ru.geekbrains.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;
import com.github.varunpant.quadtree.Point;
import com.github.varunpant.quadtree.QuadTree;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ru.geekbrains.entities.equipment.ForceShield;
import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.DummyObject;
import ru.geekbrains.entities.objects.JebediahKerman;
import ru.geekbrains.entities.objects.ObjectSide;
import ru.geekbrains.entities.objects.enemies.AbstractEnemyShip;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.enemies.BattleEnemyShip;
import ru.geekbrains.entities.objects.enemies.MainEnemyShip;
import ru.geekbrains.entities.objects.enemies.MissileEnemyShip;
import ru.geekbrains.entities.particles.Explosion;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.Planet;
import ru.geekbrains.entities.objects.PlayerShip;
import ru.geekbrains.entities.particles.Message;
import ru.geekbrains.entities.particles.SmokeTrailList;
import ru.geekbrains.entities.projectile.Ammo;
import ru.geekbrains.entities.projectile.missile.AbstractMissile;
import ru.geekbrains.entities.projectile.missile.EmpMissile;
import ru.geekbrains.entities.projectile.missile.Missile;
import ru.geekbrains.entities.projectile.missile.NewtonMissile;
import ru.geekbrains.math.Rect;
import ru.geekbrains.sprite.Background;
import ru.geekbrains.sprite.Reticle;

import org.apache.commons.lang3.time.DurationFormatUtils;


public class GameScreen extends BaseScreen {

    public static final float BACKGROUND_SIZE = 2050f;
    private static Vector2 tmp0s = new Vector2();
    private static Vector2 tmp1s = new Vector2();

    public static GameScreen INSTANCE = null;

    private static Texture missileTexture;
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
    private Vector2 tmp4 = new Vector2();
    private Vector2 tmp5 = new Vector2();
    private Vector2 tmp6 = new Vector2();
    private Vector2 tmp7 = new Vector2();
    private GameObject dummy;

    private int enemyShipsToSpawn = 0;

    public PlayerShip playerShip;
    public JebediahKerman kerman;
    public BattleEnemyShip bossShip = null;
    private boolean spawnBossShip = false;

    private static Texture mainEnemyShipTexture = new Texture("ship_enemy_main.png");
    private static Texture smallEnemyShipTexture = new Texture("ship_enemy_small.png");
    public static Texture bigEnemyShipTexture = new Texture("ship_enemy_big.png");


    private Map<String, Integer>  missileHitType = new HashMap<>();

    private Background background;
    private Reticle reticle;
    public  Planet planet;

    private Set<GameObject> spawningObjects = new HashSet<>(); // objects to spawn

    private LinkedList<GameObject> gameObjects = new LinkedList<>();

    public Set<GameObject> particleObjects = new HashSet<>();

    private Set<GameObject> explosionObjects = new HashSet<>();

    // Список объектов, по которым можно попадать снарядами
    // Используется в quadTree в качестве целей, отсортирован по убыванию радиуса
    private ArrayList<GameObject> hittableObjects = new ArrayList<>();

    //private Map<Float,GameObject> hittableObjects = new TreeMap<>((f1, f2) -> -Float.compare(f1, f2));

    //private List<GameObject> objectsToDelete = new ArrayList<>();
    private BorderNormals borderNormals = new BorderNormals();

    private QuadTree<GameObject> quadTree;

    private boolean win = false;

    //private Message msgRemains;
    private Music music;
    private Music musicLastStand;


    private Sound expl01;
    private Sound expl02;
    private Sound expl04;
    private Sound bigExpl;
    private Sound flak;
    private Sound flak_exp;
    private Sound metalHit;
    private Sound forTheEmperor;
    private Sound quack;

    private Message msgEST;
    private Message msgFuel;
    private int musicLength;

    private boolean forTheEmperorPlayed = false;




    private int ENEMY_RESPAWN_TIME;
    private int ENEMIES_COUNT_IN_WAVE;
    private int ENEMIES_COUNT_IN_WAVE_PREVOIUS;

    static {
        missileTexture = new Texture("M-45_missile2.png");
    }

    @Override
    public void show() {
        super.show();

        if (GameScreen.INSTANCE == null) {
            GameScreen.INSTANCE = this;
        }


        quadTree = new QuadTree<>(-15000,-15000,15000,15000);

        background = new Background(new TextureRegion(new Texture("A_Deep_Look_into_a_Dark_Sky.jpg")));
        background.setHeightAndResize(BACKGROUND_SIZE);

        planet = new Planet(new TextureRegion(new Texture("dune3.png")),100f, null);
        planet.pos = new Vector2(0, 0);


        target.set(500f,500f);
        reticle = new Reticle(new TextureRegion(new Texture("reticle.png")));
        reticle.setHeightAndResize(30f);

        playerShip = new PlayerShip(new TextureRegion(new Texture("ship_player.png")), 50, null);
        playerShip.pos = new Vector2(500f, 500f);
        playerShip.vel = new Vector2(0f, -10f);
        playerShip.target = null;         //add target
        //playerShip.guidance = Guidance.MANUAL;
        playerShip.name = "playerShip";
        //playerShip.gun.fireRate = 0.025f;

        //playerShip.vel = new Vector2(+100f, 0f);



        addObject(playerShip);

        Message msg = new Message("New objectives: survive till warp jump would be possible.", 0);
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


        //music = Gdx.audio.newMusic(Gdx.files.internal("Valves (remix) - Tiberian Sun soundtrack.mp3"));
        //music = Gdx.audio.newMusic(Gdx.files.internal("a0000019.ogg"));




        String musicFile;
        //String musicFile = "Lullaby.ogg";


        musicLastStand = Gdx.audio.newMusic(Gdx.files.internal("Last Stand.mp3"));

        musicFile = "Valves (remix) - Tiberian Sun soundtrack.ogg";
        musicLength = 60*5;


        //musicFile = "test_music2.mp3";
        //musicLength = 30;

        //musicFile = "304665_SOUNDDOGS__ca.mp3";
        //musicFile = "Quake_Champions_OST_Corrupted_Keep.mp3";





        music = Gdx.audio.newMusic(Gdx.files.internal(musicFile));





        forTheEmperor = Gdx.audio.newSound(Gdx.files.internal("FOR THE EMPEROR.mp3"));

        music.setVolume(1f);
        music.play();


        expl01 = Gdx.audio.newSound(Gdx.files.internal("expl01.mp3"));
        expl02 = Gdx.audio.newSound(Gdx.files.internal("expl02.mp3"));
        expl04 = Gdx.audio.newSound(Gdx.files.internal("expl04.mp3"));
        flak = Gdx.audio.newSound(Gdx.files.internal("flak.mp3"));
        flak_exp = Gdx.audio.newSound(Gdx.files.internal("flak_explosion2.ogg"));


        bigExpl = Gdx.audio.newSound(Gdx.files.internal("big_expl2.mp3"));
        metalHit = Gdx.audio.newSound(Gdx.files.internal("IMPACT CAN METAL HIT RING 01.mp3"));
        quack = Gdx.audio.newSound(Gdx.files.internal("quack2.mp3"));

        // DIFFICULTY LEVEL ------------------------------------------------------------------------
        getDifficultyLevel();


        // -----------------------------------------------------------------------------------------
    }




    private void update(float dt) {

        // spawnEnemyShip
        if (getTick() % ENEMY_RESPAWN_TIME == 0) {
            enemyShipsToSpawn += ENEMIES_COUNT_IN_WAVE;
        }

        if (enemyShipsToSpawn > 0)  {
            spawnEnemyShip();
        }

        spawnAllyShip();






        planet.update(dt);

        // Duration.Formatter.ofPattern("hh:mm:ss").format(dur);



        Duration remaining = Duration.ofSeconds(musicLength - (long)music.getPosition());

        //Duration current =   musicDuration.minus( music.getPosition(), ChronoUnit.SECONDS);
        msgEST.text = "EST: " + DurationFormatUtils.formatDuration(remaining.toMillis(), "mm:ss", true);

        if (playerShip!= null && !playerShip.readyToDispose) {
            msgFuel.text = "FUEL: " + (long)playerShip.fuel;
        }
        else if (kerman!= null && !kerman.readyToDispose) {
            msgFuel.text = "FUEL: " + (long)kerman.fuel;
        }



        if (remaining.getSeconds() <= ENEMY_RESPAWN_TIME/60 * 3 && ENEMIES_COUNT_IN_WAVE_PREVOIUS == ENEMIES_COUNT_IN_WAVE) {
            ENEMIES_COUNT_IN_WAVE++;
        }
        if (remaining.getSeconds() <= ENEMY_RESPAWN_TIME/60 * 2 && ENEMIES_COUNT_IN_WAVE_PREVOIUS == ENEMIES_COUNT_IN_WAVE - 1) {
            ENEMIES_COUNT_IN_WAVE++;
        }
        if (remaining.getSeconds() <= ENEMY_RESPAWN_TIME/60 && ENEMIES_COUNT_IN_WAVE_PREVOIUS == ENEMIES_COUNT_IN_WAVE - 2) {
            ENEMIES_COUNT_IN_WAVE++;
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

        quadTree.clear();       // NOT SO efficient
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

                obj.rotate(dt);

                // check wall bouncing
                borderBounce(obj);
            }
            // -------------------------------------------------------------------------------------

            // add obj to objectsToDelete
            else {

                // removing from gameObjects
                it.remove();

                if (obj.shouldExplode) {

                    playExplosionSound(obj, null);

                    // do not explode planet
                    if (!obj.type.contains(ObjectType.PLANET)) {
                        Explosion expl = new Explosion(obj);
                        explosionObjects.add(expl);
                    }
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


        // spawn boss

        if  (!music.isPlaying() && bossShip == null) {
            spawnBossShip = true;
            spawnEnemyShip();
            spawnBossShip = false;
            musicLastStand.play();

            Message msg = new Message("Weapon systems has been restored to full functionality.", 0);
            particleObjects.add(msg);


        }


        checkWin();

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
        //super.render(dt);

        // clear screen
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        // RENDER ALL BATCH TEXTURE OBJECTS ------------------------------------


        renderer.rendererType = RendererType.TEXTURE;

        renderer.batch.begin();

        background.draw(renderer);

        if (!planet.readyToDispose) {
            planet.draw(renderer);
        }



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


        // RENDER ALL FONT OBJECTS ------------------------------------

        renderer.rendererType = RendererType.FONT;

        renderer.batch.begin();

        // particleObjects
        for (GameObject obj : particleObjects) {
            obj.draw(renderer);
        }

        renderer.batch.end();

        // ---------------------------------------------------------------------------------------


        Duration du = Duration.between(inst, Instant.now());


        long milli = du.toMillis();

        // data processing (root finding) real-time violation
        if ( milli > (1/60d)*1000) {
            //System.out.println("TIME ERR >> " + milli);
        }
    }




    @Override
    public void resize(Rect worldBounds) {
        super.resize(worldBounds);


        // kludje
        if(msgEST == null && msgFuel == null) {

            msgEST = new Message("EST: ", 1);
            msgFuel = new Message("FUEL: ", 2);
            particleObjects.add(msgEST);
            particleObjects.add(msgFuel);
        }

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

        tmp1s.set(planet.pos).sub(obj.pos);

        float G = 2f;
        //float G = 0f;

        float divider = tmp1s.len2();
        // avoid division by zero 
        if (divider < 1)
            divider = 1f;

        tmp0s.set(tmp1s.setLength(G*planet.getMass() * obj.getMass()/divider));
        obj.applyForce(tmp0s);
    }







    private void collisionDetection(float dt) {

        ForceShield shield = playerShip.getShield();


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
            if (tgt == playerShip) {

                x1 = tgt.pos.x - 2.2*shield.getRadius();
                x2 = tgt.pos.x + 2.2*shield.getRadius();
                y1 = tgt.pos.y - 2.2*shield.getRadius();
                y2 = tgt.pos.y + 2.2*shield.getRadius();
            }




            List<Point<GameObject>> points = quadTree.searchIntersect2(x1, y1, x2, y2);

            if (points.size() <= 1) {
                shield.targetSet.clear();
            }


            for(int i = 0; i < points.size(); i++) {


                GameObject prj = points.get(i).getValue(); // projectile (may be DRIVEN_OBJECT)

                // уничтоженный объект не взаимодействует с другими, сам с собой тоже (в матрице по диагонали нули)
                if (prj.readyToDispose || tgt.readyToDispose  || tgt == prj)
                    continue;

                // vector from target to projectile
                tmp1.set(prj.pos).sub(tgt.pos);
                tmp4.set(prj.vel).sub(playerShip.vel);


                // FORCE SHIELD REPULSING ---------------------------------------------------
                if (tgt == playerShip &&
                    (prj.type.contains(ObjectType.PROJECTILE) || prj.type.contains(ObjectType.MISSILE)) &&


                    (tmp1.dot(tmp4) < 0  /*|| playerShip.shield.targetSet.contains(prj)*/)
                    //(prj.owner != tgt || prj.type.contains(ObjectType.FRAG))


                ) {

                    //PlayerShip plsp = playerShip;


                    if (tmp1.len() <= shield.getRadius() + prj.getRadius()) {


                        //plsp.shield.targetSet.add(prj);

                        Vector2 n = tmp3; // vector from target to projectile, normalized
                        n.set(prj.pos).sub(tgt.pos).nor();


                        // Силовое поле щита имеет потенциал Const/r,
                        // Соответственно сила поля, действующая на prj равна -n*prj.mass*Const*/r^2
                        // Как гравитационное поле, но со знаком "-"
                        tmp0.set(n.scl(prj.getMass()*shield.forceValue * shield.getRadius()/tmp1.len2()));
                        n = null;

                        // dA = m*E.dx // dx - ?

                        // dx = (x0=0) + (V0*t=0) + (a*t^2)/2

                        // a = tmp0/m = E

                        tmp3.set(tmp0).scl(1/prj.getMass());  // = E  напряженность поля щита

                        // dA = m * (tmp3)^2 * t^2/2; // работа щита на перемещение prj за dt

                        float dA =  (float)(tmp3.len2() * prj.getMass() * dt*dt*0.5);

                        //System.out.println(dA);

                        // EMP ordinance BLAST
                        if (prj.isEmpArmament) {
                            //prj.setMass(0.1f);

                            dA += prj.empDamage;
                            prj.readyToDispose = true;

                        }



                        //float currentA = Math.min(plsp.shield.power, dA);

                        // Если энергии поля не хватает совершить эту работу (не хватает запасенной энергии)
                        // то уменьшим силу, действующую на prj/plsp
                        if (dA > shield.power) {
                            tmp0.scl(shield.power/dA);
                        }


                        // отражаем снаряд
                        prj.applyForce(tmp0);
                        // 3 закон Ньютона - отражаем корабль
                        playerShip.applyForce(tmp0.scl(-1));

                        // depleting power shield
                        shield.power -= Math.min(shield.power, dA);

                        //System.out.println(dA);


                        // абсолютно неупругое столкновение
                        // affect impact on target ship
                        // tmp2.set(prj.vel).scl(prj.getMass() / dt);
                        // plsp.applyForce(tmp2);

                        if(prj.type.contains(ObjectType.BULLET)) {

                            prj.angVel += prj.dir.dot(playerShip.dir) > 0 ? 5 : -5;
                        }



                    }
                    else {
                        //plsp.shield.targetSet.remove(prj);
                    }
                }

                // END FORCE SHIELD ---------------------------------------------------------------



                if (tmp1.len() <= tgt.getRadius() + prj.getRadius()) {

                    // fall on planet
                    if (tgt == planet) {

                        tmp6.set(prj.pos).sub(tgt.pos).nor().rotate90(0);
                        float angle = tmp6.angle(prj.vel);

                        angle = Math.abs(angle);
                        int clockwise = -1;
                        if (angle > 90) {
                            angle =  180 - angle;
                            clockwise = 1;
                        }


                        float coeff = 3;
                        if(prj.type.contains(ObjectType.PLAYER_KERMAN)) {
                            coeff = tgt.getRadius();

                            if(tmp1.len() < tgt.getRadius()/2f) {
                                prj.shouldExplode = false;
                            }
                        }

                        // check reflection on planet atmosphere
                        if (angle < 45 && tmp1.len() > tgt.getRadius() + prj.radius  - coeff) {
                            prj.vel.rotate(angle * clockwise).scl(1f);
                            prj.vel.scl(0.99f);
                        }
                        else  {
                            // stop projectile - fallen on planet
                            prj.vel.setZero();

                            // stop proj smoke trail
                            if (prj instanceof SmokeTrailList) {
                                ((SmokeTrailList) prj).stop();
                            }

                            planet.hit(prj);
                            // destroy projectile (or driven object)
                            prj.readyToDispose = true;
                        }
                    }
                    else {

                        float effectiveArmor;// armour effectiveness
                        float amount;// damage amount


                        // повреждаем цель
                        effectiveArmor =   tgt.armour *(1 - prj.penetration);
                        amount = prj.damage * (1 - effectiveArmor);
                        tgt.doDamage(amount);


                        // повреждаем снаряд
                        effectiveArmor =   prj.armour *(1 - tgt.penetration);
                        amount = tgt.damage * (1 - effectiveArmor);
                        prj.doDamage(amount);


                        if(!prj.type.contains(ObjectType.BLACKHOLE_SHELL) &&
                            !tgt.type.contains(ObjectType.BLACKHOLE_SHELL)) {


                            // отталкиваем цель при попадании в нее ракет/снарядов

                            float expCoef;
                            float elasticCollision;
                            expCoef = 0;
                            elasticCollision = 1;

                            //if (!prj.readyToDispose) {
                            if (tgt.type.contains(ObjectType.BASIC_MISSILE) && !prj.isEmpArmament) {
                                expCoef = tgt.damage > 1 ? tgt.damage : 1;
                            }
                            if (tgt.type.contains(ObjectType.SHIP)) {
                                elasticCollision = 0.5f;
                            }
                            tmp3.set(tgt.vel).sub(prj.vel);
                            tmp5.set(prj.pos).sub(tgt.pos).nor().setLength(tmp3.len());
                            prj.applyForce(tmp5.scl(tgt.getMass() / dt * elasticCollision + expCoef));


                            expCoef = 0;
                            elasticCollision = 1;
                            //if (!tgt.readyToDispose) {
                            if (prj.type.contains(ObjectType.BASIC_MISSILE) && !prj.isEmpArmament) {
                                expCoef = prj.damage > 1 ? prj.damage : 1;
                            }
                            if (prj.type.contains(ObjectType.SHIP)) {
                                elasticCollision = 0.5f;
                            }
                            tmp3.set(prj.vel).sub(tgt.vel);
                            tmp5.set(tgt.pos).sub(prj.pos).nor().setLength(tmp3.len());
                            tgt.applyForce(tmp5.scl(prj.getMass() / dt * elasticCollision + expCoef));


                            tmp3.set(tgt.vel).sub(prj.vel);
                            if ((prj.type.contains(ObjectType.BULLET) || prj.type.contains(ObjectType.FRAG)) && (tgt.type.contains(ObjectType.SHIP) || tgt.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE))) {
                                tmp5.set(prj.pos).sub(tgt.pos).nor().setLength(tmp3.len()).scl(0.5f);
                                prj.vel.set(tmp5);
                            }
                            if (prj.type.contains(ObjectType.SHELL) && (tgt.type.contains(ObjectType.SHIP) || tgt.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE))) {
                                prj.vel.set(tgt.vel);
                            }

                            tmp3.set(prj.vel).sub(tgt.vel);
                            if ((tgt.type.contains(ObjectType.BULLET) || prj.type.contains(ObjectType.FRAG)) && (prj.type.contains(ObjectType.SHIP) || prj.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE))) {
                                tmp5.set(tgt.pos).sub(prj.pos).nor().setLength(tmp3.len()).scl(0.5f);
                                tgt.vel.set(tmp5);
                            }
                            if (tgt.type.contains(ObjectType.SHELL) && (prj.type.contains(ObjectType.SHIP) || prj.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE))) {
                                tgt.vel.set(prj.vel);
                            }
                        }

                        playExplosionSound(prj, tgt);

                        hitLogger(tgt, prj);
                        hitLogger(prj, tgt);

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

//        for (GameObject obj : gameObjects) {
//            obj.dispose();
//        }
//
//        for (GameObject obj : hittableObjects) {
//            obj.dispose();
//        }
//
//        for (GameObject obj : spawningObjects) {
//            obj.dispose();
//        }


        System.out.println("\nAA missiles down stats: ----------");

        for (Map.Entry<String, Integer> entry : missileHitType.entrySet()) {

            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("---------");


        super.dispose();
    }


    private void spawnEnemyShip() {

        if(bossShip!= null && !bossShip.readyToDispose) {
            return;
        }

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

            //float r = MathUtils.random(250, 600);
            //float fi = MathUtils.random(0, (float) (2*Math.PI));

            //float x = (float)(r * Math.cos(fi));
            //float y = (float)(r * Math.sin(fi));




            float x = MathUtils.random(-worldBounds.getHalfWidth() * aspect + 25, worldBounds.getHalfWidth() * aspect - 25);
            float y = MathUtils.random(-worldBounds.getHalfWidth() + 25, worldBounds.getHalfWidth() - 25);

            tmp1.set(x,y);
            tmp2.set(tmp1).sub(tmp0);


            tmp3.set(tmp1).sub(planet.pos);
            if (tmp3.len() < 400) {
                nearCount = 1;
            }


            dummy = new DummyObject(10,null);
            dummy.pos.set(tmp1);

            Predicate<GameObject> filter = o -> o.type.contains(ObjectType.DRIVEN_OBJECT) ||
                o.type.contains(ObjectType.PLANET);


            List<GameObject> list = getCloseObjects(dummy, 200, filter);

//            list = list.stream().filter(o ->
//                o.type.contains(ObjectType.DRIVEN_OBJECT) ||
//                    o.type.contains(ObjectType.PLANET)).collect(Collectors.toList());

            if (list.size() > 0) {
                nearCount ++;
            }

//            for (int i = 0; i < list.size(); i++) {
//
//                if (list.get(i).type.contains(ObjectType.DRIVEN_OBJECT) ||
//                        list.get(i).type.contains(ObjectType.PLANET)) {
//
//                    nearCount = 1;
//                    break;
//                }
//            }



            if (cnt++ >= 10) {
                foundPlace = false;
                break;
            }
        }
        while (tmp2.len() < 700 || nearCount > 0); // 500  - расстояние до корабля игрока







        if (foundPlace) {

            enemyShipsToSpawn--;

            AbstractEnemyShip enemyShip;
            Duration remaining = Duration.ofSeconds(musicLength - (long)music.getPosition());


            if(spawnBossShip && bossShip == null) {

                bossShip = new BattleEnemyShip(new TextureRegion(bigEnemyShipTexture), 120, null);
                bossShip.pos = tmp1.cpy();
                bossShip.name = "boss_enemy_ship";
                bossShip.side = ObjectSide.ENEMIES;
                addObject(bossShip);
            }
            else if (ThreadLocalRandom.current().nextFloat() > 0.87) {

                //new NewtonMissile(new TextureRegion(MISSILE_TEXTURE), 5, null);

                //Missile missile = new NewtonMissile(new TextureRegion(MISSILE_TEXTURE), 5, null);
                AbstractMissile missile = new NewtonMissile(new TextureRegion(missileTexture), 6, null);
                missile.pos = tmp1.cpy();
                missile.target = playerShip;
                missile.maxRotationSpeed *= 1.5f;
                missile.side = ObjectSide.ENEMIES;
                addObject(missile);

            }
            else {

                if (ThreadLocalRandom.current().nextFloat() > 0.4) {
                    enemyShip = new MainEnemyShip(new TextureRegion(mainEnemyShipTexture), 50, null);
                }
                else {
                    enemyShip = new MissileEnemyShip(new TextureRegion(smallEnemyShipTexture), 40, null);
                }

                enemyShip.pos = tmp1.cpy();
                enemyShip.name = "enemyship";

                addObject(enemyShip);
            }


        }

    }



    private void spawnAllyShip() {
        if (getTick() > 0 &&
            getTick() % 3600 == 0) {

            AbstractMissile missile = new NewtonMissile(new TextureRegion(missileTexture), 7, playerShip);
            missile.pos.set(worldBounds.getHalfWidth() * aspect, worldBounds.getHalfHeight());
            missile.engineTrail.color = new Color(0.6f, 0.6f, 0.8f, 1);
            missile.setMaxHealth(missile.getMaxHealth()*3);
            missile.setMaxThrottle(missile.throttle*1.5f);
            missile.maxRotationSpeed *= 2;
            missile.side = ObjectSide.ALLIES;

            addObject(missile);

            Message msg = new Message("Reinforcements have arrived.", 0);
            particleObjects.add(msg);
        }
    }


    /**
     * Will add new target to EnmyShips if they didn't have one
     */
    private void retargetEnemyShips() {


        for (GameObject obj : hittableObjects) {

            if ((obj.type.contains(ObjectType.ENEMY_SHIP) ||
                obj.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) &&
                    !obj.type.contains(ObjectType.BATTLE_ENEMY_SHIP) &&
                    obj.side == ObjectSide.ENEMIES) {

                DrivenObject drv = (DrivenObject) obj;

                if (drv.target == null || drv.target.readyToDispose) {

                    if (playerShip != null && !playerShip.readyToDispose) {
                        drv.target = playerShip;
                    }

                    else {

                        Predicate<GameObject> filter = t -> (!t.readyToDispose && t != drv &&
                            (t.owner == null || t.owner != drv) &&
                            (t.type.contains(ObjectType.SHIP) ||
                                t.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) ||
                                t.type.contains(ObjectType.PLAYER_KERMAN)));

                        List<GameObject> targets = GameScreen.getCloseObjects(drv, 4000, filter);




//                        targets.removeIf(t -> (t.readyToDispose || t == drv ||
//                            t.owner != null && t.owner == drv ||
//                            !t.type.contains(ObjectType.SHIP) &&
//                                !t.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) &&
//                                !t.type.contains(ObjectType.PLAYER_KERMAN)));

                        if (targets.size() > 0) {
                            drv.target = targets.get(0);
                        }
                    }



                    // Switch target to player ship only
//                    if (playerShip != null && !playerShip.readyToDispose && drv.target!= null && (
//
//                        drv.target.type.contains(ObjectType.ENEMY_SHIP) ||
//                        drv.target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) && ship.target.owner == null)) {
//
//                        ship.target = playerShip;
//                    }



//                    int cnt = 0;
//                    do {
//
//                        GameObject tmp;
//
//                        int rnd = MathUtils.random(0, hittableObjects.size() - 1);
//                        tmp = hittableObjects.get(rnd);
//
//
//                        if (!tmp.readyToDispose &&
//                                tmp != planet &&
//                                tmp != ship && // self
//                                tmp.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
//
//                            ship.target = tmp;
//                        }
//
//                        // Switch target to player ship only
//                        if (playerShip != null && !playerShip.readyToDispose &&
//
//                                !ship.target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) &&
//                                ship.target.owner != null &&
//                                ship.target.owner !=playerShip) {
//
//                            ship.target = playerShip;
//                        }
//
//
//                        if (cnt++ >= 100)
//                            break;
//                    }
//                    while (ship.target == null);

                }
            }
        }

    }




    private void borderBounce(GameObject obj) {

        if (obj.type.contains(ObjectType.SHIP) ||
            obj.type.contains(ObjectType.PLAYER_KERMAN) ||
            obj.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {


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
            if (obj.pos.x - obj.getRadius() < leftBound) {

                n = borderNormals.left;
                obj.vel.x = obj.vel.x - 2 * n.x * obj.vel.dot(n);
                obj.vel.y = obj.vel.y - 2 * n.y * obj.vel.dot(n);
                obj.vel.scl(0.2f);
                obj.pos.x = 2 * leftBound + 2 * obj.getRadius() - obj.pos.x;
            }

            // -----------------------------------------------------------------------------------------

            if (obj.pos.x + obj.getRadius() > 2f * rightBound) {
                obj.readyToDispose = true;
                return;
            }

            if (obj.pos.x + obj.getRadius() > rightBound) {

                n = borderNormals.right;
                obj.vel.x = obj.vel.x - 2 * n.x * obj.vel.dot(n);
                obj.vel.y = obj.vel.y - 2 * n.y * obj.vel.dot(n);
                obj.vel.scl(0.2f);
                obj.pos.x = 2 * rightBound - 2 * obj.getRadius() - obj.pos.x;
            }

            // -----------------------------------------------------------------------------------------

            if (obj.pos.y - obj.getRadius() < 2f * downBound) {
                obj.readyToDispose = true;
                return;
            }

            if (obj.pos.y - obj.getRadius() < downBound) {

                n = borderNormals.down;
                obj.vel.x = obj.vel.x - 2 * n.x * obj.vel.dot(n);
                obj.vel.y = obj.vel.y - 2 * n.y * obj.vel.dot(n);
                obj.vel.scl(0.2f);
                obj.pos.y = 2 * downBound + 2 * obj.getRadius() - obj.pos.y;

            }

            // -----------------------------------------------------------------------------------------

            if (obj.pos.y + obj.getRadius() > 2f * upBound) {
                obj.readyToDispose = true;
                return;
            }

            if (obj.pos.y + obj.getRadius() > upBound) {

                n = borderNormals.up;
                obj.vel.x = obj.vel.x - 2 * n.x * obj.vel.dot(n);
                obj.vel.y = obj.vel.y - 2 * n.y * obj.vel.dot(n);
                obj.vel.scl(0.2f);
                obj.pos.y = 2 * upBound - 2 * obj.getRadius() - obj.pos.y;
            }

        }


    }






    // -----------------------------------------------------------------------------------------

    private void checkWin() {





        // -----------------------------------------------------------------------------------------
        // game objective completed
        // -----------------------------------------------------------------------------------------
        if (!playerShip.readyToDispose && !win && !music.isPlaying() && bossShip!= null && bossShip.readyToDispose) {

            enemyShipsToSpawn = 0;
            musicLastStand.stop();

            ForceShield shield = playerShip.getShield();

            Message msg = new Message("You win", 0);
            particleObjects.add(msg);
            //music = null;

            // haaaack - make player ship invincible while warp jumping
            gameObjects.remove(playerShip);
            hittableObjects.remove(playerShip);

            gameObjects.remove(shield);
            hittableObjects.remove(shield);

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

    public static void addAmmo(Ammo obj) {

        INSTANCE.spawningObjects.add((GameObject) obj);
    }

//    public static void addObject(Ammo ammo) {
//
//        INSTANCE.spawningObjects.add((GameObject) ammo);
//    }


    public static void addParticleObject(GameObject obj) {

        INSTANCE.particleObjects.add(obj);
    }

    public static List<GameObject> getHittableObjects() {
        return INSTANCE.hittableObjects;
    }

    public static GameScreen getInstance() {
        return INSTANCE;
    }

    public static GameObject getPlanet() {

        return INSTANCE.planet;
    }


    /**
     *
     * @param target
     * @param radius
     * @param filter leave only this objects
     * @return
     */
    public static List<GameObject> getCloseObjects(GameObject target, float radius, Predicate<GameObject> filter) {

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

            if (p.getValue()!= null && !p.getValue().readyToDispose && filter.test(p.getValue())) {
                result.add(p.getValue());
            }
        }


        //System.out.println("Before result.sort");
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



    private void getDifficultyLevel() {
        int rank = 1;
        try {

            Properties prop = new Properties();
            String fileName = "config.ini";
            InputStream stream = null;
            try {
                stream = new FileInputStream(fileName);
            }
            catch (Exception ignore) {}
            try {
                prop.load(stream);
            }
            catch (Exception ignore) {}
            rank  = Integer.parseInt(prop.getProperty("app.rank").trim());
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
                ENEMY_RESPAWN_TIME = 1000;
                ENEMIES_COUNT_IN_WAVE = 2;
                break;

            case 3:
                // EXPERIENCED
                ENEMY_RESPAWN_TIME = 1000;
                ENEMIES_COUNT_IN_WAVE = 3;
                break;

            case 4:
                // SPECIALIST
                ENEMY_RESPAWN_TIME = 1400;
                ENEMIES_COUNT_IN_WAVE = 4;
                break;

            case 5:
                // IMPERIAL NAVY ENSIGN
                ENEMY_RESPAWN_TIME = 1600;
                ENEMIES_COUNT_IN_WAVE = 5;
                break;

            case 6:
                // IMPERIAL NAVY LIEUTENANT
                ENEMY_RESPAWN_TIME = 2000;
                ENEMIES_COUNT_IN_WAVE = 6;
                break;

            case 7:
                // IMPERIAL NAVY LORD-LIEUTENANT
                ENEMY_RESPAWN_TIME = 2200;
                ENEMIES_COUNT_IN_WAVE = 7;
                break;

            case 8:
                // IMPERIAL NAVY COMMANDER
                ENEMY_RESPAWN_TIME = 2400;
                ENEMIES_COUNT_IN_WAVE = 8;
                break;

            case 9:
                // IMPERIAL NAVY CAPITAN
                ENEMY_RESPAWN_TIME = 2500;
                ENEMIES_COUNT_IN_WAVE = 9;
                break;

            case 99:
                // DEBUG
                ENEMY_RESPAWN_TIME = 2000;
                ENEMIES_COUNT_IN_WAVE = 14;
                break;
        }

        //ENEMY_RESPAWN_TIME = 1;
        //ENEMIES_COUNT_IN_WAVE = 0;

        ENEMIES_COUNT_IN_WAVE_PREVOIUS = ENEMIES_COUNT_IN_WAVE;
    }



    private void hitLogger(GameObject tgt, GameObject prj) {
        // logging
        if (tgt.type.contains(ObjectType.PLAYER_SHIP)) {

            System.out.println("Player hitted by: " + prj.getClass().getSimpleName());

            if (prj.type.contains(ObjectType.MISSILE) &&
                prj.owner!= null &&
                prj.owner.type.contains(ObjectType.PLAYER_SHIP)) {

                System.out.println("COMMITTED SUICIDE");
            }
        }

        // AA system targets down statistic
        if (tgt.type.contains(ObjectType.BASIC_MISSILE) &&
            tgt.readyToDispose &&
            prj.owner == playerShip && prj.getClass() != NewtonMissile.class) {

            int val = missileHitType.getOrDefault(prj.getClass().getSimpleName(), 0) + 1;
            missileHitType.put(prj.getClass().getSimpleName(), val);
        }

    }

    // -----------------------------------------------------------------------------------------


    private void playExplosionSound(GameObject obj, GameObject target) {

        if (obj.type.contains(ObjectType.SHIP)) {

//             if(obj.type.contains(ObjectType.PLAYER_SHIP)) {
//                 quack.play(1f);
//            }
//            else{
//                expl01.play(1f);
//            }
            expl01.play(1f);
        }
        else if (obj.type.contains(ObjectType.MISSILE) &&
            !obj.type.contains(ObjectType.ANTIMISSILE)&&
            !obj.type.contains(ObjectType.PLASMA_FRAG_MISSILE)) {

            expl02.play(0.6f);
        }
        else if (obj.type.contains(ObjectType.PLASMA_FRAG_MISSILE)) {
            expl04.play(1f);
        }
        else if (obj.type.contains(ObjectType.ANTIMISSILE)) {
            flak.play(0.3f);
        }
        else if (target != null &&
            (obj.type.contains(ObjectType.SHELL)||
                obj.type.contains(ObjectType.PLASMA_FRAG)) &&
            (target.type.contains(ObjectType.SHIP) && !target.type.contains(ObjectType.BATTLE_ENEMY_SHIP) /* ||
                 target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)*/)) {

            metalHit.play();
        }
        else if (target != null &&
            obj.type.contains(ObjectType.SHELL) &&
            (target.type.contains(ObjectType.BATTLE_ENEMY_SHIP)|| target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE))) {

            metalHit.play();
        }


        else if (obj.type.contains(ObjectType.FLAK_SHELL)) {
            flak_exp.play(0.5f);

        }
        else if (obj.type.contains(ObjectType.PLAYER_KERMAN)) {
            quack.play();
        }

        if (obj.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {

            bigExpl.play(1f);
            bigExpl.play(1f);
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
