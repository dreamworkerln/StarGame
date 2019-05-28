package ru.geekbrains.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import ru.geekbrains.storage.Game;

public class Explosion implements Disposable {

    private Vector2 pos;
    private float maxRadius;
    private float radius;
    private long start;
    //int frame;

    public Explosion (Vector2 pos, float radius) {

        this.pos = pos.cpy();
        this.start = Game.INSTANCE.getTick();
        this.maxRadius = radius;
    }


    public void update(float dt) {

        long frame = (int)(Game.INSTANCE.getTick() - start);

        if(frame >= 0 && frame < 5) {
            radius =  maxRadius * 0.1f;
        }
        else if(frame >= 5 && frame < 10) {
            radius =  maxRadius * 0.5f;
        }
        else if(frame >= 10 && frame < 15) {
            radius =  maxRadius * 1f;
        }
        else if(frame >= 15 && frame < 30) {
            radius =  maxRadius  - maxRadius * ((frame - 15)/15f);
        }
        else {
            radius = 0;
        }




//        if(frame>=0 && frame < 10) {
//            radius =  maxRadius * 0.1f;
//        }
//        else if(frame >=10 && frame < 20 ) {
//            radius =  maxRadius * 0.5f;
//        }
//        else if(frame >=20 && frame < 30 ) {
//            radius =  maxRadius * 0.5f;
//        }
    }


    public void draw(ShapeRenderer shape) {

        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin();
        shape.set(ShapeRenderer.ShapeType.Filled);

        shape.setColor(1f, 1f, 0.2f, 1);
        shape.circle(pos.x, pos.y, radius);

        Gdx.gl.glLineWidth(1);
        shape.end();
    }






    @Override
    public void dispose() {

    }
}
