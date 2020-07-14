package ru.geekbrains.entities.weapons.launchers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import ru.geekbrains.entities.objects.DummyObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.PlayerShip;
import ru.geekbrains.entities.projectile.missile.AbstractMissile;
import ru.geekbrains.entities.projectile.missile.FastMissile;
import ru.geekbrains.entities.projectile.missile.Missile;
import ru.geekbrains.entities.projectile.missile.PlasmaFragMissile;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public class PlayerMissileLauncher extends MissileLauncher {

    private DummyObject dummy;

    public PlayerMissileLauncher(float height, GameObject owner) {
        super(height, owner);

        addAmmoType(() -> new FastMissile(new TextureRegion(MissileLauncher.MISSILE_TEXTURE), 1.5f, owner));
        dummy = new DummyObject(owner);
    }



    private List<GameObject> getTarget() {

        List<GameObject> result = new ArrayList<>();

        List<GameObject> targets;
        dummy.pos.set(GameScreen.INSTANCE.target);


        Predicate<GameObject> filter = t-> (t.type.contains(ObjectType.SHIP) || t.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) &&
            t != this.owner && t.owner != this.owner && !t.readyToDispose && t.side != owner.side;

        targets = GameScreen.getCloseObjects(dummy, 4000, filter);


//        targets.removeIf(t -> (!t.type.contains(ObjectType.SHIP) && !t.type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)));
//        targets.removeIf(t -> t == this.owner);
//        targets.removeIf(t -> t.owner == this.owner);
//        targets.removeIf(t -> t.readyToDispose);
//        targets.removeIf(t -> t.side == owner.side);

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




    @Override
    protected boolean selectTarget() {

        if(launchCnt == 0) {

            maxRepeatCount = 1;

            launchDelay = LAUNCH_DELAY_INITIAL;
            currentAmmoType = Missile.class;
            targetList = getTarget();

            visualTargets.clear();
        }

        if (launchCnt == 1) {
            targetList.removeIf(o-> o.readyToDispose);
        }


        if (targetList.size() == 0) {
            launchCnt = 0;
            return false;

        }


        if (launchCnt == 0 && targetList.get(0).type.contains(ObjectType.MISSILE_ENEMY_SHIP)) {
            maxRepeatCount = 4;

            launchDelay = 10;
            currentAmmoType = FastMissile.class;
        }

        // if first target is NewtonMissile/MissileEnemyShip - remove other targets
        if (targetList.get(0).type.contains(ObjectType.GRAVITY_REPULSE_MISSILE)) {

            GameObject tmp = targetList.get(0);
            targetList.clear();
            targetList.add(tmp);
        }

        if (targetList.get(0).type.contains(ObjectType.BATTLE_ENEMY_SHIP)) {

            currentAmmoType = PlasmaFragMissile.class;

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
        return true;
    }



    @Override
    public void draw(Renderer renderer) {

        super.draw(renderer);

        if (renderer.rendererType!= RendererType.SHAPE) {
            return;
        }

        for (GameObject o : visualTargets) {

            // Рисуем перекрестье на цели
            if (o != null && !o.readyToDispose) {


                // Рисуем перекрестье на цели
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

    }


}
