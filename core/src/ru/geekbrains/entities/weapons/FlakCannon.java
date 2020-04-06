package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.Projectile;
import ru.geekbrains.entities.projectile.shell.FlakShell;
import ru.geekbrains.entities.projectile.shell.PlasmaFlakShell;
import ru.geekbrains.screen.GameScreen;

public class FlakCannon extends Gun {

    private static Sound cannonFire;

    float maxRange;
    float maxImpactTime;

    ShellType shellType;

    FiringMode firingMode;
    FiringMode firingModeOld;

    protected float oldFireRate;



    // Цели, отсортированные по времени попадания в корабль
    private NavigableMap<Float, BPU.GuideResult> impactTimes = new TreeMap<>();

    private List<GameObject> targetList = new ArrayList<>();

    private List<GameObject> missilesList = new ArrayList<>();

    private long currentFuse = 1;

    static {
        cannonFire = Gdx.audio.newSound(Gdx.files.internal("flak_fire.ogg"));
    }

    public FlakCannon(float height, GameObject owner) {
        super(height, owner);


        setCalibre(6);
        fireRate = 0.02f;
        gunHeatingDelta = 30;
        coolingGunDelta = 2;
        maxGunHeat = 100;
        power = 150;
        maxBlastRadius = 5;

        maxRange = 1500f;
        maxImpactTime = 5f;

        maxRotationSpeed = 0.1f;

        firingMode = FiringMode.AUTOMATIC;
        firingModeOld = FiringMode.AUTOMATIC;

        //maxRotationSpeed = 1f;

        oldFireRate = fireRate;

    }


    public void setFiringMode(FiringMode firingMode) {
        this.firingMode = firingMode;
        firingModeOld = firingMode;
    }

    public FiringMode getFiringMode() {
        return firingMode;
    }




    @Override
    public void update(float dt) {

        super.update(dt);

        nozzlePos.set(dir).setLength(10).add(pos);

        impactTimes.clear();

        // getting target
        if (owner != null && !owner.readyToDispose) {

            targetList = GameScreen.getCloseObjects(owner, maxRange);

            // leave only ships and missiles
            targetList.removeIf(o -> o == owner || o.owner == owner || o.readyToDispose ||
                !o.type.contains(ObjectType.MISSILE) && !o.type.contains(ObjectType.SHIP));


//            // Определение скопление целей (ракет) в одной точке - если есть - стрелять только туда
//
//            for(GameObject o : targetList) {
//
//                if(o.type.contains(ObjectType.MISSILE) && !o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
//
//                    missilesList = GameScreen.getCloseObjects(o, 100);
//
//                    // collect only missiles nearby my missile
//                    missilesList.removeIf( g -> g == owner || g.owner == owner || g.readyToDispose);
//
//                    missilesList = missilesList.stream().filter(g -> g.type.contains(ObjectType.MISSILE) &&
//                        !g.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)).collect(Collectors.toList());
//
//                    if (missilesList.size() >=2) {
//
//                        targetList.clear();
//                        targetList.add(o);
//                        break;
//                    }
//
//                }
//            }



            targetList = targetList.stream().filter(o ->

                o.type.contains(ObjectType.BASIC_MISSILE) && (firingMode == FiringMode.AUTOMATIC || firingMode == FiringMode.FLAK_ONLY) ||

                    (/*o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) ||*/ o.type.contains(ObjectType.SHIP))
                        && (firingMode == FiringMode.AUTOMATIC || firingMode == FiringMode.PLASMA_ONLY)

            ).collect(Collectors.toList());


            for (GameObject o : targetList) {

                if (owner != null && !owner.readyToDispose) {
                    float maxPrjVel = power / firingAmmoType.getMass() * dt;  // Задаем начальную скорость пули
                    pbu.guideGun(owner, o, maxPrjVel, dt);
                }
                // get results

                Float impactTime = (float) pbu.guideResult.impactTime;

                if (!impactTime.isNaN() && impactTime >= 0 && impactTime <= maxImpactTime) {

                    impactTimes.put(impactTime, pbu.guideResult.clone());
                }
            }

        }


        target = null;
        guideVector.setZero();
        currentFuse = 0;







//        if (impactTimes.size() > 0) {
//
//            AtomicInteger missileCount = new AtomicInteger();
//
//            for (Map.Entry<Float, BPU.GuideResult> entry : impactTimes.entrySet()) {
//
//                GameObject o = entry.getValue().target;
//
//                tmp1.set(o.pos).sub(pos);
//
//                if (entry.getValue().impactTime < maxImpactTime/2f &&
//                    o.type.contains(ObjectType.MISSILE) && !o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
//
//                    missileCount.incrementAndGet();
//                }
//
//            }
//
//
//
//            BPU.GuideResult fgr = impactTimes.firstEntry().getValue();
//            double fim = fgr.impactTime;
//            GameObject ftgt = fgr.target;
//            boolean shootAtFirstShip = fim < 1f && ftgt.type.contains(ObjectType.SHIP);
//
//
//
//            // Если нет близко корабля
//            if (!shootAtFirstShip && missileCount.get() >= 3) {
//                firingMode = FiringMode.FLAK_ONLY;
//            } else {
//                firingMode = firingModeOld;
//            }
//        }


        // Определение скопление целей (ракет) в одной точке - если есть - стрелять только туда


        if (impactTimes.size() > 0) {

            BPU.GuideResult fgr = impactTimes.firstEntry().getValue();
            double fim = fgr.impactTime;
            GameObject ftgt = fgr.target;
            boolean shootAtClosestShip = fim < 1f && ftgt.type.contains(ObjectType.SHIP);

            // Closest Ship
            if (shootAtClosestShip) {

                firingMode = FiringMode.PLASMA_ONLY;
            }
            else {

                firingMode = firingModeOld;

                // Group of missiles
                boolean groupMissilesFound = false;
                for (Map.Entry<Float, BPU.GuideResult> entry : impactTimes.entrySet()) {

                    GameObject o = entry.getValue().target;

                    if (o.type.contains(ObjectType.MISSILE) && !o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {

                        missilesList = GameScreen.getCloseObjects(o, 150);

                        // collect only missiles nearby my missile
                        missilesList.removeIf(g -> g == owner || g.owner == owner || g.readyToDispose);

                        missilesList = missilesList.stream().filter(g -> g.type.contains(ObjectType.MISSILE) &&
                            !g.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)).collect(Collectors.toList());

                        if (missilesList.size() >= 2 || firingMode == FiringMode.FLAK_ONLY) {

                            groupMissilesFound = true;
                            impactTimes.clear();
                            impactTimes.put(entry.getKey(), entry.getValue());
                            firingMode = FiringMode.FLAK_ONLY;
                            break;
                        }

                    }
                }

                //  Firing on distant ships/newton_missile
                if (!groupMissilesFound /*&& firingMode != FiringMode.FLAK_ONLY*/) {

                    impactTimes.entrySet().removeIf(ge -> ge.getValue().target.type.contains(ObjectType.MISSILE) &&
                        !ge.getValue().target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE));
                }            }
        }

//        impactTimes.entrySet().removeIf(fg ->
//
//            (fg.getValue().target.type.contains(ObjectType.SHIP) || fg.getValue().target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) &&
//                fg.getKey() > 5
//        );


        if(impactTimes.size() > 0) {

            BPU.GuideResult gRes = impactTimes.firstEntry().getValue();


            target = gRes.target;
            guideVector.set(gRes.guideVector);

            float fuseMultiplier = 0.5f;
            if (target.type.contains(ObjectType.SHIP)) {
                shellType = ShellType.PLASMA;
            }
            else if (target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
                shellType = ShellType.PLASMA;
                fuseMultiplier = 0.8f;
            }

            if (target.type.contains(ObjectType.MISSILE) && !target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
                shellType = ShellType.FRAG;
                fuseMultiplier = 0.3f;

            }



            currentFuse = (long) (gRes.impactTime * 1/dt * fuseMultiplier);
        }


        // Auto fire control
        if (target != null && !target.readyToDispose &&
            Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {

            startFire();

        }
        else {
            stopFire();
        }

    }


    @Override
    protected void rotateObject() {

        // ToDo: перенести в GameObject.update()
        // rotation dynamics --------------------------------
        // Aiming
        if (!guideVector.isZero()) {

            // angle between direction and guideVector
            float guideAngle = dir.angleRad(guideVector);

            float doAngle = Math.min(Math.abs(guideAngle), maxRotationSpeed);

            if (guideAngle < 0) {
                doAngle = -doAngle;
            }
            dir.rotateRad(doAngle);
        }
    }


    @Override
    protected Projectile createProjectile() {

        Projectile result;

        //shellType = ShellType.PLASMA;

        if (shellType== ShellType.FRAG) {

            result = new FlakShell(calibre, 1, Color.RED, owner);
            ((FlakShell) result).isReadyElements = true;

            //fireRate = oldFireRate*1.5f;
            //gunHeat = 0;

        }
        else {
            result = new PlasmaFlakShell(calibre, 1, Color.GOLD, owner);
            //fireRate = oldFireRate;
        }

        //  предохранитель от самоподрыва
        if (currentFuse > 10) {
            result.setTTL(currentFuse);
        }
        else {
            result.setTTL(10);
        }

        return result;

    }


    protected void playFireSound() {
        cannonFire.play(0.4f);
    }



    private enum ShellType {

        FRAG(FlakShell.class),
        PLASMA(PlasmaFlakShell.class);

        Class type;

        ShellType(Class type) {
            this.type = type;
        }
    }






    public enum FiringMode {

        FLAK_ONLY,
        PLASMA_ONLY,
        AUTOMATIC
    }


}
