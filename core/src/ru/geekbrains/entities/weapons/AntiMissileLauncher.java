package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.geekbrains.entities.projectile.AntiMissile;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;

public class AntiMissileLauncher extends MissileLauncher {

    private static Texture missileTexture;

    public float maxRange = 1000;

    // Список целей, по которым идет огонь
    // (По которым запущены противо-ракеты и идет поражение)
    protected Map<GameObject, AntiMissile> targetMissile = new HashMap<>(); // Назначение антиракет по целям

    private List<GameObject> inboundMissiles = new ArrayList<>();


    //AntiMissileLauncher.AimFunction gf;
    //UnivariateSolver nonBracketing;


    static {
        missileTexture = new Texture("M-45_missile2.png");
    }

    public AntiMissileLauncher(float height, GameObject owner) {
        super(height, owner);

        //fireRate = 0.1f;
        fireRate = 0.05f;
        gunHeatingDelta = 50;
        coolingGunDelta = 1.5f;
        //coolingGunDelta = 60f;
        maxGunHeat = 200;
        power = 200;

    }


    @Override
    protected void fire(float dt) {


        if (target == null || target.readyToDispose ||
            owner.readyToDispose){
            return;
        }

        AntiMissile missile = (AntiMissile)createProjectile();


        targetMissile.put(target, missile);



        //tmp1.set(target.pos).sub(owner.pos);


        float maxPrjVel = power / firingAmmoType.getMass() * dt;  // Задаем начальную скорость пули
        pbu.guideGun(owner, target, maxPrjVel, dt);
        // get results

        if (!pbu.guideResult.guideVector.isZero()) {
            guideVector.set(pbu.guideResult.guideVector.nor());
        }

        // Самонаведение не сгидродоминировало
        if (guideVector.isZero()) {
            guideVector.set(target.pos).sub(owner.pos).nor();
        }

        /*

        // Берем в качестве точки прицеливания(выпуска ракеты) линейное приближение

        // 1. get distance to target
        float dst = tmp1.len() - (owner.getRadius() + target.getRadius());
        if (dst < 0) {
            dst = 0;
        }

        //tmp4.set(tmp1).nor();

        // 2. get speed vector projection to vector target - ship

        tmp2.set(target.vel);
        float pr1 = -tmp2.dot(tmp1)/tmp1.len();

        //tmp2.set(tmp1).nor().scl(pr).scl(-1f);
        //tmp2.nor().scl(pr);

        // same with ship speed, then
        tmp3.set(owner.vel);
        float pr2 = tmp3.dot(tmp1)/tmp1.len();
        //tmp3.nor().scl(pr);
        //tmp3.set(tmp1).nor().scl(pr);

        // sum both projections (sub due to inverse)
        //float pr3 = pr1 + pr2;
        //tmp4.set(tmp2).add(tmp3);


        // ~time to collision
        float tt = Math.abs(dst/(pr1 + pr2));

        // дистанция, которую пройдет цель
        tmp4.set(target.vel).scl(tt);

        // позиция цели через tt
        tmp3.set(target.pos).add(tmp4);


        tmp1.set(tmp3).sub(owner.pos).nor();
*/

        //dir.set(tmp1);

        // set launcher toward guide point (rotate cost zero time)
        dir.set(guideVector).nor();

        tmp0.set(dir).setLength(owner.getRadius() + missile.getRadius()*10).add(owner.pos);

        missile.pos.set(tmp0);
        missile.vel.set(owner.vel);
        missile.dir.set(dir);

        missile.target = target;


        // apply force applied to missile
        tmp0.set(dir).scl(power * 0.3f); //.add(tmp1); // force
        missile.applyForce(tmp0);

        GameScreen.addObject(missile);
    }



    @Override
    public void update(float dt) {

//
//        // clear current target
//        if (target != null && target.readyToDispose) {
//            target = null;
//        }

        target = null;

        // Убираем из списка целей, по которым идет огонь
        // уничтоженные цели
        // Или цели, находящиеся за пределами работы системы
        // Или цели, по которым не ведется огонь противоракетами (противоракеты сбиты)
        // Сейчас по одной цели запускается ровно 1 противоракета
        Iterator<Map.Entry<GameObject, AntiMissile>> it = targetMissile.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<GameObject, AntiMissile> pair = it.next();

            GameObject  o  = pair.getKey();
            AntiMissile m = pair.getValue();

            // цель уничтожена
            if (o.readyToDispose) {
                it.remove();
                continue;
            }

            // цель вышла за радиус поражения
            tmp0.set(o.pos).sub(owner.pos);
            if (tmp0.len() > maxRange*2){

                it.remove();
                continue;
            }

            // цель жива, но ракета-перехватчик уничтожена
            if (m.readyToDispose) {
                it.remove();
            }

        }



        // --------------------------------------------------
        // getting target

        List<GameObject> targets;

        // inbound missiles
        inboundMissiles.clear();

        targets = GameScreen.getCloseObjects(owner, maxRange);

        // inbound missiles
        for (GameObject o : targets) {

            if (o != owner &&
                    o.owner != owner &&
                    !o.readyToDispose && (o.type.contains(ObjectType.MISSILE) ||
                    o.type.contains(ObjectType.SHIP))) {

                // Умеет сопровождать не более 6 целей одновременно
                if (inboundMissiles.size() > 6) {
                    break;
                }
                inboundMissiles.add(o);
            }
        }

        // В inboundMissiles лежит не более 6 целей,
        // отсортированных в порядке удаления





        // Умеет сопровождать не более 6 целей одновременно
        if(targetMissile.size() <= 6) {

            for (GameObject o : inboundMissiles) {

                // Если цели нет в списке targetMissile
                // То стрелять по этой цели
                if (!targetMissile.containsKey(o)) {

                    target = o;
                    break;
                }

            }
        }


        // Auto fire control
        if (target != null) {
            startFire();
        }
        else {
            stopFire();
        }

        super.update(dt);
    }


/*    @Override
    public void draw(Renderer renderer) {


        if (target!= null &&
                !target.readyToDispose ) {

            ShapeRenderer shape = renderer.shape;

            shape.begin();

            tmp0.set(pos);//.add(tmp4);
            //Gdx.gl.glLineWidth(1);
            shape.set(ShapeRenderer.ShapeType.Line);
            // reticle
            //        shape.setColor(0f, 1f, 0f, 1);
            //        shape.circle(tmp0.x, tmp0.y, 3);
            shape.setColor(1f, 1f, 0f, 0.5f);
            shape.line(pos, tmp3);
            shape.circle(tmp3.x, tmp3.y, 10);

            Gdx.gl.glLineWidth(2);
            shape.circle(target.pos.x, target.pos.y, 20);
            Gdx.gl.glLineWidth(1);
            shape.end();

        }


    }*/


    /*

    // ToDo: Говнокод, перенести в class Gun
    public static class AimFunction implements UnivariateFunction {

        public double rx, ry, vx, vy, ax, ay, VCC;


        public AimFunction() {}

        public double value(double t) {

            double result = Math.pow(rx,2) + Math.pow(ry,2) + ((Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(t,4))/4. +
                    Math.pow(t,3)*(ax*vx + ay*vy) + 2*t*(rx*vx + ry*vy) +
                    Math.pow(t,2)*(ax*rx + ay*ry - Math.pow(VCC,2) + Math.pow(vx,2) + Math.pow(vy,2));

            return result;
        }
    }

    // ToDo: Говнокод, перенести в class Gun
    public void selfGuiding(float dt) {

        // Система наведения для  minigun
        //https://gamedev.stackexchange.com/questions/149327/projectile-aim-prediction-with-acceleration


        if (target== null || target.readyToDispose)
            return;

        // F = m*a
        // a = f / m;
        // dv = a*t
        // a = dv/t;
        // f/m = dv/t
        // dv = f/m*t - Импульс силы, деленный на массу пули


                         // Лютый говнокод (0.01f)
        gf.VCC = power / 0.01f * dt;  // Начальная скорость пули


        // ORIGINAL
        // r =  rt - rs
        gf.rx = target.pos.x - owner.pos.x;
        gf.ry = target.pos.y - owner.pos.y;

        //  relative target velocity to object
        gf.vx = target.vel.x - owner.vel.x;
        gf.vy = target.vel.y - owner.vel.y;

        gf.ax = target.acc.x - owner.acc.x;
        gf.ay = target.acc.y - owner.acc.y;


        // Гидра доминатус !!!!


        //double tbd = 0;
        // Цикл - попытка отделить корни

        //int i_tt = 0;
        for (int i = 0; i< 100; i++) {
            try {

                // Корней нет - функция не пересекает ось Ox
                if (gf.value(0) > 0 && gf.value(dt * i*10) > 0 ||
                        gf.value(0) < 0 && gf.value(dt * i*10) < 0) {

                    continue;
                }

                double t = nonBracketing.solve(100, gf,  0, dt * i*10);

                //t -= 0.2;

                if (!Double.isNaN(t) && !Double.isInfinite(t) && t > 0) {

                    double vs_x = gf.rx / t + 0.5 * gf.ax * t + gf.vx;
                    double vs_y = gf.ry / t + 0.5 * gf.ay * t + gf.vy;

                    guideVector.set((float) vs_x, (float) vs_y);//.nor();
                    break;
                }
            }
            catch (Exception ignore) {}

        }


    }

    */

    @Override
    protected GameObject createProjectile() {
        return new AntiMissile(new TextureRegion(missileTexture), 1f, owner);
    }


    @Override
    public void dispose() {

        stopFire();
        super.dispose();
    }

}
