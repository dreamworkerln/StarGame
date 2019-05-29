package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import ru.geekbrains.screen.KeyDown;
import ru.geekbrains.screen.Renderer;

public class PlayerShip extends Ship {

    public PlayerShip(TextureRegion textureRegion, float height) {

        super(textureRegion, height);

    }


    @Override
    protected void guide() {

        if (KeyDown.A) {
            dir.rotateRad(maxRotationSpeed);
        }
        if (KeyDown.D) {
            dir.rotateRad(-maxRotationSpeed);
        }

        if (KeyDown.W) {
            throttle += maxThrottle * 0.05f;
            if (throttle >= maxThrottle) {
                throttle = maxThrottle;
            }
        }

        if (KeyDown.S) {
            throttle -= maxThrottle * 0.05f;
            if (throttle < 0) {
                throttle = 0;
            }
        }

        // 50% throttle ------------------------
        if (KeyDown.SPACE) {
            throttle = maxThrottle * 1.f;
            KeyDown.SPACE_TRIGGER_ON = true;
        }

        if (!KeyDown.SPACE && KeyDown.SPACE_TRIGGER_ON) {
            throttle = 0;
            KeyDown.SPACE_TRIGGER_ON = false;
        }

        // gun fire------------------------------
        if (KeyDown.MOUSE0) {
            gun.startFire();
        }
        else {
            gun.stopFire();
        }

    }

    @Override
    public void draw(Renderer renderer) {


        ShapeRenderer shape =renderer.shape;


        shape.begin();
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.set(ShapeRenderer.ShapeType.Line);
        shape.setColor(0f,0.76f,0.9f,0.5f);

        tmp0.set(dir).setLength(500).add(pos);

        //shape.circle(tmp0.x, tmp0.y, 10);
        renderer.shape.line(pos,tmp0);

        Gdx.gl.glLineWidth(1);
        shape.end();



        super.draw(renderer);
    }
}
