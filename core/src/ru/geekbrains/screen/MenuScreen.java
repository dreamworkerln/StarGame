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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.DrivenObject;
import ru.geekbrains.entities.GameObject;
import ru.geekbrains.entities.Guidance;
import ru.geekbrains.entities.Kerbonaut;
import ru.geekbrains.entities.Planet;
import ru.geekbrains.entities.Player;
import ru.geekbrains.math.Rect;
import ru.geekbrains.sprite.Background;
import ru.geekbrains.sprite.Reticle;
import ru.geekbrains.storage.Game;

public class MenuScreen extends BaseScreen {

    private Background background;
    private Reticle reticle;
    private Planet planet;
    private Set<GameObject> gameObjects = new HashSet<>();
    private List<GameObject> objectsToDelete = new ArrayList<>();
    private MenuScreen.borderNormals borderNormals = new borderNormals();

    private Player player;
    private GameObject trajectorySim;
    private ArrayList<Vector2> trajectorySimulated  = new ArrayList<>();


    private Vector2 tmp0 = new Vector2();
    private Vector2 tmp1 = new Vector2();
    private Vector2 tmp2 = new Vector2();




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


        player = new Player(new TextureRegion(new Texture("ship_player.png")), 50);
        player.pos = new Vector2(+700f, +700f);
        player.target = null;         //add target
        player.guidance = Guidance.MANUAL;
        player.name = "player";
        gameObjects.add(player);


        // trajectory sim
        trajectorySim = new GameObject(new TextureRegion(new Texture("ship_player.png")), 50);





        for (int i= 0; i < 10; i++) {

            Texture enemyShipTexture = new Texture("ship_enemy.png");

            DrivenObject enemyShip = new DrivenObject(new TextureRegion(enemyShipTexture), 50);
            enemyShip.pos = new Vector2(MathUtils.random(-700, 700), MathUtils.random(-700, 700));
            enemyShip.target = player.pos;  //add target
            enemyShip.maxRotationSpeed *= 4;
            enemyShip.name = "enemyship_" + i;
            gameObjects.add(enemyShip);
        }
    }


    @Override
    public void render(float delta) {

        // perform simulation step
        update(delta);

        // rendering
        super.render(delta);

        // clear screen
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        // background

        background.draw(renderer.batch);
        planet.draw(renderer);


        // coordinate axis
        renderer.shape.begin();
        //Gdx.gl.glLineWidth(2);
        renderer.shape.set(ShapeRenderer.ShapeType.Line);
        renderer.shape.setColor(Color.BLUE);
        renderer.shape.line(new Vector2(-1000f, 0f), new Vector2(1000, 0f));
        renderer.shape.line(new Vector2(0f, -1000f), new Vector2(0, 1000f));
        //Gdx.gl.glLineWidth(1);
        renderer.shape.end();


        // player trajectory sim

        renderer.shape.begin();
        Gdx.gl.glLineWidth(1);
        renderer.shape.set(ShapeRenderer.ShapeType.Line);
        renderer.shape.setColor(Color.YELLOW);

        for (int i = 0; i< trajectorySimulated.size() -2; i++) {
            renderer.shape.line(trajectorySimulated.get(i), trajectorySimulated.get(i+1));
        }
        renderer.shape.end();

        // reticle
        reticle.draw(renderer.batch);

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

        // gameObjects

        for (GameObject obj : gameObjects) {

            // zeroing force , applied to object
            obj.force.setZero();

            // calculate gravitation force from planet
            applyPlanetGravForce(obj);

            // check wall bouncing
            borderBounce(obj);

            // check falling to planet
            checkPlanetCollide(obj);

            // update velocity, position
            obj.update(dt);

            // -------------------------------------------------------------------------------------

            // add obj to objectsToDelete
            if (obj.readyToDispose) {
                objectsToDelete.add(obj);
            }
        }

        // simulate player trajectory to future steps
        simulatePlayerTrajectory();

        // remove dead objects
        for (GameObject o : objectsToDelete) {
            gameObjects.remove(o);
            o.dispose();
        }
        objectsToDelete.clear();

        // increment game clock
        Game.INSTANCE.updateTick();
    }


    @Override
    public void dispose() {

        background.dispose();

        for (GameObject kerb : gameObjects) {
            kerb.dispose();
        }

        super.dispose();
    }

    private void applyPlanetGravForce(GameObject obj) {

        if (obj == planet)
            return;

        // Newton's law of universal gravitation
        // F = G * m1*m2/r^2;

        tmp1.set(planet.pos);
        tmp1.sub(obj.pos);
        float G = 2f;
        float divider = tmp1.len2();
        // avoid division by zero 
        if (divider < 0.001)
            divider = 0.001f;

        tmp0 = tmp1.setLength(G*planet.mass * obj.mass/divider);
        obj.force.add(tmp0);
    }


    protected void borderBounce(GameObject kerb) {

        // wall bouncing ----------------------------------------
        //https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector

        Vector2 n;

        // Подстраиваем левую и правую стенки игрового мира(world) под соотношенире сторон устройства
        float leftBound = worldBounds.getLeft() * aspect;
        float rightBound = worldBounds.getRight() * aspect;

        float upBound = worldBounds.getTop();
        float downBound = worldBounds.getBottom();

        // reflected_vel=vel−2(vel⋅n)n, where n - unit normal vector
        if (kerb.pos.x - kerb.getRadius() < leftBound) {

            n = borderNormals.left;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            // move away from edge to avoid barrier penetration
            kerb.pos.x = 2 * leftBound + 2*kerb.getRadius() - kerb.pos.x;
        }

        if (kerb.pos.x + kerb.getRadius() > rightBound) {

            n = borderNormals.right;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.x = 2 * rightBound - 2*kerb.getRadius() - kerb.pos.x;
        }

        if (kerb.pos.y - kerb.getRadius() < downBound) {

            n = borderNormals.down;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.y = 2 * downBound + 2*kerb.getRadius() - kerb.pos.y;

        }

        if (kerb.pos.y + kerb.getRadius() > upBound) {

            n = borderNormals.up;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.y = 2 * upBound - 2 *kerb.getRadius() - kerb.pos.y;
        }
    }


    private void checkPlanetCollide(GameObject obj) {

        tmp1.set(planet.pos);
        tmp1.sub(obj.pos);

        if (tmp1.len() <= planet.getRadius() + obj.getRadius()) {
            obj.explode();
        }
    }




    private void simulatePlayerTrajectory() {


        trajectorySimulated.clear();
        trajectorySim.pos = player.pos.cpy();
        trajectorySim.vel = player.vel.cpy();

        float dt = 1/60f;


        for (int i = 0; i < 1500; i++) {

            // zeroing force , applied to object
            trajectorySim.force.setZero();

            // calculate gravitation force from planet
            applyPlanetGravForce(trajectorySim);

            // check falling to planet
            tmp1.set(planet.pos);
            tmp1.sub(trajectorySim.pos);
            if (tmp1.len() <= planet.getRadius() + trajectorySim.getRadius()) {
                break;
            }
            
            // update velocity, position
            trajectorySim.update(dt);

            // add new point to simulated trajectory
            trajectorySimulated.add(trajectorySim.pos.cpy());
        }

    }

}




