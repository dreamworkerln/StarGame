package ru.geekbrains.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.time.Duration;
import java.time.Instant;

import ru.geekbrains.entities.GameObject;

public class MenuScreen extends BaseScreen {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private GameObject obj;
    private Instant tick;
    private BitmapFont font;

    private Normals normals = new Normals();

    private Vector2 target = new Vector2(0,0);
    private Vector2 vecToTarget = new Vector2();

    @Override
    public void show() {
        super.show();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(5f);
        font.setColor(Color.RED);
        tick = Instant.now();
        obj = new GameObject("badlogic.jpg");
        obj.pos = new Vector2(obj.radius + 100, obj.radius + 100);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        step(delta);


        Gdx.gl.glClearColor(0.4f, 0.3f, 0.9f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // target
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(target.x, target.y, 32);
        shapeRenderer.end();

        batch.begin();
        obj.draw(batch);

        if(obj.fuel <=0) {
            font.draw(batch, "Out of fuel",
                    Gdx.graphics.getWidth() / 2f - 100,
                    Gdx.graphics.getHeight() / 4f);
        }


        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        obj.dispose();

        super.dispose();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        super.touchDown(screenX, screenY, pointer, button);

        screenY = Gdx.graphics.getHeight() - screenY;

        // set target
        target = target.set(screenX, screenY);
        return false;
    }


    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        super.touchDragged(screenX, screenY, pointer);

        screenY = Gdx.graphics.getHeight() - screenY;

        // set target
        target = target.set(screenX, screenY);

        return false;
    }

    // ------------------------------------------------------------------------------------




    class Normals {

        Vector2 left = new Vector2(1, 0);
        Vector2 right = new Vector2(-1, 0);
        Vector2 up = new Vector2(0, -1);
        Vector2 down = new Vector2(0, 1);
    }


    /**
     * Simulator step
     */
    private void step(float dt) {

        // wall bouncing ----------------------------------------

        Vector2 n;

        // bounced_vel=vel−2(vel⋅n)n, where n - normal vector, len=1
        if (obj.pos.x - obj.radius < 0) {

            n = normals.left;
            //obj.vel = obj.vel.cpy().sub(n.cpy().scl(obj.vel.cpy().dot(n)*2));
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);

            obj.pos.x = 2*obj.radius - obj.pos.x;
        }

        if (obj.pos.x + obj.radius > Gdx.graphics.getWidth()) {

            n = normals.right;
            //obj.vel = obj.vel.cpy().sub(n.cpy().scl(obj.vel.cpy().dot(n)*2));
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.pos.x = 2 * Gdx.graphics.getWidth() - 2*obj.radius - obj.pos.x;
        }

        if (obj.pos.y - obj.radius < 0) {

            n = normals.down;
            //obj.vel = obj.vel.cpy().sub(n.cpy().scl(obj.vel.cpy().dot(n)*2));
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.pos.y = 2*obj.radius - obj.pos.y;

        }

        if (obj.pos.y + obj.radius > Gdx.graphics.getHeight()) {

            n = normals.up;
            //obj.vel = obj.vel.cpy().sub(n.cpy().scl(obj.vel.cpy().dot(n)*2));
            obj.vel.x = obj.vel.x - 2 *n.x * obj.vel.dot(n);
            obj.vel.y = obj.vel.y - 2 *n.y * obj.vel.dot(n);
            obj.pos.y = 2 * Gdx.graphics.getHeight() - 2 *obj.radius - obj.pos.y;
        }

        // -----------------------------------------------------------------------------------

        // medium resistance
        obj.mediumRes.set(obj.vel);

        // medium resistance ~ vel
        //obj.mediumRes.scl(-0.001f * (obj.vel.len() + 1000));

        // medium resistance ~ const - moar bouncing
        obj.mediumRes.scl(-0.001f * ( /*obj.vel.len()*/ + 100));

        // Thruster force
        // Direct thruster to target
        if (target.len2() > 0.1) {

            vecToTarget.set(target).sub(obj.pos);
            obj.thruster.set(vecToTarget);
            obj.thruster.setLength(GameObject.MAX_THRUSTER);
        }

        // calc result force
        obj.force.setZero(); //zeroing
        obj.force.add(obj.mediumRes); // media resistance
        if (obj.fuel > 0) {           // thruster
            obj.force.add(obj.thruster);
        }

        obj.applyForce(); // calc result acceleration

        // calculate new velocity and position
        //float dt = Duration.between(tick, Instant.now()).toMillis() / 1000.0f;

        // allow to ignore fps ib simulator
        //tick = Instant.now();
        
        obj.step(dt);
    }







}
