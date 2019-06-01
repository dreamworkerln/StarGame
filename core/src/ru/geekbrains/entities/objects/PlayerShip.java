package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.auxiliary.TrajectorySimulator;
import ru.geekbrains.entities.equipment.ForceShield;
import ru.geekbrains.entities.weapons.Minigun;
import ru.geekbrains.screen.KeyDown;
import ru.geekbrains.screen.Renderer;

public class PlayerShip extends Ship {


    public TrajectorySimulator trajectorySim;
    public TrajectorySimulator gunSim;

    public Minigun minigun;

    public ForceShield shield;

    public PlayerShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.PLAYER_SHIP);

        trajectorySim = new TrajectorySimulator(this, this);
        gunSim = new TrajectorySimulator(this, new Shell(gun.calibre, owner));

        shield = new ForceShield(this, new Color(0.1f , 0.5f, 1f, 1f));

        minigun = new Minigun(4, this);
    }


    @Override
    protected void guide() {

        if (KeyDown.A) {
            dir.rotateRad(maxRotationSpeed);
            minigun.dir.rotateRad(maxRotationSpeed);

        }
        if (KeyDown.D) {
            dir.rotateRad(-maxRotationSpeed);
            minigun.dir.rotateRad(-maxRotationSpeed);
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
    public void update(float dt) {
        super.update(dt);

        // simulate playerShip trajectory for future steps
        trajectorySim.update(dt);

        gunSim.update(dt);

        shield.update(dt);

        minigun.update(dt);
    }

    @Override
    public void draw(Renderer renderer) {

        // trajectory sim
        trajectorySim.draw(renderer);

        gunSim.draw(renderer);

        shield.draw(renderer);

        minigun.draw(renderer);

//        // ship line of fire
//        ShapeRenderer shape = renderer.shape;
//        shape.begin();
//        Gdx.gl.glLineWidth(1);
//        Gdx.gl.glEnable(GL20.GL_BLEND);
//        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        shape.set(ShapeRenderer.ShapeType.Line);
//        shape.setColor(0f,0.76f,0.9f,0.5f);
//
//        tmp0.set(dir).setLength(500).add(pos);
//
//        //shape.circle(tmp0.x, tmp0.y, 10);
//        renderer.shape.line(pos,tmp0);
//
//        Gdx.gl.glLineWidth(1);
//        shape.end();



        super.draw(renderer);
    }


    @Override
    public void dispose() {
        super.dispose();
        trajectorySim.dispose();
        gunSim.dispose();

        shield.readyToDispose = true;
        shield.dispose();
    }
}
