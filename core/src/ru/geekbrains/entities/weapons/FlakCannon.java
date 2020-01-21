package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.FlakShell;
import ru.geekbrains.entities.projectile.Fragment;
import ru.geekbrains.entities.projectile.PlasmaFlakShell;
import ru.geekbrains.entities.projectile.Projectile;
import ru.geekbrains.screen.GameScreen;

public class FlakCannon extends Gun {

    float maxRange;
    float maxTime;

    ShellType shellType;

    private static Sound cannonFire;

    // Цели, отсортированные по времени попадания в корабль
    private NavigableMap<Float, BPU.GuideResult> impactTimes = new TreeMap<>();

    private List<GameObject> targetList = new ArrayList<>();

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
        power = 300;
        maxBlastRadius = 5;

        maxRange = 2000f;
        maxTime = 4f;

        maxRotationSpeed = 0.2f;

        //maxRotationSpeed = 1f;

    }

    @Override
    public void update(float dt) {

        super.update(dt);

        nozzlePos.set(dir).setLength(10).add(pos);

        impactTimes.clear();

        // getting target
        if (owner != null && !owner.readyToDispose) {

            targetList = GameScreen.getCloseObjects(owner, maxRange);

            targetList.removeIf( o -> o == owner || o.owner == owner || o.readyToDispose ||
                                 !o.type.contains(ObjectType.MISSILE) && !o.type.contains(ObjectType.SHIP));


            for (GameObject o : targetList) {

                if (owner != null && !owner.readyToDispose) {
                    float maxPrjVel = power / firingAmmoType.getMass() * dt;  // Задаем начальную скорость пули
                    pbu.guideGun(owner, o, maxPrjVel, dt);
                }
                // get results

                Float impactTime = (float) pbu.guideResult.impactTime;

                if (!impactTime.isNaN() && impactTime >= 0 && impactTime <= maxTime) {

                    impactTimes.put(impactTime, pbu.guideResult.clone());
                }
            }

        }


        target = null;
        guideVector.setZero();
        currentFuse = 0;

        if(impactTimes.size() > 0) {

            BPU.GuideResult gRes = impactTimes.firstEntry().getValue();
            target = gRes.target;
            guideVector.set(gRes.guideVector);
            currentFuse = (long) (gRes.impactTime * 1/dt - gRes.impactTime * 1/dt*0.3f);


            if (target.type.contains(ObjectType.SHIP)) {
                shellType = ShellType.PLASMA;
                power = 450;
            }
            else {
                shellType = ShellType.FRAG;
                power = 300;
            }
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
        }
        else {
            result = new PlasmaFlakShell(calibre, 1, Color.GOLD, owner);
            currentFuse*=0.9;
        }

        //  предохранитель от самоподрыва
        if (currentFuse > 0) {
            result.setTTL(currentFuse);
        }
        else {
            result.setTTL(100);
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


}
