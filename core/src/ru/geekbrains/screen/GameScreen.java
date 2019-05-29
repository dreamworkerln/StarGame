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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ru.geekbrains.entities.DrivenObject;
import ru.geekbrains.entities.Explosion;
import ru.geekbrains.entities.GameObject;
import ru.geekbrains.entities.Guidance;
import ru.geekbrains.entities.Planet;
import ru.geekbrains.entities.PlayerShip;
import ru.geekbrains.entities.TrajectorySimulator;
import ru.geekbrains.math.Rect;
import ru.geekbrains.sprite.Background;
import ru.geekbrains.sprite.Reticle;
import ru.geekbrains.storage.Game;

public class GameScreen extends BaseScreen {

    private static Vector2 tmp0s = new Vector2();
    private static Vector2 tmp1s = new Vector2();

    private Vector2 tmp0 = new Vector2();
    private Vector2 tmp1 = new Vector2();
    private Vector2 tmp2 = new Vector2();



    private Background background;
    private Reticle reticle;
    private Planet planet;

    private Set<GameObject> gameObjects = new HashSet<>();
    private Set<GameObject> particleObjects = new HashSet<>();

    //private List<GameObject> objectsToDelete = new ArrayList<>();
    private GameScreen.borderNormals borderNormals = new borderNormals();

    private PlayerShip playerShip;



    private TrajectorySimulator trajectorySim;









    @Override
    public void show() {
        super.show();
        background = new Background(new TextureRegion(new Texture("A_Deep_Look_into_a_Dark_Sky.jpg")));
        background.setHeightAndResize(2000f);

        planet = new Planet(new TextureRegion(new Texture("dune.png")),100f);
        planet.pos = new Vector2(0, 0);

        target.set(500f,500f);
        reticle = new Reticle(new TextureRegion(new Texture("reticle.png")));
        reticle.setHeightAndResize(30f);


        playerShip = new PlayerShip(new TextureRegion(new Texture("ship_player.png")), 50);
        playerShip.pos = new Vector2(+700f, +700f);
        playerShip.target = null;         //add target
        playerShip.guidance = Guidance.MANUAL;
        playerShip.name = "playerShip";
        gameObjects.add(playerShip);

        trajectorySim = new TrajectorySimulator(playerShip, planet);


        for (int i= 0; i < 10; i++) {

            Texture enemyShipTexture = new Texture("ship_enemy.png");

            DrivenObject enemyShip = new DrivenObject(new TextureRegion(enemyShipTexture), 50);
            enemyShip.pos = new Vector2(MathUtils.random(-700, 700), MathUtils.random(-700, 700));
            enemyShip.target = playerShip;  //add target
            enemyShip.maxRotationSpeed *= 4;
            enemyShip.name = "enemyship_" + i;
            gameObjects.add(enemyShip);
        }
    }


    @Override
    public void render(float delta) {

        //DEBUG delta
        float dt = Game.INSTANCE.isDEBUG() ? 1/60f : delta;

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

        // trajectory sim
        trajectorySim.draw(renderer);

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



    class borderNormals {

        Vector2 left = new Vector2(1, 0);
        Vector2 right = new Vector2(-1, 0);
        Vector2 up = new Vector2(0, -1);
        Vector2 down = new Vector2(0, 1);
    }




    private void update(float dt) {

        // reticle
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

            // check wall bouncing
            borderBounce(obj);

            // check falling to planet
            checkPlanetCollide(obj);

            // update velocity, position
            obj.update(dt);

            // -------------------------------------------------------------------------------------

            // add obj to objectsToDelete
            if (obj.readyToDispose) {

                // removing from gameObjects
                it.remove();

                // check for create explosion
                if (obj instanceof DrivenObject) {

                    Explosion expl = new Explosion(obj.pos, obj.getRadius()*3);
                    expl.addSmokeTrail(((DrivenObject)obj).getSmokeTrail());
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

        // -----------------------------------------------------------------------------------------

        
//        // remove dead objects
//        for (GameObject o : objectsToDelete) {
//            gameObjects.remove(o);
//            o.dispose();
//        }
//        objectsToDelete.clear();

        // -----------------------------------------------------------------------------------------
        // simulate playerShip trajectory for future steps
        trajectorySim.update(dt);

        // -----------------------------------------------------------------------------------------

        // increment game tick
        Game.INSTANCE.updateTick();
    }


    public static void applyPlanetGravForce(GameObject obj, Planet planet) {

        if (obj == planet)
            return;



        // Newton's law of universal gravitation
        // F = G * m1*m2/r^2;

        tmp1s.set(planet.pos);
        tmp1s.sub(obj.pos);
        float G = 2f;
        float divider = tmp1s.len2();
        // avoid division by zero 
        if (divider < 0.0001)
            divider = 0.0001f;

        tmp0s = tmp1s.setLength(G*planet.getMass() * obj.getMass()/divider);
        obj.applyForce(tmp0s);
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

            n = borderNormals.left;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            // move away from edge to avoid barrier penetration
            obj.pos.x = 2 * leftBound + 2*obj.getRadius() - obj.pos.x;
        }

        if (obj.pos.x + obj.getRadius() > rightBound) {

            n = borderNormals.right;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.pos.x = 2 * rightBound - 2*obj.getRadius() - obj.pos.x;
        }

        if (obj.pos.y - obj.getRadius() < downBound) {

            n = borderNormals.down;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.pos.y = 2 * downBound + 2*obj.getRadius() - obj.pos.y;

        }

        if (obj.pos.y + obj.getRadius() > upBound) {

            n = borderNormals.up;
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.pos.y = 2 * upBound - 2 *obj.getRadius() - obj.pos.y;
        }
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

}




