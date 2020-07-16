package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.auxiliary.TrajectorySimulator;
import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.equipment.ForceShield;
import ru.geekbrains.entities.equipment.interfaces.WeaponSystem;
import ru.geekbrains.entities.objects.enemies.BattleEnemyShip;
import ru.geekbrains.entities.particles.Message;
import ru.geekbrains.entities.projectile.missile.PlasmaFragMissile;
import ru.geekbrains.entities.projectile.shell.FlakShell;
import ru.geekbrains.entities.projectile.shell.Shell;
import ru.geekbrains.entities.weapons.launchers.AntiMissileLauncher;
import ru.geekbrains.entities.weapons.FlakCannon;
import ru.geekbrains.entities.weapons.Minigun;
import ru.geekbrains.entities.weapons.launchers.MissileLauncher;
import ru.geekbrains.entities.weapons.launchers.PlayerMissileLauncher;
import ru.geekbrains.entities.weapons.gun.CourseGun;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.KeyDown;
import ru.geekbrains.screen.KeyToggle;
import ru.geekbrains.utils.PlayList;
import ru.geekbrains.utils.SoundPlay;

import static ru.geekbrains.screen.GameScreen.addObject;


public class PlayerShip extends Ship {

    //public float maxAimRange = 1000;
    protected float corpseHealth;                       // здоровье корпуса после смерти

    private NavigableMap<Float, BPU.GuideResult> impactTimes = new TreeMap<>();
    private List<GameObject> targetList = new ArrayList<>();



    //boolean isPlayListPlaying = false;




    static SoundPlay hic;
    static SoundPlay hib25, hib50;
    static SoundPlay decin321;


    PlayList playList = new PlayList();

    boolean shouldBlowup = false;
    long blowupTick = -1;

    boolean kermanSaved = false;




    public PlayerShip(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.PLAYER_SHIP);
        side = ObjectSide.ALLIES;

        damage = getMaxHealth();


        TrajectorySimulator trajectorySim;
        TrajectorySimulator gunSim;

        Minigun minigun;
        ForceShield shield;
        MissileLauncher launcher;
        AntiMissileLauncher antiLauncher;
        FlakCannon flakCannon;

        // tuning gun
        CourseGun gun = (CourseGun)componentList.get(CompNames.COURSE_GUN);
        gun.maxGunHeat = 300;
        gun.drift = 0.03f;
        gun.burst= 6;


        trajectorySim = new TrajectorySimulator(this, this);
        gunSim = new TrajectorySimulator(this, new Shell(gun.getCalibre(), owner));
        shield = new ForceShield(this, new Color(0.1f , 0.5f, 1f, 1f));
        minigun = new Minigun(4, this);
        launcher = new PlayerMissileLauncher(10, this);
        launcher.addAmmoType(() -> new PlasmaFragMissile(new TextureRegion(MissileLauncher.MISSILE_TEXTURE), 2.5f, owner));
        antiLauncher = new AntiMissileLauncher(10, this);
        flakCannon = new FlakCannon(10, this);

        flakCannon.ammoTypeList.remove(FlakShell.class);
        flakCannon.addAmmoType(() -> {
            FlakShell shell = new FlakShell(flakCannon.getCalibre(), 1, Color.RED, owner);
            shell.isReadyElements = true;
            return shell;
        });


        //flakCannon = new FlakCannon(10, this);

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

        corpseHealth = maxHealth * 6f;


        // sounds
        hic = new SoundPlay(Gdx.audio.newSound(Gdx.files.internal("hull_bridge_integrity_compromised_2.mp3")), 2664, SoundPlay.SoundType.HEALTH_HALF);
        hib50 = new SoundPlay(Gdx.audio.newSound(Gdx.files.internal("hull_bridge_integrity_below_50_2.mp3")), 3265, SoundPlay.SoundType.HEALTH_HALF);
        hib25 = new SoundPlay(Gdx.audio.newSound(Gdx.files.internal("hull_bridge_integrity_below_25_2.mp3")), 3541, SoundPlay.SoundType.HEALTH_LOW);
        decin321 = new SoundPlay(Gdx.audio.newSound(Gdx.files.internal("decompression_is_imminent_in_321_2.mp3")), 4748, SoundPlay.SoundType.HEALTH_DEAD);

    }


    public ForceShield getShield() {
        return (ForceShield)componentList.get(CompNames.FORCESHIELD);
    }


    @Override
    protected void guide(float dt) {

        WeaponSystem gun = weaponList.get(CompNames.COURSE_GUN);
        FlakCannon flakCannon = (FlakCannon)componentList.get(CompNames.FLACK_CANNON);
        MissileLauncher launcher = (MissileLauncher)componentList.get(CompNames.LAUNCHER);


        float rot = maxRotationSpeed;
        float currentThrottle = maxThrottle;


        if (KeyDown.SHIFT) {
            rot = maxRotationSpeed/2;
            currentThrottle = maxThrottle/2;
        }

        if (KeyDown.A) {
            guideVector.rotateRad(rot);

        }
        if (KeyDown.D) {
            guideVector.rotateRad(-rot);
        }

        if (KeyDown.W) {
            requiredThrottle = throttle + currentThrottle * 0.05f;
        }

        if (KeyDown.S) {
            requiredThrottle = throttle - currentThrottle * 0.05f;
        }

        // full throttle ------------------------
        if (KeyDown.SPACE) {
            requiredThrottle = currentThrottle;
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

        if(KeyDown.P) {
            if(!shouldBlowup) {
                evacuateShip();
            }
        }
    }

    @Override
    public void update(float dt) {

        long tick = GameScreen.INSTANCE.getTick();
        if(shouldBlowup && tick > blowupTick) {
            readyToDispose = true;
            kermanSaved = true;
        }

        super.update(dt);

        playList.update(dt);

        if (GameScreen.INSTANCE.bossShip != null && !GameScreen.INSTANCE.bossShip.readyToDispose) {

            ((TrajectorySimulator)componentList.get(CompNames.SIM_GUN)).baseIterationCount = 3000;
        }

    }

    private void spawnKerman() {

        float fi = (float) ThreadLocalRandom.current().nextDouble(0, 2*Math.PI);
        float x = (float) (10 * Math.cos(fi));
        float y = (float) (10 * Math.sin(fi));

        JebediahKerman kerman = new JebediahKerman(new TextureRegion(new Texture("jebediah2.png")), 15, null);

        GameScreen.INSTANCE.kerman = kerman;

        kerman.pos.set(pos).add(x,y);
        kerman.vel.set(vel);
        kerman.dir.set(dir);

        addObject(kerman);
    }

    @Override
    public void doDamage(float amount) {

        float newHealth = health - amount;

        float healthDamage = Math.min(amount, health - 0.01f);
        float corpseDamage = newHealth >=0 ? 0 : amount - health;




        if (!shouldBlowup) {

            SoundPlay soundPlay;
            if (newHealth < maxHealth * 0.5 && newHealth >= maxHealth * 0.25) {

                if(ThreadLocalRandom.current().nextBoolean()) {
                    soundPlay = hic;
                }
                else {
                    soundPlay = hib50;
                }
                playList.add(soundPlay);
            }


            soundPlay = hib25;
            if (newHealth < maxHealth * 0.25) {
                playList.add(soundPlay);
            }

            if (newHealth <= 0) {
                evacuateShip();
            }
        }

          super.doDamage(healthDamage);
        corpseHealth -= corpseDamage;


        if (corpseHealth <= 0) {
            readyToDispose = true;
        }
    }



    private void evacuateShip() {

        SoundPlay soundPlay = decin321;
        playList.clear();
        playList.add(soundPlay);

        shouldBlowup = true;
        blowupTick = GameScreen.INSTANCE.getTick() + soundPlay.durationTick;

        maxRotationSpeed = 0;
        healthRegenerationCoefficient = 0;
        engineOnline = false;

        for (ShipComponent component : componentList.values()) {
            component.enable(false);
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

        if(kermanSaved) {
            Message msg = new Message("Safely land Kerman to planet to construct new ship", 0);
            GameScreen.INSTANCE.particleObjects.add(msg);
            spawnKerman();
        }


        super.dispose();
    }
}
