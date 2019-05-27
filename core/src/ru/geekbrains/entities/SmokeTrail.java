package ru.geekbrains.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.Iterator;
import java.util.LinkedList;

import ru.geekbrains.storage.Game;

public class SmokeTrail {

    static class TraceElement {

        public static long TTL = 60; // time to live (in ticks)
        public static long speed = 400; // time to live (in ticks)

        public static float radius = 5f;

        public Vector2 pos;
        public Vector2 vel;
        protected Vector2 tmp;

        public long expired;

        public TraceElement(Vector2 pos, Vector2 dir, Vector2 vel) {

            this.tmp = new Vector2();

            this.pos = pos;
            this.expired = Game.INSTANCE.getTick() + TTL;
            this.vel = dir.nor().scl(-speed);
        }


        public void update(float dt) {

            tmp.set(vel);
            pos.add(tmp.scl(dt));
        }
    }




    private LinkedList<TraceElement> list = new LinkedList<>();


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
    

    public void add(Vector2 pos, Vector2 dir, Vector2 vel) {

        list.add(new TraceElement(pos.cpy(), dir.cpy(), vel.cpy()));
    }




    public void draw(ShapeRenderer shape) {

        Gdx.gl.glLineWidth(32);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.begin();
        shape.set(ShapeRenderer.ShapeType.Line);

        long tick = Game.INSTANCE.getTick();



        for(TraceElement el : list) {
            shape.setColor(0.5f, 0.5f, 0.5f, 1f*((el.expired - tick)/(float)TraceElement.TTL));

            shape.circle(el.pos.x, el.pos.y, TraceElement.radius +
                    TraceElement.radius * 4 *(1-((el.expired - tick)/(float)TraceElement.TTL)));
        }
        //Gdx.gl.glLineWidth(50);
        shape.end();
        Gdx.gl.glLineWidth(1);
    }



}
