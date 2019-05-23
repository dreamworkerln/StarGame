package ru.geekbrains.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

import ru.geekbrains.entities.Kerbonaut;
import ru.geekbrains.math.Rect;
import ru.geekbrains.sprite.Background;
import ru.geekbrains.sprite.Reticle;

public class MenuScreen extends BaseScreen {

    private Background background;
    private Reticle reticle;
    private List<Kerbonaut> kerbonauts = new ArrayList<>();

    private MenuScreen.borderNormals borderNormals = new borderNormals();


    Vector2 buf0 = new Vector2();

    @Override
    public void show() {
        super.show();
        background = new Background(new TextureRegion(new Texture("background.jpg")));
        background.setHeightAndResize(1.2f);


        reticle = new Reticle(new TextureRegion(new Texture("reticle.png")));
        reticle.setHeightAndResize(0.05f);

        Kerbonaut jebediah = new Kerbonaut(new TextureRegion(new Texture("jebediah2.png")));
        jebediah.pos = new Vector2(+0.1f, +0.2f);
        jebediah.setHeightAndResize(0.07f);
        jebediah.target = target;         //add target
        kerbonauts.add(jebediah);


        //jebediah.vel = new Vector2(-0.2f,-0.2f);

        Kerbonaut valentina = new Kerbonaut(new TextureRegion(new Texture("valentina.png")));
        valentina.pos = new Vector2(-0.1f, -0.2f);
        valentina.setHeightAndResize(0.07f);
        valentina.target = jebediah.pos;  //add target
        kerbonauts.add(valentina);
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

        // batching
        batch.begin();
        background.draw(batch);
        reticle.draw(batch);

        for (Kerbonaut kerb : kerbonauts) {
            kerb.draw(batch);
        }
        batch.end();
    }


    

    @Override
    public void dispose() {

        background.dispose();

        for (Kerbonaut kerb : kerbonauts) {
            kerb.dispose();
        }
        
        super.dispose();
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

        // kerbonauts

//        // check kerbonaut collision
//        collide();


        for (Kerbonaut kerb : kerbonauts) {


            // check wall bouncing
            borderBounce(kerb);


            // -----------------------------------------------------------------------------------

            // apply medium resistance - proportionally speed
            kerb.mediumRes.set(kerb.vel);

            // medium resistance ~ vel -
            //obj.mediumRes.scl(-0.001f * (obj.vel.len() + 1000));

            //scale
            kerb.mediumRes.scl(-0.1f);

            // -----------------------------------------------------------------------------------

            // Kerbonaut jetpack thruster force

            kerb.vecTarget.set(kerb.target).sub(kerb.pos);
            kerb.thruster.set(kerb.vecTarget);

            if (kerb.vecTarget.len() > kerb.radius / 4f) {
                kerb.thruster.setLength(Kerbonaut.MAX_THRUSTER);
            }
            else {
                kerb.thruster.setLength(0);
            }

            // calc result force
            kerb.force.setZero(); //zeroing
            kerb.force.add(kerb.mediumRes); // media resistance
            if (kerb.fuel > 0) {
                kerb.force.add(kerb.thruster); // thruster
            }

            kerb.applyForce(); // calc resulting acceleration

            // update velocity, position
            kerb.update(dt);
        }

    }


    private void borderBounce(Kerbonaut kerb) {

        // wall bouncing ----------------------------------------
        //https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector

        Vector2 n;

        // reflected_vel=vel−2(vel⋅n)n, where n - unit normal vector
        if (kerb.pos.x - kerb.radius < worldBounds.getLeft()) {

            n = borderNormals.left;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            // move away from edge to avoid barrier penetration
            kerb.pos.x = 2 * worldBounds.getLeft() + 2*kerb.radius - kerb.pos.x;
        }

        if (kerb.pos.x + kerb.radius > worldBounds.getRight()) {

            n = borderNormals.right;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.x = 2 * worldBounds.getRight() - 2*kerb.radius - kerb.pos.x;
        }

        if (kerb.pos.y - kerb.radius < worldBounds.getBottom()) {

            n = borderNormals.down;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.y = 2 * worldBounds.getBottom() + 2*kerb.radius - kerb.pos.y;

        }

        if (kerb.pos.y + kerb.radius > worldBounds.getTop()) {

            n = borderNormals.up;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.y = 2 * worldBounds.getTop() - 2 *kerb.radius - kerb.pos.y;
        }
    }


//    private void collide() {
//
//        Kerbonaut kerb0 = kerbonauts.get(0);
//        Kerbonaut kerb1 = kerbonauts.get(1);
//
//        buf0.set(kerb0.pos).sub(kerb1.pos);
//
//        if (buf0.len() < kerb0.radius + kerb1.radius) {
//
//
//
//        }
//
//    }

}
