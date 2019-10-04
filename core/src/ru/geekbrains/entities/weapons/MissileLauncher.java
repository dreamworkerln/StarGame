package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.DummyObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.projectile.Missile;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public class MissileLauncher extends Gun {

    private int sideLaunch = -1;

    public int sideLaunchCount = 2;

    private DummyObject dummy;

    public GameObject target = null;

    protected int TTL = 10;  // задержка между запусками ракет при залпе (чтоб не попали друг в друга)

    protected long start = -1;

    private boolean reverseLaunch;


    public MissileLauncher(float height, GameObject owner) {
        super(height, owner);

        dummy = new DummyObject(owner);

        fireRate = 0.004f;
        //fireRate = 0.05f;
        gunHeatingDelta = 0;
        coolingGunDelta = 0;
        maxGunHeat = 1;
        power = 500;
    }


    @Override
    public void update(float dt) {
        super.update(dt);

        // костыли
        if (this.getClass() ==  MissileLauncher.class) {


            if (start > 0 && GameScreen.INSTANCE.getTick() - start > TTL) {

                repeatFire();
                start = -1;
                reverseLaunch = false;
            }
        }

    }





    @Override
    protected void fire() {

        List<GameObject> targets;

        if (owner.type.contains(ObjectType.PLAYER_SHIP)) {

            dummy.pos.set(GameScreen.INSTANCE.target);

            targets = GameScreen.getCloseObjects(dummy, 2000);

            for (GameObject o : targets) {

                // берем первую - ближайшую цель
                // которая не является ни owner ни его снарядами
                if (o != owner &&
                        o.owner != owner &&
                        !o.readyToDispose &&
                        (o.type.contains(ObjectType.SHIP))) {

                    target = o;
                    break;
                }
            }
        }
        else {

            target = ((DrivenObject)owner).target;
        }


        if (target== null || target.readyToDispose){

            return;
        }



        repeatFire();

        // запуск двух ракет с задержкой
        // (чтобы одна в другую не влетела при подлете к цели)
        if (sideLaunchCount > 1) {
            start = GameScreen.INSTANCE.getTick();
        }
    }


    protected void repeatFire() {


        tmp4.set(dir);
        if (reverseLaunch) {
            tmp4.scl(-1);
        }


        Missile missile =
                new Missile(new TextureRegion(new Texture("M-45_missile2.png")), 2, owner);

        tmp0.set(tmp4).setLength(owner.getRadius() + missile.getRadius()*2)
                .rotate(90 * sideLaunch).add(owner.pos);


        missile.pos.set(tmp0);
        missile.vel.set(owner.vel);
        missile.dir.set(tmp4);

        missile.target = target;


        //tmp1.set(dir).nor().scl(sideLaunch*100);
        // apply force applied to missile
        tmp1.set(tmp4).scl(power * 0.3f);
        tmp0.set(tmp4).setLength(power).rotate(40*sideLaunch).add(tmp1); // force
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

        //super.draw(renderer);

        // костыли
        if (this.getClass() ==  MissileLauncher.class) {


            if (target != null && !target.readyToDispose) {

                ShapeRenderer shape = renderer.shape;

                Gdx.gl.glLineWidth(1);
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                shape.begin();
                shape.set(ShapeRenderer.ShapeType.Line);

                shape.setColor(0.5f, 0.9f, 0.9f, 0.5f);
                shape.circle(target.pos.x, target.pos.y, target.getRadius() * 2);

                tmp0.set(target.pos).sub(target.getRadius() * 2, 0);
                tmp1.set(tmp0).set(target.pos).add(target.getRadius() * 2, 0);
                shape.line(tmp0, tmp1);

                tmp0.set(target.pos).sub(0, target.getRadius() * 2);
                tmp1.set(tmp0).set(target.pos).add(0, target.getRadius() * 2);
                shape.line(tmp0, tmp1);

                shape.end();
            }
        }

    }

}
