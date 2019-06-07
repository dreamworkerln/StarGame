package ru.geekbrains.entities.equipment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.particles.ParticleObject;
import ru.geekbrains.screen.Renderer;

public class ForceShield extends ParticleObject {



    public Color color;
    public Color bufColor;

    public Color chargingColor = new Color(1f, 1f, 0.5f, 1f);

    //public float forceValue = 100f;
    public float forceValue = 200f;

    //public float forceValue = 30f;

    public float power;
    public float maxPower = 2000;
    //public float maxPower = 30000000;

    public float powerIncrementDelta =  maxPower * 0.0000005f;

    public ForceShield(GameObject owner, Color color) {
        super(owner);

        this.color = color;
        this.radius = radius * 2f;
        power = maxPower;
        bufColor = new Color();
    }


    @Override
    public void update(float dt) {

        if (owner == null) {
            return;
        }

        pos.set(owner.pos);

        if (power <  maxPower) {
            power += powerIncrementDelta * maxPower;
        }
    }


    @Override
    public void draw(Renderer renderer) {

        ShapeRenderer shape = renderer.shape;

        Gdx.gl.glLineWidth(radius);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.begin();
        shape.set(ShapeRenderer.ShapeType.Line);



//            bufColor.set(color);
//            bufColor.a = power/maxPower*0.5f;



        if (power/maxPower < 0.2) {
            bufColor.set(chargingColor);
            bufColor.a = 0.2f + power/maxPower;
        }
        else {
            bufColor.set(color);
            bufColor.a = 0.4f + power/maxPower * 0.2f;
        }


        shape.setColor(bufColor);
        shape.circle(pos.x, pos.y, radius);
        Gdx.gl.glLineWidth(1);
        shape.end();
    }
}
