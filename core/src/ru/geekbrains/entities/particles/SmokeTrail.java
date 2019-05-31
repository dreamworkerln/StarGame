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
import ru.geekbrains.storage.Game;

public class SmokeTrail extends ParticleObject {

    public long TTL = 60; // time to live (in ticks)
    public long speed = 300; // trail speed

    public Color color;
    public Color bufColor;

    public SmokeTrail(float height, Color color, GameObject owner) {
        super(height, owner);

        this.color = color;
        bufColor = new Color();
    }

    class TraceElement {

        public Vector2 pos;
        public Vector2 vel;
        protected Vector2 tmp;
        public float throttlePercent;

        public long expired;

        /**
         *
         * @param pos thruster pos
         * @param dir negated thruster direction
         * @param vel ship velocity
         */
        public TraceElement(Vector2 pos, Vector2 dir, Vector2 vel, float throttlePercent) {

            this.tmp = new Vector2();

            this.throttlePercent = throttlePercent;
            this.pos = pos;
            this.expired = GameScreen.INSTANCE.getTick() + (long)(TTL*throttlePercent);
            this.vel = dir.nor().scl(-speed);
        }


        public void update(float dt) {

            tmp.set(vel);
            pos.add(tmp.scl(dt));
        }

    }




    private LinkedList<TraceElement> list = new LinkedList<>();


    public void update(float dt) {

        long tick = GameScreen.INSTANCE.getTick();

        Iterator<TraceElement> it = list.iterator();
        if (it.hasNext()) {

            TraceElement el = it.next();
            if (tick >= el.expired) {
                it.remove();
            }
        }

        // move smoke
        for(TraceElement el : list) {
            el.update(dt);
        }


        // allow to dispose owner of SmokeTrail if owner is destroyed
        // Чтобы дым весь рассеялся перед тем как убирать со сцены
        readyToDispose = list.size() == 0;
    }




    public void add(Vector2 pos, Vector2 dir, Vector2 vel, float throttlePercent) {

        list.add(new TraceElement(pos.cpy(), dir.cpy(), vel.cpy(), throttlePercent));
    }



    public void draw(Renderer renderer) {

        ShapeRenderer shape = renderer.shape;


        Gdx.gl.glLineWidth(radius);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.begin();
        shape.set(ShapeRenderer.ShapeType.Line);

        long tick = GameScreen.INSTANCE.getTick();



        for(TraceElement el : list) {

            bufColor.set(color);

            bufColor.a = 1f*((el.expired - tick)/(float)TTL);

            if (bufColor.a < 0) {
                bufColor.a = 0;
            }


            shape.setColor(bufColor);
            //shape.setColor(0.5f, 0.5f, 0.5f, 1f*((el.expired - tick)/(float)SmokeTrail.TTL));

            shape.circle(el.pos.x, el.pos.y, 2 *radius +
                         radius * 2 * el.throttlePercent *(1-((el.expired - tick)/(float)TTL)));
        }
        Gdx.gl.glLineWidth(1);
        shape.end();
    }




    @Override
    public void dispose() {

//        for(TraceElement el : list) {
//            el.dispose();
//        }
        list.clear();
    }

}
