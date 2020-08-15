package ru.geekbrains.entities.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;
import java.util.Set;

import ru.geekbrains.entities.objects.DrivenObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.projectile.shell.ExplosionShell;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public class Explosion extends ParticleObject {

    private float maxRadius;
    private long start;
    private Color color;
    //long frame;

    private Set<ObjectType> objType;

    private boolean trailReadyToDispose = false;

    private List<SmokeTrail> smokeTrailList = null;


//    public static float calculateNewRadius(GameObject owner) {
//
////        float newRadius = owner.getRadius() * 4;
////        if (owner.type.contains(ObjectType.FLAK_SHELL)) {
////            newRadius *= 3;
////        }
////
////        if (owner.getClass() == Fragment.class) {
////            newRadius = 1;
////        }
//
//        return newRadius;
//    }


    public Explosion (GameObject owner) {

        super(2* owner.getExplosionRadius(), owner);




        //super(Explosion.calculateNewRadius(owner), owner);

        this.mass = owner.getMass();
        this.pos = owner.pos.cpy();
        this.vel = owner.vel.cpy();
        this.start = GameScreen.INSTANCE.getTick();
        this.maxRadius = this.radius;
        this.objType = owner.type;
        this.color = owner.explosionColor;


        // move SmokeTrail fom owner to this
        if (owner instanceof SmokeTrailList /*owner.type.contains(ObjectType.DRIVEN_OBJECT)*/) {

            SmokeTrailList slist = (SmokeTrailList)owner;

            this.smokeTrailList = slist.removeSmokeTrailList();
        }




        if(owner.type.contains(ObjectType.SHIP)) {

            ExplosionShell shell = new ExplosionShell(2, owner);
            shell.pos = owner.pos.cpy();
            shell.vel = owner.vel.cpy();
            shell.acc = owner.vel.cpy();
            shell.setTTL(0);

            if(owner.type.contains(ObjectType.BATTLE_SHIP)) {
                shell.fragCount = 200;
                shell.fragTTL = 50;
                shell.explosionPower = 20;
            }


            GameScreen.addObject(shell);
        }
    }



    public void update(float dt) {

        super.update(dt);

        if (smokeTrailList != null) {
            for (SmokeTrail st : smokeTrailList) {
                st.update(dt);
            }
        }

        //frame = GameScreen.INSTANCE.getTick() - start;

        if(age >= 0 && age < 5) {
            radius =  maxRadius * 0.3f;
        }
        else if(age >= 5 && age < 10) {
            radius =  maxRadius * 0.5f;
        }
        else if(age >= 10 && age < 15) {
            radius =  maxRadius * 1f;
        }
        else if(age >= 15 && age < 30) {
            radius =  maxRadius  - maxRadius * ((age - 15)/15f);
        }
        else {
            radius = 0;

            trailReadyToDispose = true;

            if (smokeTrailList != null) {

                for (SmokeTrail st : smokeTrailList) {
                    trailReadyToDispose = trailReadyToDispose && st.readyToDispose;
                }
            }
            readyToDispose = trailReadyToDispose;
        }

        // ---------------------------------------------------------


    }



    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);

        if (renderer.rendererType!= RendererType.SHAPE) {
            return;
        }

        ShapeRenderer shape = renderer.shape;


        // render smokeTrail if have one
        if (smokeTrailList != null) {
            for (SmokeTrail st : smokeTrailList) {
                st.draw(renderer);
            }
        }

        //shape.begin();
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.set(ShapeRenderer.ShapeType.Filled);

        shape.setColor(color);


        if (age < 10) {
            //shape.setColor(1f, 1f, 0.2f, 1);
        }
        else if (age < 15) {
            //shape.setColor(1f, 1f, 0.2f, 1);
        }
        else {

            shape.getColor().a = 1 - (age - 15) / 15f;
            //shape.setColor(1f, 1f, 0.2f, 1 - (age - 15) / 15f);
        }


        /*
        if(objType.contains(ObjectType.EMP_MISSILE)) {

            float a = shape.getColor().a;
            shape.setColor();
        }

        if(objType.contains(ObjectType.GRAVITY_REPULSE_TORPEDO)) {

            float a = shape.getColor().a;
            shape.setColor();
        }
        */


        shape.circle(pos.x, pos.y, radius);

        Gdx.gl.glLineWidth(1);
        //shape.end();
    }

    @Override
    public void dispose() {

        if (smokeTrailList != null) {
            for (SmokeTrail st : smokeTrailList) {
                st.dispose();
            }
        }
    }
}
