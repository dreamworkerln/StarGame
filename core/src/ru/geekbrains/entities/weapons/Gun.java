package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.ShipComponent;
import ru.geekbrains.entities.objects.WeaponSystem;
import ru.geekbrains.entities.projectile.Projectile;
import ru.geekbrains.entities.projectile.shell.Shell;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public class Gun extends ShipComponent implements WeaponSystem {

    private static Sound cannonFire;

    protected float calibre = 6;
    public float power = 230;     // force length, applied to shell
    //public float projectileMass = -1;

    public float fireRate = 0.2f;
    public float gunHeat = 0;
    public float gunHeatingDelta = 60;
    public float coolingGunDelta = 2;
    public int maxGunHeat = 200;

    protected long lastFired;
    //public long lastFiredBurst;

    protected float blastRadius;
    public float maxBlastRadius;


    public Vector2 nozzlePos;

    protected boolean firing = false;
    protected boolean overHeated = false;


    public GameObject target;                      // цель
    protected Vector2 guideVector = new Vector2(); // вектор куда нужно целиться

    public float maxRotationSpeed = 0; // maximum rotation speed

    protected GameObject firingAmmoType;
    //protected Float maxProjectileVel = null;

    protected boolean displayTargetingVector = false;

    protected BPU pbu = new BPU();

    public float drift = 0;
    public int burst = 1;

    static {
        cannonFire = Gdx.audio.newSound(Gdx.files.internal("Light Naval Cannon Blast 15.mp3"));
    }


    //ToDo: make abstract gun than fire abstract Projectile
    // then inherit Gun and minigun from it

    public Gun(float height, GameObject owner) {

        super(height, owner);

        isModule = true;

        this.dir.set(owner.dir);

        lastFired = -1000;
        //lastFiredBurst = -1000;
        nozzlePos = new Vector2();

        setCalibre(this.calibre);

    }







    public void startFire() {

        firing = true;
    }

    public void stopFire() {

        firing = false;
    }

    @Override
    public float getPower() {
        return power;
    }

    @Override
    public GameObject getFiringAmmoType() {
        return firingAmmoType;
    }

    @Override
    public float getFireRate() {
        return fireRate;
    }

    @Override
    public void setFireRate(float rate) {
        fireRate = rate;
    }














    @Override
    protected void rotateObject() {

        // Nozzle-mounted gun
        if (owner!= null && !owner.readyToDispose) {
            dir.set(owner.dir);
        }

    }

    @Override
    public void update(float dt) {

        super.update(dt);

        if (owner == null || owner.readyToDispose) {
            return;
        }

        long tick = GameScreen.INSTANCE.getTick();

        nozzlePos.set(dir).setLength(owner.getRadius() + firingAmmoType.getRadius() + 5).add(pos);

        if (firing && !overHeated && lastFired <= (long)(tick - 1/fireRate)) {

            lastFired = GameScreen.INSTANCE.getTick();
            fire(dt);
        }

        // gun cooling

        if (gunHeat > 0) {
            gunHeat -= coolingGunDelta;
        }

        if (overHeated && gunHeat < maxGunHeat * 0.3) {
            overHeated = false;
        }



        // animation
        long frame = GameScreen.INSTANCE.getTick() - lastFired;

        blastRadius = maxBlastRadius - maxBlastRadius * ((frame - 5) / 5f);



//        if (age >= 0 && age < 2) {
//            blastRadius = maxBlastRadius * 0.1f;
//        } else if (age >= 2 && age < 5) {
//            blastRadius = maxBlastRadius * 0.5f;
//        } else if (age >= 5 && age < 7) {
//            blastRadius = maxBlastRadius * 1f;
//        } else if (age >= 7 && age < 10) {
//            blastRadius = maxBlastRadius - maxBlastRadius * ((age - 10) / 10f);
//        } else {
//            blastRadius = 0;
//        }

    }



    protected GameObject createProjectile() {
        return new Shell(calibre, calibre/8, owner);
    }




    protected void fire(float dt) {

        //System.out.println(owner.name);


        if (burst == 1) {
            playFireSound(0.4f);
        }
        else {
            playFireSound(0.4f * burst/2f);
        }


        for (int i = 0; i < burst; i++) {

            
            Projectile proj = (Projectile) createProjectile();






            proj.pos.set(nozzlePos);
            proj.vel.set(owner.vel);
            proj.dir.set(dir);
            tmp0.set(dir).setLength(power); // force




            if (drift > 0) {
                double gs = ThreadLocalRandom.current().nextGaussian()*drift;
                tmp0.rotateRad((float) gs);
            }


            proj.applyForce(tmp0);         // apply force applied to bullet

            //System.out.println("power: " + power);
            //System.out.println("mass: "    + proj.getMass());
            //System.out.println("dir: " + proj.dir);
            //System.out.println("pos: " + proj.pos);
            //System.out.println("vel: " + proj.vel);
            //System.out.println("force: " + tmp0);

            // DEBUG UNCOMMENT
            // recoil applied to ship
            owner.applyForce(tmp0.scl(-1));


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


            GameScreen.addObject(proj);

            gunHeat+= gunHeatingDelta;

            // trigger for gun overheating
            if (gunHeat > maxGunHeat) {
                overHeated = true;
                break;
            }
        }
     }


    @Override
    public void draw(Renderer renderer) {

        super.draw(renderer);

        if (renderer.rendererType!=RendererType.SHAPE) {
            return;
        }


        // Ракетные установки без анимации
        if(this.type.contains(ObjectType.MISSILE_LAUNCHER)) {
            return;
        }

        ShapeRenderer shape = renderer.shape;

        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        //shape.begin();
        shape.set(ShapeRenderer.ShapeType.Filled);

        shape.setColor(1f, 1f, 0.2f, 1);
        shape.circle(nozzlePos.x, nozzlePos.y, blastRadius);


//        shape.set(ShapeRenderer.ShapeType.Line);
//        shape.setColor(1f, 1f, 1f, 1);
//        shape.circle(nozzlePos.x, nozzlePos.y, 3);

        if (displayTargetingVector) {

            tmp0.set(pos).add(guideVector);

            //Gdx.gl.glLineWidth(1);

            shape.set(ShapeRenderer.ShapeType.Line);
            // reticle
//        shape.setColor(0f, 1f, 0f, 1);
//        shape.circle(tmp0.x, tmp0.y, 3);
            shape.setColor(1f, 0f, 0f, 0.5f);
            shape.line(pos, tmp0);
            Gdx.gl.glLineWidth(2);
        }
        //shape.end();
    }



    @Override
    public Vector2 getDir() {
        return dir;
    }

    @Override
    public float getCalibre() {
        return calibre;
    }

    public void setCalibre(float calibre) {
        this.calibre = calibre;
        maxBlastRadius = calibre/1.5f;
        firingAmmoType = createProjectile();
    }

//    public void recalibrate() {
//        maxBlastRadius = calibre/1.5f;
//        firingAmmoType = createProjectile();
//    }




    // костыли, нарушение подстановки Лискова, выделить базовый функционал в класс abstract BaseMissileLauncher
    // в абстрактный  метод playLaunchSound()


    protected void playFireSound(float vol) {
        cannonFire.play(vol);
    }


    @Override
    public void dispose() {

        stopFire();
        super.dispose();
    }

}
