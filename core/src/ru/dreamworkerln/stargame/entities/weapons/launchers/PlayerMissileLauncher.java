package ru.dreamworkerln.stargame.entities.weapons.launchers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import ru.dreamworkerln.stargame.entities.equipment.BPU;
import ru.dreamworkerln.stargame.entities.equipment.interfaces.GunSystem;
import ru.dreamworkerln.stargame.entities.objects.DummyObject;
import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.objects.PlayerShip;
import ru.dreamworkerln.stargame.entities.projectile.Ammo;
import ru.dreamworkerln.stargame.entities.projectile.missile.EmpMissile;
import ru.dreamworkerln.stargame.entities.projectile.missile.FastMissile;
import ru.dreamworkerln.stargame.entities.projectile.missile.Missile;
import ru.dreamworkerln.stargame.entities.projectile.missile.NewtonMissile;
import ru.dreamworkerln.stargame.screen.GameScreen;
import ru.dreamworkerln.stargame.screen.Renderer;
import ru.dreamworkerln.stargame.screen.RendererType;

public class PlayerMissileLauncher extends MissileLauncher {

    private DummyObject dummy;

    private final NavigableMap<Float, BPU.GuideResult> reticleMap = new TreeMap<>();

    public PlayerMissileLauncher(float height, GameObject owner) {
        super(height, owner);
        dummy = new DummyObject(owner);
    }


    private List<GameObject> getTarget() {

        List<GameObject> result = new ArrayList<>();

        List<GameObject> targets;
        dummy.pos.set(GameScreen.INSTANCE.target);


        Predicate<GameObject> filter = t-> (t.type.contains(ObjectType.SHIP) || t.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) &&
            t != this.owner && t.owner != this.owner && !t.readyToDispose && t.side != owner.side;

        targets = GameScreen.getCloseObjects(dummy, 4000, filter);


//        targets.removeIf(t -> (!t.type.contains(ObjectType.SHIP) && !t.type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)));
//        targets.removeIf(t -> t == this.owner);
//        targets.removeIf(t -> t.owner == this.owner);
//        targets.removeIf(t -> t.readyToDispose);
//        targets.removeIf(t -> t.side == owner.side);

        if (targets.size() == 1) {
            result.add(targets.get(0));
        }
        else {

            for (GameObject o : targets) {

                result.add(o);
                if (result.size() >= 2) {
                    break;
                }
            }
        }

        return result;
    }



    // Select target to fire and load appropriate ammo
    @Override
    protected void selectTarget() {


        visualTargets.clear();
        launchDelay = LAUNCH_DELAY_INITIAL;
        burstMax = pylonCount;

        targetList = getTarget();


        //default ammo 
        currentAmmoType = Missile.class;


//        if(launchPylonNo == 0) {
//
//            burstMax = 1;
//
//            launchDelay = LAUNCH_DELAY_INITIAL;
//            currentAmmoType = Missile.class;
//            targetList = getTarget();
//
//            visualTargets.clear();
//        }
//
//        if (launchPylonNo == 1) {
//            targetList.removeIf(o-> o.readyToDispose);
//        }
//
//
//        if (targetList.size() == 0) {
//            launchPylonNo = 0;
//            return false;
//        }

        if (targetList.size() == 0) {
            return;
        }


        if (targetList.get(0).type.contains(ObjectType.MISSILE_SHIP)) {
            launchDelay = 10;
            burstMax = 4;
            currentAmmoType = FastMissile.class;
        }

        // if first target is NewtonTorpedo/MissileShip - remove other targets
        if (targetList.get(0).type.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) {
            burstMax = 4;
            launchDelay = 15;
            currentAmmoType = Missile.class;
            if(burstNo == 0 ) {
                currentAmmoType = EmpMissile.class;
            }

            GameObject tmp = targetList.get(0);
            targetList.clear();
            targetList.add(tmp);
        }

        if (targetList.get(0).type.contains(ObjectType.BATTLE_SHIP)) {

            burstMax = 1;
            currentAmmoType = NewtonMissile.class;

            GameObject tmp = targetList.get(0);
            targetList.clear();
            targetList.add(tmp);
        }


        visualTargets.addAll(targetList);


//        launchPylonNo++;
//        if (launchPylonNo >= pylonCount) {
//            launchPylonNo = 0;
//        }
//
//        if (launchPylonNo == 1) {
//            visualTargets.addAll(targetList);
//        }


    }

    
    // делаем подсветку для курсовой пушки
    @Override
    public void update(float dt) {

        super.update(dt);

        reticleMap.clear();

        visualTargets.removeIf(o -> o == null || o.readyToDispose);

        if (visualTargets.size() != 1) {
            return;
        }

        // getting target
        if (owner != null && !owner.readyToDispose) {

            GunSystem gun = ((PlayerShip)owner).getCourseGun();
            Ammo ammo = gun.getFiringAmmo();



            for (GameObject o : visualTargets) {

                if(o == null || o.readyToDispose) {
                    continue;
                }

                float maxPrjVel = ammo.getFirePower() / ammo.getMass() * dt;  // Задаем начальную скорость пули
                BPU.GuideResult gr = pbu.guideGun(owner, o, maxPrjVel, dt);

                // get results

                float impactTime = (float) gr.impactTime;

                if (!Float.isNaN(impactTime) && impactTime >= 0 && impactTime <= 5) {
                    reticleMap.put(impactTime, gr);
                }
            }
        }
    }




    @Override
    public void draw(Renderer renderer) {

        super.draw(renderer);

        if (renderer.rendererType!= RendererType.SHAPE) {
            return;
        }

        for (GameObject o : visualTargets) {

            // Рисуем перекрестье на цели
            if (o != null && !o.readyToDispose) {


                // Рисуем перекрестье на цели
                ShapeRenderer shape = renderer.shape;

                Gdx.gl.glLineWidth(1);
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                //shape.begin();
                shape.set(ShapeRenderer.ShapeType.Line);

                shape.setColor(0.5f, 0.9f, 0.9f, 0.7f);
                shape.circle(o.pos.x, o.pos.y, o.getRadius() * 2);

                tmp0.set(o.pos).sub(o.getRadius() * 2, 0);
                tmp1.set(tmp0).set(o.pos).add(o.getRadius() * 2, 0);
                shape.line(tmp0, tmp1);

                tmp0.set(o.pos).sub(0, o.getRadius() * 2);
                tmp1.set(tmp0).set(o.pos).add(0, o.getRadius() * 2);
                shape.line(tmp0, tmp1);

                //shape.end();




                // Course gun reticles
                reticleMap.forEach( (aFloat, gr) -> {

                    //BPU.GuideResult gr = reticleMap.firstEntry().getValue();
                    shape.setColor(0.9f, 0.9f, 0.9f, 0.5f);

                    tmp0.set(owner.pos).add(gr.impactVector);
                    shape.line(owner.pos, tmp0);

                    shape.getColor().a = 1;
                    shape.circle(tmp0.x, tmp0.y, 10);
                });


                Gdx.gl.glLineWidth(1);
                shape.flush();
            }

        }

    }


}
