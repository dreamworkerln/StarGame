package ru.geekbrains.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.DummyObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.Missile;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Projectile;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public class MissileLauncher extends Gun {

    protected int sideLaunch = -1;

    public int sideLaunchCount = 2;

    protected DummyObject dummy;

    GameObject target = null;

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




        for (int i = 0; i < sideLaunchCount; i++) {


            Missile missile =
                    new Missile(new TextureRegion(new Texture("M-45_missile2.png")), 2, owner);

            tmp0.set(dir).setLength(owner.getRadius() + missile.getRadius() + 10)
                    .rotate(90 * sideLaunch).add(owner.pos);


            //tmp2.set(dir).scl(sideLaunch).scl(15f); // Сдвиг ракет (альтернатива задержки перед запуском, чтобы одна в другую не влетела)

            missile.pos.set(tmp0);/*.add(tmp2);*/
            missile.vel.set(owner.vel);
            missile.dir.set(dir);
            
            missile.target = target;


            tmp1.set(dir).nor().scl(sideLaunch*100);
            // apply force applied to bullet
            tmp0.set(dir).setLength(power).add(tmp1); // force  // разные ускорения слева и справа
                                                                // (альтернатива задержки перед запуском, чтобы одна в другую не влетела)
            //tmp0.rotate(60 * sideLaunch);
            missile.applyForce(tmp0);
            //tmp0.set(dir).setLength(power);


            GameScreen.addObject(missile);


            // invert launch side ---------------------------------------------
            sideLaunch = -sideLaunch;
        }



    }

    @Override
    public void draw(Renderer renderer) {

        //super.draw(renderer);



        if (target!= null && !target.readyToDispose) {

            ShapeRenderer shape = renderer.shape;

            Gdx.gl.glLineWidth(1);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shape.begin();
            shape.set(ShapeRenderer.ShapeType.Line);

            shape.setColor(0.5f, 0.9f, 0.9f, 0.5f);
            shape.circle(target.pos.x, target.pos.y, target.getRadius() * 2);

            tmp0.set(target.pos).sub(target.getRadius() * 2, 0);
            tmp1.set(tmp0).set(target.pos).add(target.getRadius()*2, 0);
            shape.line(tmp0, tmp1);

            tmp0.set(target.pos).sub(0, target.getRadius() * 2);
            tmp1.set(tmp0).set(target.pos).add(0, target.getRadius()*2);
            shape.line(tmp0, tmp1);

            shape.end();
        }

    }

}
