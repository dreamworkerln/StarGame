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
import ru.geekbrains.storage.Game;

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
        background.setHeightAndResize(1200f);


        reticle = new Reticle(new TextureRegion(new Texture("reticle.png")));
        reticle.setHeightAndResize(50f);

        Kerbonaut jebediah = new Kerbonaut(new TextureRegion(new Texture("jebediah2.png")));
        jebediah.pos = new Vector2(+100f, +200f);
        jebediah.setHeightAndResize(70f);
        jebediah.target = target;         //add target
        kerbonauts.add(jebediah);


        //jebediah.vel = new Vector2(-0.2f,-0.2f);

        Kerbonaut valentina = new Kerbonaut(new TextureRegion(new Texture("valentina.png")));
        valentina.pos = new Vector2(-100f, -200f);
        valentina.setHeightAndResize(70f);
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


        // background
        background.draw(renderer.batch);

        // coordinate axis
        renderer.shape.begin();
        //Gdx.gl.glLineWidth(2);
        renderer.shape.set(ShapeRenderer.ShapeType.Line);
        renderer.shape.setColor(Color.BLUE);
        renderer.shape.line(new Vector2(-1000f, 0f), new Vector2(1000, 0f));
        renderer.shape.line(new Vector2(0f, -1000f), new Vector2(0, 1000f));
        //Gdx.gl.glLineWidth(1);
        renderer.shape.end();

        // reticle
        reticle.draw(renderer.batch);

        // kerbonauts
        for (Kerbonaut kerb : kerbonauts) {
            kerb.draw(renderer);
        }
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

        for (Kerbonaut kerb : kerbonauts) {

            // check wall bouncing
            borderBounce(kerb);

            // -----------------------------------------------------------------------------------
            // update velocity, position
            kerb.force.setZero();
            kerb.update(dt);
        }

        Game.INSTANCE.updateTick();

    }


    protected void borderBounce(Kerbonaut kerb) {

        // wall bouncing ----------------------------------------
        //https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector

        Vector2 n;

        // Подстраиваем левую и правую стенки игрового мира(world) под соотношенире сторон устройства
        float leftBound = worldBounds.getLeft() * aspect;
        float rightBound = worldBounds.getRight() * aspect;

        float upBound = worldBounds.getTop();
        float downBound = worldBounds.getBottom();

        // reflected_vel=vel−2(vel⋅n)n, where n - unit normal vector
        if (kerb.pos.x - kerb.radius < leftBound) {

            n = borderNormals.left;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            // move away from edge to avoid barrier penetration
            kerb.pos.x = 2 * leftBound + 2*kerb.radius - kerb.pos.x;
        }

        if (kerb.pos.x + kerb.radius > rightBound) {

            n = borderNormals.right;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.x = 2 * rightBound - 2*kerb.radius - kerb.pos.x;
        }

        if (kerb.pos.y - kerb.radius < downBound) {

            n = borderNormals.down;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.y = 2 * downBound + 2*kerb.radius - kerb.pos.y;

        }

        if (kerb.pos.y + kerb.radius > upBound) {

            n = borderNormals.up;
            kerb.vel.x = kerb.vel.x - 2 *n.x * kerb.vel.dot(n);
            kerb.vel.y = kerb.vel.y - 2 *n.y * kerb.vel.dot(n);
            kerb.pos.y = 2 * upBound - 2 *kerb.radius - kerb.pos.y;
        }
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
