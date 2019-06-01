package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

import ru.geekbrains.entities.objects.Bullet;
import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.EnemyShip;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Projectile;
import ru.geekbrains.screen.GameScreen;


public class Minigun extends Gun {


    // Скопировано из DrivenObject, EnemyShip
    // походу нужно множественное наследование из C++

    public GameObject target;                       // цель
    protected Vector2 guideVector = new Vector2(); // вектор куда нужно целиться

    public float maxRotationSpeed = 0.07f; // maximum rotation speed

    public float maxRange = 450f;

    public Minigun(float height, GameObject owner) {

        super(height, owner);

        radius = 50;
        calibre = 2;
        fireRate = 1;
        gunHeatingDelta = 10;
        coolingGunDelta = 2;
        maxGunHeat = 200;
        power = 40;

    }


    @Override
    public void update(float dt) {


        if (target != null && target.readyToDispose) {
            target = null;
        }

        // --------------------------------------------------
        // getting target

        List<GameObject> targets;

        if (target == null){

            targets =  GameScreen.getCloseObjects(owner, maxRange);

            if (targets.size() > 1) { // на радаре есть кто-то кроме меня самого

                for (GameObject o : targets) {

                    // берем первую попавшуюся цель (она будет ближе всего к границе радара)
                    // которая не является ни owner ни его снарядами
                    if (o != owner &&
                            o.owner != owner &&
                            !o.readyToDispose &&
                            o.type.contains(ObjectType.ENEMY_SHIP)) {

                        target = o;
                        break;
                    }
                }
            }
        }


        // target out of range - reset
        if (target != null) {

            tmp0.set(target.pos).sub(pos);
            if (tmp0.len() > maxRange) {

                target = null;
            }
        }



        // --------------------------------------------------
        //aiming target

        guideVector.setZero();


        EnemyShip.selfGuiding((DrivenObject)owner, target,guideVector);

        if(target != null) {

            guideVector.set(target.pos).sub(pos).nor();
        }


        // Auto fire control
        if (target != null &&
                Math.abs(dir.angleRad(guideVector)) < maxRotationSpeed) {

            startFire();
        }
        else {
            stopFire();
        }

        super.update(dt);
    }



    @Override
    protected void rotateGun() {

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

        return new Bullet(calibre, owner);
    }
}
