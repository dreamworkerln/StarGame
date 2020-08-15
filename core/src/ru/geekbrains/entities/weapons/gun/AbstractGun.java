package ru.geekbrains.entities.weapons.gun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;


import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.equipment.interfaces.GunSystem;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.ShipComponent;
import ru.geekbrains.entities.projectile.Ammo;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public abstract class AbstractGun extends ShipComponent implements GunSystem {


    // производитель боеприпасов через Supplier
    // типы поддерживаемых боеприпасов настраиваются до использования оружия
    // в конце инициализации нужно вызвать weapon.init();  (нет DI контейнера)
    public LinkedHashMap<Class<? extends Ammo>, Supplier<Ammo>> ammoProducer = new LinkedHashMap<>();


    // хранит поддерживаемые типы боеприпасов оружия, с их индивидуальными настройками
    // кеш доступных боеприпасов, их параметры используются BPU для баллистических расчетов выстрела
    protected HashMap<Class<? extends Ammo>, Ammo> ammoTemplateList = new HashMap<>();

    // тип боеприпаса, которым стреляем в данный момент
    protected Class<? extends Ammo> currentAmmoType;



    protected Sound gunFire;

    protected float calibre = 6;
    //public float power = 230;     // force length, applied to shell
    //public float projectileMass = -1;

    public float fireRate = 0.2f;
    public float gunHeat = 0;
    public float gunHeatingDelta = 60;
    public float coolingGunDelta = 2;
    public int   maxGunHeat = 200;

    // multibarrel / multipylon system, время перезарядки делится на число стволов
    // соотв если два ствола, то перезарядка начнется когда выстрелят из обоих
    // перегрев будет считаться с каждого выстрела (если нужно, задай maxGunHeat*=barrelCount)
    //public int barrelUsed = 0;
    //public int barrelCount = 1;


    //protected long lastFired;
    //public long lastFiredBurst;

    protected float blastRadius;
    public float maxBlastRadius;

    protected long gunLastFired = -1;


    public Vector2 nozzlePos;

    protected boolean firing = false;
    protected boolean overHeated = false;


    public GameObject target;
    //protected GameObject firingAmmoType;

    protected boolean displayTargetingVector = false;

    protected BPU pbu = new BPU();

    public float drift = 0;
    public int burst = 1;


    //ToDo: make abstract gun than fire abstract Projectile
    // then inherit Gun and minigun from it

    public AbstractGun(float height, GameObject owner) {

        super(height, owner);
        isModule = true;

        this.dir.set(owner.dir);
        nozzlePos = new Vector2();
        setCalibre(this.calibre);
    }

    public void startFire() {
        firing = true;
    }

    public void stopFire() {
        firing = false;
    }

    /*
    @Override
    public float getPower() {
        return power;
    }
    */

    @Override
    public Ammo getFiringAmmo() {
        return ammoTemplateList.get(currentAmmoType);
    }

    @Override
    public Class<? extends Ammo> getCurrentAmmoType() {
        return currentAmmoType;
    }

    @Override
    public void setCurrentAmmoType(Class<? extends Ammo> ammo) {
        currentAmmoType = ammo;
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
    public void update(float dt) {

        super.update(dt);

        if (owner == null || owner.readyToDispose || !enabled) {
            return;
        }

        nozzlePos.set(dir).setLength(owner.getRadius() + 10).add(pos);

        if (firing) {
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
        long frame = GameScreen.INSTANCE.getTick() - gunLastFired;
        frame = frame > 5 ? frame : 0;

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


    /**
     * Will produce new ammo of currentAmmoType type
     */
    private Ammo produceAmmo() {
        return ammoProducer.get(currentAmmoType).get();
    }


    /**
     * Call this to load gun with ammo
     * @return Ammo
     */
    protected Ammo createAmmo() {

        Ammo result;
        Ammo template = ammoTemplateList.get(currentAmmoType);

        if(template == null) {
            throw new RuntimeException("Ammo " + currentAmmoType.getSimpleName() + " not equipped in " + this.getClass().getSimpleName());
        }

        //System.out.println(currentAmmoType);
        result = produceAmmo();
        template.copyTo(result);
        result.setOwner(owner);
        result.setSide(owner.side);
        return result;
    }


    protected void fire(float dt) {

        if(!enabled) {
            return;
        }

        long tick = GameScreen.INSTANCE.getTick();

        Ammo currentAmmo = ammoTemplateList.get(currentAmmoType);

        nozzlePos.set(dir).setLength(owner.getRadius() + currentAmmo.getRadius() + 10).add(pos);


        if (overHeated || currentAmmo.getLastFired() + currentAmmo.getReloadTime() > (long)(tick - 1/fireRate)) {
            return;
        }

        gunLastFired = tick;
        currentAmmo.setLastFired(tick);


        if (burst == 1) {
            playFireSound(0.4f);
        }
        else {
            playFireSound(0.4f * burst/2f);
        }


        for (int i = 0; i < burst; i++) {

            Ammo ammo = createAmmo();

            ammo.getPos().set(nozzlePos);
            ammo.getVel().set(owner.vel);
            ammo.getDir().set(dir);
            tmp0.set(dir).setLength(ammo.getFirePower()); // force




            if (drift > 0) {
                double gs = ThreadLocalRandom.current().nextGaussian()*drift;
                tmp0.rotateRad((float) gs);
            }


            ammo.applyForce(tmp0);         // apply force applied to bullet

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


            GameScreen.addAmmo(ammo);


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

        if (renderer.rendererType!= RendererType.SHAPE) {
            return;
        }

        if(!enabled) {
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

            tmp1.set(guideVector).scl(300);
            tmp0.set(pos).add(tmp1);

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
    public float getCalibre() {
        return calibre;
    }

    public void setCalibre(float calibre) {
        this.calibre = calibre;
        maxBlastRadius = calibre/1.5f;
        //firingAmmoType = createProjectile();
    }

//    public void recalibrate() {
//        maxBlastRadius = calibre/1.5f;
//        firingAmmoType = createProjectile();
//    }

    /**
     * Добавить тип поддерживаемых боеприпасов в оружие
     * @param supplier
     */
    public void addAmmoType(Supplier<Ammo> supplier) {

        // йохо-хо ублюдки, чтобы узнать тип ракеты, мы ей стреляем в сборщик мусора
        Class<? extends Ammo> type = supplier.get().getClass();

        ammoProducer.put(type, supplier);
    }


    protected void playFireSound(float vol) {
        gunFire.play(vol);
    }


    // Call this после того как установил все типы поддерживаемых боеприпасов для оружия
    @Override
    public void init() {
        currentAmmoType = ammoProducer.entrySet().iterator().next().getKey();

        ammoTemplateList.clear();
        for (Map.Entry<Class<? extends Ammo>, Supplier<Ammo>> entry : ammoProducer.entrySet()) {
            ammoTemplateList.put(entry.getKey(), entry.getValue().get());
        }
    }

    @Override
    public void dispose() {

        stopFire();
        super.dispose();
    }



//    public class Set<Projectile> AmmoType {
//
//        SHELL(Shell.class);
//        Class type;
//        ShellType(Class type) {
//            this.type = type;
//        }
//    }

}
