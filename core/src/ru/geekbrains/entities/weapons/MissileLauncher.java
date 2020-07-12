package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.DummyObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.PlayerShip;
import ru.geekbrains.entities.projectile.missile.AbstractMissile;
import ru.geekbrains.entities.projectile.missile.EmpMissile;
import ru.geekbrains.entities.projectile.missile.FastMissile;
import ru.geekbrains.entities.projectile.missile.PlasmaFragMissile;
import ru.geekbrains.entities.projectile.missile.Missile;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.shell.FlakShell;
import ru.geekbrains.entities.projectile.shell.PlasmaFlakShell;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public class MissileLauncher extends Gun {


    public static enum Side {
        LEFT,
        RIGHT;
    }






    private static Sound missileFire01;
    private static Texture missileTexture;

    private int sideLaunch = -1;

    public int sideLaunchCount = 2;

    protected int repeatCount = 0;
    protected int maxRepeatCount = 1;



    private DummyObject dummy;

    public GameObject target = null;

    private List<GameObject> targetList = new ArrayList<>();
    private List<GameObject> visualTargets = new ArrayList<>();


    private int launchCnt = 0;

    protected int TTL = 10;  // задержка между запусками ракет при залпе (чтоб не попали друг в друга)

    protected long start = -1;

    private boolean reverseLaunch;

    MissileType missileType;

    static {
        missileFire01 = Gdx.audio.newSound(Gdx.files.internal("launch04.mp3"));
        missileTexture = new Texture("M-45_missile2.png");
    }


    public MissileLauncher(float height, GameObject owner) {

        super(height, owner);

        type.add(ObjectType.MISSILE_LAUNCHER);

        isModule = true;

        dummy = new DummyObject(owner);

        fireRate = 0.004f;
        gunHeatingDelta = 0;
        coolingGunDelta = 0;
        maxGunHeat = 1;
        //power = 500;


    }


    @Override
    public void update(float dt) {
        super.update(dt);

        if (owner == null || owner.readyToDispose) {
            return;
        }

        // костыли, нарушение подстановки Лискова, выделить базовый функционал в класс abstract BaseMissileLauncher
        if (this.getClass() ==  MissileLauncher.class) {


            if (start > 0 && GameScreen.INSTANCE.getTick() - start > TTL) {

                repeatFire();

                if (repeatCount >= maxRepeatCount) {

                    repeatCount = 0;
                    start = -1;
                    reverseLaunch = false;
                    launchCnt = 0;
                }
            }

        }
    }







    @Override
    protected void fire(float dt) {

        repeatFire();
    }

    private List<GameObject> getTarget() {

        List<GameObject> result = new ArrayList<>();

        List<GameObject> targets;
        dummy.pos.set(GameScreen.INSTANCE.target);

        targets = GameScreen.getCloseObjects(dummy, 2000);

        targets.removeIf(t -> (!t.type.contains(ObjectType.SHIP) && !t.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)));
        targets.removeIf(t -> t == this.owner);
        targets.removeIf(t -> t.owner == this.owner);
        targets.removeIf(t -> t.readyToDispose);
        targets.removeIf(t -> t.side == owner.side);

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


    protected void repeatFire() {



        // запуск двух ракет с задержкой
        // (чтобы одна в другую не влетела при подлете к цели)
        if (sideLaunchCount > 1) {
            start = GameScreen.INSTANCE.getTick();
        }
        repeatCount++;


        // ???
        tmp6.set(dir);
        if (reverseLaunch) {
            tmp6.scl(-1);
        }



        if (owner.type.contains(ObjectType.PLAYER_SHIP) &&
            this.getClass() ==  MissileLauncher.class) {

            if(launchCnt == 0) {

                maxRepeatCount = 1;
                missileType = MissileType.HE;

                targetList = getTarget();

//                for (GameObject o : targetList) {
//                    if(o == null) {throw new RuntimeException("o == null");}
//                }

                visualTargets.clear();
            }

            if (launchCnt == 1) {
                targetList.removeIf(o-> o.readyToDispose);
            }


            if (targetList.size() == 0) {
                launchCnt = 0;
                return;

            }


            if (launchCnt == 0 && targetList.get(0).type.contains(ObjectType.MISSILE_ENEMY_SHIP)) {
                maxRepeatCount = 4;
                missileType = MissileType.Fast;
            }

            // if first target is NewtonMissile/MissileEnemyShip - remove other targets
            if (targetList.get(0).type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {

                GameObject tmp = targetList.get(0);
                targetList.clear();
                targetList.add(tmp);
            }



            launchCnt++;
            if (launchCnt >= sideLaunchCount) {
                launchCnt = 0;
            }










            if (launchCnt == 1) {
                visualTargets.addAll(targetList);
            }

        }
        else if (!owner.type.contains(ObjectType.PLAYER_SHIP)) {

            GameObject tmp = ((DrivenObject)owner).target;

            if (tmp == null || tmp.readyToDispose){
                return;
            }

            targetList.add(tmp);
        }



        // Duplicate first target - will get 2 shot on it
        if(targetList.size() == 1) {
            targetList.add(targetList.get(0));
        }


        AbstractMissile missile = (AbstractMissile)createProjectile();
        playLaunchSound();

        tmp0.set(tmp6).setLength(owner.getRadius() + missile.getRadius()*3)
            .rotate(90*sideLaunch).add(owner.pos);

        tmp1.set(tmp6).setLength(owner.getRadius() + missile.getRadius()*3)
            .rotate(-90*sideLaunch).add(owner.pos);

        try {


            tmp2.set(targetList.get(0).pos).sub(tmp0);
            tmp3.set(targetList.get(1).pos).sub(tmp0);

            tmp4.set(targetList.get(0).pos).sub(tmp1);
            tmp5.set(targetList.get(1).pos).sub(tmp1);

        }
        catch (Exception e) {

            System.out.println("Error: " + e.toString());

            System.out.println("tmp3: " + tmp3.toString());
            System.out.println("tmp0: " + tmp0.toString());

            System.out.println(targetList);
            System.out.println(targetList.get(1));
            System.out.println(targetList.get(1).pos);


        }


        //System.out.println(tmp2.len() + " " + tmp4.len());
        //System.out.println(tmp3.len() + " " + tmp5.len());

        // OK
        if (tmp2.len() < tmp4.len() &&
            tmp5.len() < tmp3.len()) {

            //sideLaunch = -sideLaunch;
            //tmp0.set(tmp1);
            target = targetList.remove(0);

            //
        }
        else {
            target = targetList.remove(1);
        }


        missile.pos.set(tmp0);
        missile.vel.set(owner.vel);
        missile.dir.set(tmp6);
        missile.target = target;

//        if(owner.getClass()==PlayerShip.class) {
//            System.out.println("MISSILE TARGET: " + target.getClass().getSimpleName() + " " + System.identityHashCode(target));
//        }





        //tmp1.set(dir).nor().scl(sideLaunch*100);
        // apply force applied to missile
        //tmp1.set(tmp6).scl((missile.boost) * 0.3f);    40
        //tmp0.set(tmp6).setLength((missile.boost)).rotate(30*sideLaunch)/*.add(tmp1)*/; // force

        tmp0.set(tmp6).setLength(missile.boost);
        if (sideLaunchCount > 1) {
            tmp0.rotate(15 * sideLaunch);
        }


        if (reverseLaunch) {
            tmp0.scl(0.75f);
        }


        if (missile.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
            tmp0.scl(3);
        }

        //tmp0.rotate(60 * sideLaunch);
        missile.applyForce(tmp0);

        //tmp0.set(dir).setLength(power);


        GameScreen.addObject(missile);


        // invert launch side ---------------------------------------------
        sideLaunch = -sideLaunch;
    }

    public void reverse(boolean reverseLaunch) {
        this.reverseLaunch = reverseLaunch;
    }


    @Override
    public void draw(Renderer renderer) {

        super.draw(renderer);

        if (renderer.rendererType!=RendererType.SHAPE) {
            return;
        }

        // костыли, нарушение Лискова, нужно выделить в  класс abstract BaseMissileLauncher
        if (this.getClass() ==  MissileLauncher.class &&
            this.owner.getClass() == PlayerShip.class) {


            for (GameObject o : visualTargets) {

                // Рисуем перекрестье на цели
                if (o != null && !o.readyToDispose) {


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
                    Gdx.gl.glLineWidth(1);
                    shape.flush();


                    //shape.end();
                }


            }
//            // Рисуем перекрестье на цели
//            if (target != null && !target.readyToDispose) {
//
//                ShapeRenderer shape = renderer.shape;
//
//                Gdx.gl.glLineWidth(1);
//                Gdx.gl.glEnable(GL20.GL_BLEND);
//                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//                //shape.begin();
//                shape.set(ShapeRenderer.ShapeType.Line);
//
//                shape.setColor(0.5f, 0.9f, 0.9f, 0.5f);
//                shape.circle(target.pos.x, target.pos.y, target.getRadius() * 2);
//
//                tmp0.set(target.pos).sub(target.getRadius() * 2, 0);
//                tmp1.set(tmp0).set(target.pos).add(target.getRadius() * 2, 0);
//                shape.line(tmp0, tmp1);
//
//                tmp0.set(target.pos).sub(0, target.getRadius() * 2);
//                tmp1.set(tmp0).set(target.pos).add(0, target.getRadius() * 2);
//                shape.line(tmp0, tmp1);
//
//
//                //shape.end();
//            }
        }

    }

    @Override
    protected GameObject createProjectile() {

        GameObject result = null;

        if (owner.type.contains(ObjectType.PLAYER_SHIP)) {




            if (missileType== MissileType.Fast) {

                result = new FastMissile(new TextureRegion(missileTexture), 1.5f, owner);
            }
            else  if (missileType== MissileType.HE){
                result = new Missile(new TextureRegion(missileTexture), 2, owner);
            }



            //result =  new NewtonMissile(new TextureRegion(missileTexture), 5, owner);

            //result =  new PlasmaFragMissile(new TextureRegion(missileTexture), 2.5f, owner);
        }
        else if(owner.type.contains(ObjectType.MAIN_ENEMY_SHIP)) {

            float rnd = ThreadLocalRandom.current().nextFloat();

            if (rnd >= 0.5) {
                result = new EmpMissile(new TextureRegion(missileTexture), 2, owner);
            } else {
                result = new Missile(new TextureRegion(missileTexture), 2, owner);
            }
        }
        else if (owner.type.contains(ObjectType.MISSILE_ENEMY_SHIP)) {
            result = new PlasmaFragMissile(new TextureRegion(missileTexture), 2.5f, owner);
        }

        if(result == null) {
            result = new Missile(new TextureRegion(missileTexture), 2, owner);
        }

        return result;
    }


    // костыли, нарушение подстановки Лискова, выделить базовый функционал в класс abstract BaseMissileLauncher
    // в абстрактный  метод playLaunchSound()
    private void playLaunchSound() {

        if (this.getClass() == MissileLauncher.class) {
            missileFire01.play(0.25f);
        }
    }




    @Override
    public void dispose() {

        stopFire();
        super.dispose();
    }



    private enum MissileType {

        HE(Missile.class),
        Fast(FastMissile.class);
        Class type;
        MissileType(Class type) {
            this.type = type;
        }
    }

}
