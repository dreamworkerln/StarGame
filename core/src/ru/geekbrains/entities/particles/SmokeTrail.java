package ru.geekbrains.entities.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.Iterator;
import java.util.LinkedList;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

public class SmokeTrail extends GameObject{

    public long speed = 300L; // trail speed

    protected GameObject owner;
    public Color color;
    public Color bufColor;

    public boolean isStatic = false;

    public SmokeTrail(float radius, Color color, GameObject owner) {

        this.owner = owner;
        this.color = color;
        this.radius = radius;
        this.bufColor = new Color();
    }

    private LinkedList<TraceElement> list = new LinkedList<>();


    public void update(float dt) {

        // pos already setted in setTrailPos()
        dir.set(owner.dir).scl(-1);
        // НАРУШЕНИЕ ФИЗИКИ, НО ТАК КРАСИВЕЕ

        if (!isStatic) {
            vel.set(owner.vel);
        }

        //vel.set(owner.vel);//.scl(-1);

        //long tick = GameScreen.INSTANCE.getTick();


        // move smoke
        for(TraceElement el : list) {
            el.update(dt);
        }

        Iterator<TraceElement> it = list.iterator();
        if (it.hasNext()) {
            TraceElement el = it.next();
            if (el.readyToDispose) {
                it.remove();
            }
        }




        // allow to dispose owner of SmokeTrail if owner is destroyed
        // Чтобы дым весь рассеялся перед тем как убирать со сцены
        readyToDispose = list.size() == 0;
    }




    public void add(float throttlePercent) {

        list.add(new TraceElement(throttlePercent, this));
    }



    public void draw(Renderer renderer) {

        super.draw(renderer);

        if (renderer.rendererType!=RendererType.SHAPE) {
            return;
        }

        ShapeRenderer shape = renderer.shape;


        Gdx.gl.glLineWidth(radius);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        //shape.begin();
        shape.set(ShapeRenderer.ShapeType.Line);

        //long tick = GameScreen.INSTANCE.getTick();


        for(TraceElement el : list) {

            bufColor.set(color);

            bufColor.a = color.a - 1f*((el.getAge())/(float)el.getTTL());

            if (bufColor.a < 0 || bufColor.a > 1) {
                bufColor.a = 0;
            }


            shape.setColor(bufColor);


            shape.circle(el.pos.x, el.pos.y, el.getRadius());


            //shape.setColor(0.5f, 0.5f, 0.5f, 1f*((el.expired - tick)/(float)SmokeTrail.launchDelay));

//            shape.circle(el.pos.x, el.pos.y, 2 *radius +
//                    radius * 2 * el.throttlePercent *(1-(el.getAge()/(float)launchDelay)));

            //shape.circle(el.pos.x, el.pos.y, 10);
        }
        Gdx.gl.glLineWidth(1);
        //shape.end();

    }

    public void stop() {

        for(TraceElement el : list) {
            el.stop();
        }

    }




    @Override
    public void dispose() {

//        for(TraceElement el : list) {
//            el.dispose();
//        }
        list.clear();
    }

    public void setTrailPos(Vector2 pos) {
        this.pos.set(pos);
    }


    // =============================================================================================




    class TraceElement extends ParticleObject {


        float throttlePercent;

        //public long expired;

        /**
         *
         * @param pos thruster pos
         * @param dir negated thruster direction
         * @param vel ship velocity
         */
        public TraceElement(float throttlePercent, GameObject owner) {
            super(owner);


            //this.tmp = new Vector2();

            this.throttlePercent = throttlePercent;


            if (isStatic) {
                this.pos.set(owner.pos);
            }
            else {
                tmp3.set(owner.dir).scl(throttlePercent * owner.getRadius() * 1.5f);
                tmp1.set(owner.pos).add(tmp3);
                this.pos.set(tmp1);
            }



            this.dir.set(owner.dir);
            this.radius = owner.getRadius() * throttlePercent;

            tmp0.set(owner.dir).scl(speed*throttlePercent);
            //this.vel.set(tmp0);
            this.vel.set(owner.vel).add(tmp0);

            this.TTL = (long) (owner.getTTL()*throttlePercent);
        }

        public void update(float dt) {

            // no super - update age and pos manually
            age = GameScreen.INSTANCE.getTick() - birth;

            tmp0.set(vel);

            pos.add(tmp0.scl(dt)/*HAAX.scl(1.0f)*/);


            if (radius < 2* owner.getRadius()) {
                radius += 0.2f;
            }

            readyToDispose = age > TTL;
        }

        public void stop() {

            vel.setZero();

        }
    }

}
