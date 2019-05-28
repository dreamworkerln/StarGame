package ru.geekbrains.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.Iterator;
import java.util.LinkedList;

import ru.geekbrains.storage.Game;

public class SmokeTrail implements Disposable {

    public static long TTL = 60; // time to live (in ticks)
    public static long speed = 300; // time to live (in ticks)

    public float radius;

    class TraceElement implements Disposable{



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
         * @param radius ship radius
         */
        public TraceElement(Vector2 pos, Vector2 dir, Vector2 vel, float throttlePercent) {

            this.tmp = new Vector2();

            this.throttlePercent = throttlePercent;
            this.pos = pos;
            this.expired = Game.INSTANCE.getTick() + (long)(TTL*throttlePercent);
            this.vel = dir.nor().scl(-speed);
        }


        public void update(float dt) {

            tmp.set(vel);
            pos.add(tmp.scl(dt));
        }

        @Override
        public void dispose() {
        }
    }




    private LinkedList<TraceElement> list = new LinkedList<>();


    public SmokeTrail(float radius) {
        this.radius = radius;
    }

    public void update(float dt) {

        long tick = Game.INSTANCE.getTick();

        Iterator<TraceElement> it = list.iterator();
        if (it.hasNext()) {
            if (tick > it.next().expired) {
                list.removeFirst();
            }
        }

        // move smoke
        for(TraceElement el : list) {
            el.update(dt);
        }
    }
    

    public void add(Vector2 pos, Vector2 dir, Vector2 vel, float throttlePercent) {

        list.add(new TraceElement(pos.cpy(), dir.cpy(), vel.cpy(), throttlePercent));
    }




    public void draw(ShapeRenderer shape) {

        Gdx.gl.glLineWidth(radius);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.begin();
        shape.set(ShapeRenderer.ShapeType.Line);

        long tick = Game.INSTANCE.getTick();



        for(TraceElement el : list) {
            shape.setColor(0.5f, 0.5f, 0.5f, 1f*((el.expired - tick)/(float)SmokeTrail.TTL));

            shape.circle(el.pos.x, el.pos.y, 2 *radius +
                    radius * 2 * el.throttlePercent *(1-((el.expired - tick)/(float)SmokeTrail.TTL)));
        }
        Gdx.gl.glLineWidth(1);
        shape.end();
    }


    @Override
    public void dispose() {

        for(TraceElement el : list) {
            el.dispose();
        }
        list.clear();
    }



}
