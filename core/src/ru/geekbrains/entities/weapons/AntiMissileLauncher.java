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

    public float maxRange = 800;

    // Список целей, по котоым идет огонь
    // По которым запущены противо-ракеты и идет поражение
    protected Map<GameObject, AntiMissile> targetMissile = new HashMap<>(); // Назначение антиракет по целям

    private List<GameObject> inboundMissiles = new ArrayList<>();


    public AntiMissileLauncher(float height, GameObject owner) {
        super(height, owner);

        //dafireRate = 0.1f;
        fireRate = 0.05f;
        gunHeatingDelta = 60;
        coolingGunDelta = 1;
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



        tmp1.set(target.pos).sub(owner.pos).nor();

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
                    !o.readyToDispose && o.type.contains(ObjectType.MISSILE)) {

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
