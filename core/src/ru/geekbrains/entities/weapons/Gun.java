package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.entities.objects.Bullet;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Projectile;
import ru.geekbrains.entities.objects.Shell;
import ru.geekbrains.entities.particles.ParticleObject;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public class Gun extends ParticleObject {

    public float calibre = 6;
    public float power = 230;     // force length, applied to shell
    public float projectileMass = -1;

    public float fireRate = 0.2f;
    public float gunHeat = 0;
    public float gunHeatingDelta = 60;
    public float coolingGunDelta = 2;
    public int maxGunHeat = 200;

    protected long lastFired;
    //public long lastFiredBurst;

    protected float blastRadius;
    protected float maxBlastRadius;


    protected Vector2 nozzlePos;

    protected boolean firing = false;
    protected boolean overHeated = false;




    public Gun(float height, GameObject owner) {
        super(height, owner);

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


    protected void rotateGun() {

        // Nozzle-mounted gun
        dir.set(owner.dir);

    }

    @Override
    public void update(float dt) {

        long tick = GameScreen.INSTANCE.getTick();

        rotateGun();

        pos = owner.pos;
        nozzlePos.set(dir).setLength(owner.getRadius() + 15).add(pos);


        if (firing && !overHeated && lastFired < tick - 1/fireRate) {

            lastFired = GameScreen.INSTANCE.getTick();
            fire();
        }

        // gun heating

        if (gunHeat > 0) {
            gunHeat -= coolingGunDelta;
        }


        // trigger for gun overheating
        if (gunHeat > maxGunHeat) {
            overHeated = true;
        }
        if (overHeated && gunHeat < maxGunHeat * 0.3) {
            overHeated = false;
        }



        // animation
        long frame = GameScreen.INSTANCE.getTick() - lastFired;

        blastRadius = maxBlastRadius - maxBlastRadius * ((frame - 5) / 5f);



//        if (frame >= 0 && frame < 2) {
//            blastRadius = maxBlastRadius * 0.1f;
//        } else if (frame >= 2 && frame < 5) {
//            blastRadius = maxBlastRadius * 0.5f;
//        } else if (frame >= 5 && frame < 7) {
//            blastRadius = maxBlastRadius * 1f;
//        } else if (frame >= 7 && frame < 10) {
//            blastRadius = maxBlastRadius - maxBlastRadius * ((frame - 10) / 10f);
//        } else {
//            blastRadius = 0;
//        }

    }



    protected Projectile createProjectile() {
        return new Shell(calibre, owner);
    }




    protected void fire() {

        gunHeat+= gunHeatingDelta;


        Projectile proj = createProjectile();

        if (projectileMass > 0) {
            proj.setMass(projectileMass);
        }



        proj.pos.set(nozzlePos);
        proj.vel.set(owner.vel.cpy());
        proj.dir.set(dir);
        tmp0.set(dir).setLength(power); // force to bullet

        proj.applyForce(tmp0);         // force applied to bullet
        owner.applyForce(tmp0.scl(-1)); // recoil applied to ship

        //bullet.vel.set(owner.vel).add(tmp0);

        GameScreen.addObject(proj);

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
