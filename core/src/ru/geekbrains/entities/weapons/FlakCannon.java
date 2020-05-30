package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.DummyObject;
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
    float maxImpactTimeFlak;

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

        // DEBUG
        //maxGunHeat = 999999999;
        //fireRate = 0.2f;

        power = 150;
        maxBlastRadius = 5;

        maxRange = 1500f;
        maxImpactTime = 5f;
        maxImpactTimeFlak = 3f;

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



//            targetList = targetList.stream().filter(o ->
//
//                (o.type.contains(ObjectType.SHIP)  || o.type.contains(ObjectType.BASIC_MISSILE)) &&
//                    (firingMode == FiringMode.AUTOMATIC || firingMode == FiringMode.FLAK_ONLY) ||
//
//                    (/*o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE) ||*/ o.type.contains(ObjectType.SHIP))
//                        && (firingMode == FiringMode.AUTOMATIC || firingMode == FiringMode.PLASMA_ONLY)
//
//            ).collect(Collectors.toList());

            targetList = targetList.stream().filter(o ->  o.type.contains(ObjectType.SHIP)  ||
                    o.type.contains(ObjectType.BASIC_MISSILE) ||
                    o.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE))
                    .collect(Collectors.toList());


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

            // Closest Ship - emergency take down
            if (shootAtClosestShip) {

                firingMode = FiringMode.PLASMA_ONLY;
            }
            else {

                firingMode = firingModeOld;

                switch (firingMode) {

                    case FLAK_ONLY:
                        impactTimes.entrySet().removeIf(e -> !e.getValue().target.type.contains(ObjectType.BASIC_MISSILE));
                        break;

                    case AUTOMATIC:
                        impactTimes.entrySet().removeIf(e ->
                                e.getValue().target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE));
                        break;

                    case PLASMA_ONLY:
                        impactTimes.entrySet().removeIf(e -> e.getValue().target.type.contains(ObjectType.BASIC_MISSILE));
                        break;


                }

//                if (firingMode == FiringMode.FLAK_ONLY) {
//
//                }
//                else if (firingMode == FiringMode.PLASMA_ONLY) {
//                    impactTimes.entrySet().removeIf(e -> e.getValue().target.type.contains(ObjectType.BASIC_MISSILE));
//                }

                // Group of missiles
                boolean groupMissilesFound = false;
                outer:
                for (Map.Entry<Float, BPU.GuideResult> entry : impactTimes.entrySet()) {

                    GameObject o = entry.getValue().target;

                    if (o.type.contains(ObjectType.BASIC_MISSILE)) {

                        missilesList = GameScreen.getCloseObjects(o, 150);

                        // collect only missiles nearby my missile
                        missilesList.removeIf(g -> g == owner || g.owner == owner || g.readyToDispose);

                        missilesList.removeIf(m -> !m.type.contains(ObjectType.BASIC_MISSILE));


                        // Inbound PLASMA_FRAG_MISSILE
                        List<BPU.GuideResult> incomingPlasmaFragMissiles = new ArrayList<>();
                        for (GameObject m : missilesList) {

                            if (m.type.contains(ObjectType.PLASMA_FRAG_MISSILE)) {

                                float maxPrjVel = power / firingAmmoType.getMass() * dt;  // Задаем начальную скорость пули
                                pbu.guideGun(owner, m, maxPrjVel, dt);
                                Double impactTime = pbu.guideResult.impactTime;

                                if (!impactTime.isNaN() && impactTime >= 0 && impactTime <= maxImpactTimeFlak) {

                                    incomingPlasmaFragMissiles.add(pbu.guideResult);
                                }

                                if (incomingPlasmaFragMissiles.size() > 0 &&
                                        (firingMode == FiringMode.FLAK_ONLY || impactTime < maxImpactTimeFlak/1.3f)) {

                                    groupMissilesFound = true;
                                    impactTimes.clear();
                                    impactTimes.put((float) incomingPlasmaFragMissiles.get(0).impactTime,
                                            incomingPlasmaFragMissiles.get(0));
                                    break outer;
                                }
                            }
                        }




                        if ((firingMode == FiringMode.FLAK_ONLY && missilesList.size() >= 2) ||
                                (firingMode == FiringMode.AUTOMATIC && missilesList.size() >= 3)) {

                            // calc center
                            tmp4.setZero();
                            tmp5.setZero();
                            tmp6.setZero();
                            for (GameObject m : missilesList) {
                                tmp4.add(m.pos);
                                tmp5.add(m.vel);
                                tmp6.add(m.acc);
                            }
                            tmp4.scl(1f/missilesList.size());
                            tmp5.scl(1f/missilesList.size());
                            tmp6.scl(1f/missilesList.size());


//                          impactTimes.put(entry.getKey(), entry.getValue());


                            DummyObject dummy = new DummyObject(o);
                            dummy.pos.set(tmp4);
                            dummy.vel.set(tmp5);
                            dummy.acc.set(tmp6);
                            dummy.type.addAll(o.type);

                            float maxPrjVel = power / firingAmmoType.getMass() * dt;  // Задаем начальную скорость пули
                            pbu.guideGun(owner, dummy, maxPrjVel, dt);
                            Float impactTime = (float) pbu.guideResult.impactTime;


                            if (!impactTime.isNaN() && impactTime >= 0 && impactTime <= maxImpactTimeFlak) {

                                groupMissilesFound = true;
                                impactTimes.clear();
                                impactTimes.put(impactTime, pbu.guideResult.clone());
                            }
                            break;
                        }
                    }
                }

                //  Firing on distant ships/newton_missile
                if (!groupMissilesFound) {

                    impactTimes.entrySet().removeIf(ge -> ge.getValue().target.type.contains(ObjectType.BASIC_MISSILE));
                }
            }
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

            float fuseMultiplier = 0.9f;
            if (target.type.contains(ObjectType.SHIP)) {
                shellType = ShellType.PLASMA;
            }
            else if (target.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
                shellType = ShellType.PLASMA;
                fuseMultiplier = 0.8f; // 0.8
            }

            if (target.type.contains(ObjectType.BASIC_MISSILE) ) {
                shellType = ShellType.FRAG;

                fuseMultiplier = (float) (gRes.impactTime/7f);

                //fuseMultiplier = 0.3f;

                // DEBUG
                //fuseMultiplier = 2;


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
