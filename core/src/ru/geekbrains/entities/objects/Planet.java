package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.projectile.FragMissile;
import ru.geekbrains.entities.projectile.Missile;
import ru.geekbrains.entities.projectile.Shell;
import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.screen.GameScreen;

public class Planet extends GameObject {

    private static Texture missileTexture;

    private static float MAX_TOLERANCE = 2f;

    private float tolerance = MAX_TOLERANCE;

    private GameObject target;

    private Vector2 guideVector = new Vector2();

    private BPU pbu = new BPU();

    static {
        missileTexture = new Texture("M-45_missile2.png");
    }

    private Gun gun;

    public Planet(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        name = "planet";


        mass = 1000000f;
        this.type.add(ObjectType.PLANET);


        gun = new Gun(radius * 0.3f, this) {

            @Override
            protected void rotateObject() {}

            @Override
            protected GameObject createProjectile() {

                Shell result = new Shell(calibre, owner);
                //result.setMass(0.16f);
                return result;
            }
        };


        gun.power = 260;
        gun.fireRate = 0.01f;
        target = null;
        gun.recalibrate();
    }

    // Силы планетарной обороны
    public void hit(GameObject o) {

        tolerance -= o.damage;


        if (tolerance < 0) {

            tolerance = MAX_TOLERANCE;


            // revenge to intruder
            if (o.owner != null && !o.owner.readyToDispose && o.owner != this ) {

                target = o.owner;

                launch();
            }
            else {

                List<GameObject> list = GameScreen.getHittableObjects();
                list = list.stream().filter(ob->ob.type.contains(ObjectType.SHIP) && !ob.readyToDispose && ob!= this).collect(Collectors.toList());

                if(list.size() > 0) {
                    target = list.get(MathUtils.random(0, list.size() - 1));
                    launch();
                }
            }
        }

    }


    private void launch() {

        gun.target = target;
        Missile missile;
        //int tryCount = 0;

        //do {

            tmp0.set(target.pos).sub(pos);
            tmp0.scl(radius * 1.3f / tmp0.len());


            float fromAn = (float) (Math.PI / 2);
            float toAn = (float) (Math.PI / 2);
            float fi_min = (tmp0.angleRad() - fromAn);
            float fi_max = (tmp0.angleRad() + toAn);
            tmp0.rotateRad(MathUtils.random(fi_min, fi_max));


            missile = new FragMissile(new TextureRegion(missileTexture), 4.5f, this);
            missile.setMass(1f);
            missile.maxFuel = 100;
            missile.setMaxThrottle(50f);
            missile.maxRotationSpeed /= 2;

            missile.pos.set(tmp0);
            missile.dir.set(tmp0).nor();
            missile.vel.set(tmp0).nor().scl(230);
            missile.target = target;
            missile.owner = this;
            //tryCount++;
        //}
        //while (GameScreen.getCloseObjects(missile, missile.radius*2.1f).size() > 0 && tryCount < 10 );
        //    GameScreen.addObject(missile);
        GameScreen.addObject(missile);

    }


    private void fire(float dt) {

        if (target != null && !target.readyToDispose) {



            // скорость снаряда
            float maxVel = gun.power / gun.firingAmmoType.getMass() * dt;
            pbu.guideGun(this, target, maxVel, dt);

            if (!pbu.guideResult.guideVector.isZero()) {
                guideVector.set(pbu.guideResult.guideVector.nor());
            }

//            // Самонаведение не сгидродоминировало
//            if (guideVector.isZero()) {
//                guideVector.set(target.pos).sub(pos).nor();
//            }
            if (pbu.guideResult.impactTime < 1.8f) {

                gun.dir.set(guideVector).nor();

                gun.startFire();
            }
            else {
                gun.stopFire();
            }
        }
        else {

            gun.stopFire();
        }

    }


    @Override
    public void update(float dt) {
        gun.update(dt);
        fire(dt);

    }

}
