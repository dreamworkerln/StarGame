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

    public float maxRange = 1000;

    // Список целей, по которым идет огонь
    // (По которым запущены противо-ракеты и идет поражение)
    protected Map<GameObject, AntiMissile> targetMissile = new HashMap<>(); // Назначение антиракет по целям

    private List<GameObject> inboundMissiles = new ArrayList<>();


    public AntiMissileLauncher(float height, GameObject owner) {
        super(height, owner);

        //fireRate = 0.1f;
        fireRate = 0.05f;
        gunHeatingDelta = 50;
        coolingGunDelta = 1.5f;
        //coolingGunDelta = 60f;
        maxGunHeat = 200;
        power = 300;

    }


    @Override
    protected void fire() {


        if (target == null || target.readyToDispose){
            return;
        }

        AntiMissile missile =
                new AntiMissile(new TextureRegion(new Texture("M-45_missile2.png")), 1f, owner);

        targetMissile.put(target, missile);



        tmp1.set(target.pos).sub(owner.pos);

        // Берем в качестве точки прицеливания(выпуска ракеты) линейное приближение

        // 1. get distance to target
        float dst = tmp1.len() - (owner.getRadius() + target.getRadius());
        if (dst < 0) {
            dst = 0;
        }

        //tmp4.set(tmp1).nor();

        // 2. get speed vector projection to vector target - ship

        tmp2.set(target.vel);
        float pr = tmp2.dot(tmp1)/tmp1.len();
        tmp2.nor().scl(pr);

        // same with ship speed, then
        tmp3.set(owner.vel);
        pr = tmp3.dot(tmp1)/tmp1.len();
        tmp3.nor().scl(pr);

        // sum both projections (sub due to inverse)
        tmp2.add(tmp3);

        // time to collision
        float tt = dst/tmp2.len();

        // дистанция, которую пройдет цель
        tmp4.set(tmp2).scl(tt);

        // позиция цели через tt
        tmp3.set(target.pos).add(tmp4);


        tmp1.set(tmp3).sub(owner.pos).nor();




        // set launcher collinear to vector ship - target
        dir.set(tmp1);

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

}
