package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ru.geekbrains.entities.auxiliary.TrajectorySimulator;
import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.equipment.ForceShield;
import ru.geekbrains.entities.equipment.interfaces.WeaponSystem;
import ru.geekbrains.entities.projectile.shell.Shell;
import ru.geekbrains.entities.weapons.AntiMissileLauncher;
import ru.geekbrains.entities.weapons.FlakCannon;
import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.entities.weapons.Minigun;
import ru.geekbrains.entities.weapons.MissileLauncher;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.KeyDown;
import ru.geekbrains.screen.KeyToggle;
import ru.geekbrains.utils.PlayList;
import ru.geekbrains.utils.SoundPlay;

public class PlayerShip extends Ship {

    public float maxAimRange = 1000;
    private NavigableMap<Float, BPU.GuideResult> impactTimes = new TreeMap<>();
    private List<GameObject> targetList = new ArrayList<>();



    //boolean isPlayListPlaying = false;




    static SoundPlay hic;
    static SoundPlay hib25, hib50;
    static SoundPlay decin321;


    PlayList playList = new PlayList();

    boolean shouldBlowup = false;
    long blowupTick = -1;




    public PlayerShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.PLAYER_SHIP);


        TrajectorySimulator trajectorySim;
        TrajectorySimulator gunSim;

        Minigun minigun;
        ForceShield shield;
        MissileLauncher launcher;
        AntiMissileLauncher antiLauncher;
        FlakCannon flakCannon;

        // tuning gun
        Gun gun = (Gun)componentList.get(CompNames.GUN);
        gun.maxGunHeat = 300;
        gun.drift = 0.03f;
        gun.burst= 6;


        trajectorySim = new TrajectorySimulator(this, this);
        gunSim = new TrajectorySimulator(this, new Shell(gun.getCalibre(), owner));
        shield = new ForceShield(this, new Color(0.1f , 0.5f, 1f, 1f));
        minigun = new Minigun(4, this);
        launcher = new MissileLauncher(10, this);
        antiLauncher = new AntiMissileLauncher(10, this);
        flakCannon = new FlakCannon(10, this);

        addComponent(CompNames.SIM_TRAJECTORY, trajectorySim);
        addComponent(CompNames.SIM_GUN, gunSim);
        addComponent(CompNames.FORCESHIELD,shield);
        addComponent(CompNames.MINIGUN,minigun);
        addComponent(CompNames.LAUNCHER,launcher);
        addComponent(CompNames.ANTI_LAUNCHER,antiLauncher);
        addComponent(CompNames.FLACK_CANNON,flakCannon);

        // -----------------------------------------------------------------------------------------

        maxThrottle = 80f;

        guideVector.set(dir);

        //setMaxHealth(1000);


        // sounds
        hic = new SoundPlay(Gdx.audio.newSound(Gdx.files.internal("hull_bridge_integrity_compromised_2.mp3")), 2664);
        hib25 = new SoundPlay(Gdx.audio.newSound(Gdx.files.internal("hull_bridge_integrity_below_25_2.mp3")), 3541);
        hib50 = new SoundPlay(Gdx.audio.newSound(Gdx.files.internal("hull_bridge_integrity_below_50_2.mp3")), 3265);
        decin321 = new SoundPlay(Gdx.audio.newSound(Gdx.files.internal("decompression_is_imminent_in_321_2.mp3")), 4748);

    }


    public ForceShield getShield() {
        return (ForceShield)componentList.get(CompNames.FORCESHIELD);
    }


    @Override
    protected void guide(float dt) {

        WeaponSystem gun = weaponList.get(CompNames.GUN);
        FlakCannon flakCannon = (FlakCannon)componentList.get(CompNames.FLACK_CANNON);
        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);


        float rot = maxRotationSpeed;

        if (KeyDown.SHIFT) {
            rot = maxRotationSpeed/2;

        }

        if (KeyDown.A) {
            guideVector.rotateRad(rot);

        }
        if (KeyDown.D) {
            guideVector.rotateRad(-rot);
        }

        if (KeyDown.W) {
            requiredThrottle = throttle + maxThrottle * 0.05f;
        }

        if (KeyDown.S) {
            requiredThrottle = throttle - maxThrottle * 0.05f;
        }

        // full throttle ------------------------
        if (KeyDown.SPACE) {
            requiredThrottle = maxThrottle;
            KeyDown.SPACE_TRIGGER_ON = true;
        }

        if (!KeyDown.SPACE && KeyDown.SPACE_TRIGGER_ON) {
            requiredThrottle = 0;
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

            guideVector.rotateRad(rot);
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

        long tick = GameScreen.INSTANCE.getTick();
        if(shouldBlowup && tick > blowupTick) {
            readyToDispose = true;
        }

        super.update(dt);

        playList.update(dt);

    }

    @Override
    public void doDamage(float amount) {

        float newHealth = health - amount;

        
        if (!shouldBlowup) {

            SoundPlay soundPlay = hic;
            if (newHealth < maxHealth * 0.5 && newHealth >= maxHealth * 0.25) {
                playList.add(soundPlay);
            }


            soundPlay = hib25;
            if (newHealth < maxHealth * 0.25) {
                playList.add(soundPlay);
            }

            soundPlay = decin321;
            if (newHealth < 0) {

                playList.clear();
                playList.add(soundPlay);

                shouldBlowup = true;
                blowupTick = GameScreen.INSTANCE.getTick() + soundPlay.durationTick;


                health = 0.01f;
                maxRotationSpeed = 0;
                healthRegenerationCoefficient = 0;
                engineOnline = false;

                for (ShipComponent component : componentList.values()) {
                    component.enable(false);
                }


                //hib25.sound.stop();
                //hic.sound.stop();
            }

        }

        if (!shouldBlowup || newHealth < -maxHealth*1.5) {
            super.doDamage(amount);
        }

    }

    //    @Override
//    public void update(float dt) {
//        super.update(dt);
//        aimHelp(dt);
//    }


//    public void aimHelp(float dt) {

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



//    }


//    @Override
//    public void draw(Renderer renderer) {
//
//        super.draw(renderer);
//
//        // trajectory sim
//        trajectorySim.draw(renderer);
//
//        gunSim.draw(renderer);
//
//        shield.draw(renderer);
//
//        gun.draw(renderer);
//
//        minigun.draw(renderer);
//
//        launcher.draw(renderer);
//
//        antiLauncher.draw(renderer);
//
//        flakCannon.draw(renderer);


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



//        super.draw(renderer);
//    }


    @Override
    public void dispose() {

        playList.clear();

        super.dispose();
    }
}
