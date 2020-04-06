package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import ru.geekbrains.entities.auxiliary.TrajectorySimulator;
import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.equipment.ForceShield;
import ru.geekbrains.entities.projectile.shell.Shell;
import ru.geekbrains.entities.weapons.AntiMissileLauncher;
import ru.geekbrains.entities.weapons.FlakCannon;
import ru.geekbrains.entities.weapons.Minigun;
import ru.geekbrains.entities.weapons.MissileLauncher;
import ru.geekbrains.screen.KeyDown;
import ru.geekbrains.screen.KeyToggle;
import ru.geekbrains.screen.Renderer;

public class PlayerShip extends Ship {




    public TrajectorySimulator trajectorySim;
    public TrajectorySimulator gunSim;

    public Minigun minigun;
    public ForceShield shield;
    public MissileLauncher launcher;
    public AntiMissileLauncher antiLauncher;

    public FlakCannon flakCannon;

    public BPU pbu = new BPU();
    public float maxAimRange = 1000;
    private NavigableMap<Float, BPU.GuideResult> impactTimes = new TreeMap<>();
    private List<GameObject> targetList = new ArrayList<>();



    public PlayerShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.PLAYER_SHIP);

        trajectorySim = new TrajectorySimulator(this, this);

        gunSim = new TrajectorySimulator(this, new Shell(gun.getCalibre(), owner));

        shield = new ForceShield(this, new Color(0.1f , 0.5f, 1f, 1f));

        minigun = new Minigun(4, this);

        launcher = new MissileLauncher(10, this);

        antiLauncher = new AntiMissileLauncher(10, this);

        flakCannon = new FlakCannon(10, this);

        maxThrottle = 80f;

        //launcher.fireRate = 0.02f;


        //setMaxHealth(100);

        gun.maxGunHeat = 300;
        //gun.fireRate = 1f;
        gun.drift = 0.03f;
        gun.burst= 6;
    }


    @Override
    protected void guide(float dt) {

        float rot = maxRotationSpeed;

        if (KeyDown.SHIFT) {
            rot = maxRotationSpeed/2;

        }

        if (KeyDown.A) {
            dir.rotateRad(rot);
            minigun.dir.rotateRad(rot);
            flakCannon.dir.rotateRad(rot);

        }
        if (KeyDown.D) {
            dir.rotateRad(-rot);
            minigun.dir.rotateRad(-rot);
            flakCannon.dir.rotateRad(-rot);
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

        // full throttle ------------------------
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


        // missile fire------------------------------
        if (KeyDown.MOUSE1 || KeyDown.CTRL) {

            if (KeyDown.SHIFT || KeyDown.CTRL) {
                launcher.reverse(true);
            }
            else {
                launcher.reverse(false);
            }

            launcher.startFire();
        }
        else {
            launcher.stopFire();
        }


        if (KeyDown.SCROLLED != 0) {

            rot = maxRotationSpeed/2 * KeyDown.SCROLLED;

            dir.rotateRad(rot);
            minigun.dir.rotateRad(rot);
            flakCannon.dir.rotateRad(rot);

            KeyDown.SCROLLED = 0;
        }

        if (KeyToggle.F) {
            flakCannon.setFiringMode(FlakCannon.FiringMode.FLAK_ONLY);
        }
        if (KeyToggle.G)  {
            flakCannon.setFiringMode(FlakCannon.FiringMode.AUTOMATIC);
        }
        if (KeyToggle.V)  {
            flakCannon.setFiringMode(FlakCannon.FiringMode.PLASMA_ONLY);
        }

        //System.out.println(flakCannon.getFiringMode());

    }


    @Override
    public void update(float dt) {
        super.update(dt);


        trajectorySim.update(dt);
        gunSim.update(dt);

        shield.update(dt);

        gun.update(dt);

        minigun.update(dt);

        launcher.update(dt);

        antiLauncher.update(dt);

        flakCannon.update(dt);

        aimHelp(dt);
    }


    public void aimHelp(float dt) {

//        impactTimes.clear();
//        targetList.clear();
//
//        // getting target
//        if (!this.readyToDispose) {
//            targetList = GameScreen.getCloseObjects(this, maxAimRange);
//        }
//
//
//
//        for (GameObject o : targetList) {
//
//            if (o == this || o.owner == this || o.readyToDispose) {
//                continue;
//            }
//
//            if (!o.type.contains(ObjectType.SHIP)) {
//                continue;
//            }
//
//
//            if (!this.readyToDispose) {
//                float maxPrjVel = gun.power / gun.firingAmmoType.getMass() * dt;  // Задаем начальную скорость пули
//                pbu.guideGun(this, o, maxPrjVel, dt);
//            }
//            // get results
//
//            Float impactTime = (float)pbu.guideResult.impactTime;
//
//            if (!impactTime.isNaN() && impactTime >= 0 && impactTime <= 3f) {
//                impactTimes.put(impactTime, pbu.guideResult.clone());
//            }
//        }



    }

    @Override
    public void draw(Renderer renderer) {

        super.draw(renderer);

        // trajectory sim
        trajectorySim.draw(renderer);

        gunSim.draw(renderer);

        shield.draw(renderer);

        gun.draw(renderer);

        minigun.draw(renderer);

        launcher.draw(renderer);

        antiLauncher.draw(renderer);

        flakCannon.draw(renderer);


//        if (renderer.rendererType!= RendererType.SHAPE) {
//            return;
//        }
//
//        ShapeRenderer shape = renderer.shape;
//
//        shape.set(ShapeRenderer.ShapeType.Line);
//        shape.setColor(0f,0.76f,0.9f,0.6f);
//
//        //Gdx.gl.glLineWidth(4);
//
//        for (BPU.GuideResult value : impactTimes.values()) {
//
//
//            tmp0.set(value.guideVector).add(this.pos);
//            shape.line(tmp0.x,tmp0.y, value.target.pos.x, value.target.pos.y);
//            Gdx.gl.glLineWidth(1);
//            shape.flush();
//            shape.circle(tmp0.x, tmp0.y, 10);
//            Gdx.gl.glLineWidth(2);
//            shape.flush();
//
//            break;
//        }




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

        trajectorySim.dispose();
        gunSim.dispose();

        launcher.dispose();
        antiLauncher.dispose();
        flakCannon.dispose();
        shield.dispose();

        minigun.dispose();
        gun.dispose();

        super.dispose();
    }
}
