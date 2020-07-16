package ru.geekbrains.entities.weapons.launchers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.PlayerShip;
import ru.geekbrains.entities.projectile.Ammo;
import ru.geekbrains.entities.projectile.missile.AbstractMissile;
import ru.geekbrains.entities.projectile.missile.Missile;
import ru.geekbrains.entities.weapons.gun.NonRotatableGun;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public abstract class AbstractMissileLauncher extends NonRotatableGun {

    public final static Texture MISSILE_TEXTURE = new Texture("M-45_missile2.png");

    protected int sideLaunch = -1;

    public int sideLaunchCount = 2;

    protected int repeatCount = 0;
    protected int maxRepeatCount = 1;

    public GameObject target = null;

    protected List<GameObject> targetList = new ArrayList<>();
    protected List<GameObject> visualTargets = new ArrayList<>();


    protected int launchCnt = 0;


    protected final int LAUNCH_DELAY_INITIAL = 20;
    protected int launchDelay = LAUNCH_DELAY_INITIAL;  // задержка между запусками ракет при залпе (чтоб не попали друг в друга)



    protected long start = -1;

    protected boolean reverseLaunch;

    //public MissileType missileType;



    public AbstractMissileLauncher(float height, GameObject owner) {

        super(height, owner);

        isModule = true;
        fireRate = 0.004f;
        gunHeatingDelta = 0;
        coolingGunDelta = 0;
        maxGunHeat = 1;
    }


    @Override
    public void update(float dt) {
        super.update(dt);

        if (owner == null || owner.readyToDispose) {
            return;
        }
    }







//    @Override
//    protected void fire(float dt) {
//
//        repeatFire();
//    }

    @Override
    protected void fire(float dt) {



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



        if (!selectTarget()) {
            return;
        }


        // Duplicate first target - will get 2 shot on it
        if(targetList.size() == 1) {
            targetList.add(targetList.get(0));
        }

        playFireSound(0.35f);

        Ammo missile = createAmmo();

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


        missile.getPos().set(tmp0);
        missile.getVel().set(owner.vel);
        missile.getDir().set(tmp6);
        missile.setTarget(target);

//        if(owner.getClass()==PlayerShip.class) {
//            System.out.println("MISSILE TARGET: " + target.getClass().getSimpleName() + " " + System.identityHashCode(target));
//        }





        //tmp1.set(dir).nor().scl(sideLaunch*100);
        // apply force applied to missile
        //tmp1.set(tmp6).scl((missile.boost) * 0.3f);    40
        //tmp0.set(tmp6).setLength((missile.boost)).rotate(30*sideLaunch)/*.add(tmp1)*/; // force

        tmp0.set(tmp6).setLength(missile.getFirePower());
        if (sideLaunchCount > 1) {
            tmp0.rotate(10 * sideLaunch);
        }


        if (reverseLaunch) {
            tmp0.scl(0.75f);
        }


        if (missile.getType().contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {
            tmp0.scl(3);
        }

        //tmp0.rotate(60 * sideLaunch);
        missile.applyForce(tmp0);

        //tmp0.set(dir).setLength(power);


        GameScreen.addAmmo(missile);


        // invert launch side ---------------------------------------------
        sideLaunch = -sideLaunch;
    }


    protected boolean selectTarget() {

        GameObject tmp = ((DrivenObject)owner).target;

        if (tmp == null || tmp.readyToDispose){
            return false;
        }

        targetList.add(tmp);
        return true;
    }


    public void reverse(boolean reverseLaunch) {
        this.reverseLaunch = reverseLaunch;
    }


    // no visual effects
    @Override
    public void draw(Renderer renderer) {

    }


//    @Override
//    public void draw(Renderer renderer) {
//
//        super.draw(renderer);
//
//        if (renderer.rendererType!=RendererType.SHAPE) {
//            return;
//        }
//
//        // костыли, нарушение Лискова, нужно выделить в  класс abstract BaseMissileLauncher
//        if (this.getClass() ==  AbstractMissileLauncher.class &&
//            this.owner.getClass() == PlayerShip.class) {
//
//
//            for (GameObject o : visualTargets) {
//
//                // Рисуем перекрестье на цели
//                if (o != null && !o.readyToDispose) {
//
//
//                    ShapeRenderer shape = renderer.shape;
//
//                    Gdx.gl.glLineWidth(1);
//                    Gdx.gl.glEnable(GL20.GL_BLEND);
//                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//                    //shape.begin();
//                    shape.set(ShapeRenderer.ShapeType.Line);
//
//                    shape.setColor(0.5f, 0.9f, 0.9f, 0.7f);
//                    shape.circle(o.pos.x, o.pos.y, o.getRadius() * 2);
//
//                    tmp0.set(o.pos).sub(o.getRadius() * 2, 0);
//                    tmp1.set(tmp0).set(o.pos).add(o.getRadius() * 2, 0);
//                    shape.line(tmp0, tmp1);
//
//                    tmp0.set(o.pos).sub(0, o.getRadius() * 2);
//                    tmp1.set(tmp0).set(o.pos).add(0, o.getRadius() * 2);
//                    shape.line(tmp0, tmp1);
//                    Gdx.gl.glLineWidth(1);
//                    shape.flush();
//
//
//                    //shape.end();
//                }
//
//
//            }
////            // Рисуем перекрестье на цели
////            if (target != null && !target.readyToDispose) {
////
////                ShapeRenderer shape = renderer.shape;
////
////                Gdx.gl.glLineWidth(1);
////                Gdx.gl.glEnable(GL20.GL_BLEND);
////                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
////                //shape.begin();
////                shape.set(ShapeRenderer.ShapeType.Line);
////
////                shape.setColor(0.5f, 0.9f, 0.9f, 0.5f);
////                shape.circle(target.pos.x, target.pos.y, target.getRadius() * 2);
////
////                tmp0.set(target.pos).sub(target.getRadius() * 2, 0);
////                tmp1.set(tmp0).set(target.pos).add(target.getRadius() * 2, 0);
////                shape.line(tmp0, tmp1);
////
////                tmp0.set(target.pos).sub(0, target.getRadius() * 2);
////                tmp1.set(tmp0).set(target.pos).add(0, target.getRadius() * 2);
////                shape.line(tmp0, tmp1);
////
////
////                //shape.end();
////            }
//        }
//
//    }

    /*
    @Override
    protected GameObject createProjectile() {

        GameObject result = null;

        if (owner.type.contains(ObjectType.PLAYER_SHIP)) {




            if (missileType== MissileType.Fast) {

                result = new FastMissile(new TextureRegion(MISSILE_TEXTURE), 1.5f, owner);
            }
            else  if (missileType== MissileType.HE){
                result = new Missile(new TextureRegion(MISSILE_TEXTURE), 2, owner);
            }



            //result =  new NewtonMissile(new TextureRegion(MISSILE_TEXTURE), 5, owner);

            //result =  new PlasmaFragMissile(new TextureRegion(MISSILE_TEXTURE), 2.5f, owner);
        }
        else if(owner.type.contains(ObjectType.MAIN_ENEMY_SHIP)) {

            float rnd = ThreadLocalRandom.current().nextFloat();

            if (rnd >= 0.5) {
                result = new EmpMissile(new TextureRegion(MISSILE_TEXTURE), 2, owner);
            } else {
                result = new Missile(new TextureRegion(MISSILE_TEXTURE), 2, owner);
            }
        }
        else if (owner.type.contains(ObjectType.MISSILE_ENEMY_SHIP)) {
            result = new PlasmaFragMissile(new TextureRegion(MISSILE_TEXTURE), 2.5f, owner);
        }

        if(result == null) {
            result = new Missile(new TextureRegion(MISSILE_TEXTURE), 2, owner);
        }

        return result;
    }
    */

//
//    // костыли, нарушение подстановки Лискова, выделить базовый функционал в класс abstract BaseMissileLauncher
//    // в абстрактный  метод playLaunchSound()
//    private void playLaunchSound() {
//
//        if (this.getClass() == AbstractMissileLauncher.class) {
//            MISSILE_FIRE_01.play(0.25f);
//        }
//    }




    @Override
    public void dispose() {

        stopFire();
        super.dispose();
    }



//    private enum MissileType {
//
//        HE(Missile.class),
//        Fast(FastMissile.class);
//        Class type;
//        MissileType(Class type) {
//            this.type = type;
//        }
//    }

}
