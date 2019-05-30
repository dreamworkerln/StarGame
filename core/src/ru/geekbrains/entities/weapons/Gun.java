package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.Shell;
import ru.geekbrains.entities.particles.ParticleObject;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.storage.Game;

public class Gun extends ParticleObject {

    public float fireRate = 0.2f;
    public int gunHeat = 0;

    protected long lastFired;
    //public long lastFiredBurst;

    protected float blastRadius;
    protected float maxBlastRadius;


    protected Vector2 nozzlePos;

    protected GameObject owner;


    protected boolean firing = false;
    protected boolean overHeated = false;


    public Gun(GameObject owner) {
        super(owner.getRadius() * 0.3f);

        this.owner = owner;
        this.dir.set(owner.dir);

        lastFired = -1000;
        //lastFiredBurst = -1000;
        maxBlastRadius = this.radius;
        nozzlePos = new Vector2();
    }

    public void startFire() {

        firing = true;
    }

    public void stopFire() {

        firing = false;
    }

    @Override
    public void update(float dt) {

        if (gunHeat > 0) {
            gunHeat -= 2;
        }

        long tick = GameScreen.INSTANCE.getTick();

        // Nozzle-mounted gun
        pos = owner.pos;
        dir.set(owner.dir);
        nozzlePos.set(dir).setLength(owner.getRadius() + 15).add(pos);


        if (firing && !overHeated && lastFired < tick - 1/fireRate) {

            lastFired = GameScreen.INSTANCE.getTick();
            fire();
        }

        // trigger for gun overheating
        if (gunHeat > 170) {
            overHeated = true;
        }
        if (overHeated && gunHeat < 50) {
            overHeated = false;
        }



        // animation
        long frame = GameScreen.INSTANCE.getTick() - lastFired;

        if (frame >= 0 && frame < 2) {
            blastRadius = maxBlastRadius * 0.1f;
        } else if (frame >= 2 && frame < 5) {
            blastRadius = maxBlastRadius * 0.5f;
        } else if (frame >= 5 && frame < 7) {
            blastRadius = maxBlastRadius * 1f;
        } else if (frame >= 7 && frame < 10) {
            blastRadius = maxBlastRadius - maxBlastRadius * ((frame - 10) / 10f);
        } else {
            blastRadius = 0;
        }

    }



    protected void fire() {

        gunHeat+=60;

        Shell shell = new Shell(3);

        shell.pos.set(nozzlePos);
        shell.vel.set(owner.vel.cpy());
        tmp0.set(dir).setLength(300); // shell speed

        shell.applyForce(tmp0);         // force applied to shell
        owner.applyForce(tmp0.scl(-1)); // recoil applied to ship

        //shell.vel.set(owner.vel).add(tmp0);

        GameScreen.addObject(shell);

     }




    @Override
    public void draw(Renderer renderer) {

        ShapeRenderer shape = renderer.shape;

        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin();
        shape.set(ShapeRenderer.ShapeType.Filled);

        shape.setColor(1f, 1f, 0.2f, 1);
        shape.circle(nozzlePos.x, nozzlePos.y, blastRadius);

        Gdx.gl.glLineWidth(1);
        shape.end();
    }

}
