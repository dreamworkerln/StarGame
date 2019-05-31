package ru.geekbrains.entities.equipment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.particles.ParticleObject;
import ru.geekbrains.screen.Renderer;

public class ForceShield extends ParticleObject {


    public Color color;
    public Color bufColor;

    public Color chargingColor = new Color(1f, 1f, 0.5f, 1f);

    public float power;
    public float maxPower = 1;

    public float powerIncrementDelta = 0.001f;

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
            power += powerIncrementDelta;
        }
        if (power <  0) {
            power = 0;
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

        if (power < maxPower) {
            bufColor.set(chargingColor);
            bufColor.a = power * 0.7f;
        }
        else {
            bufColor.set(color);
            bufColor.a = power * 0.5f;
        }


        shape.setColor(bufColor);
        shape.circle(pos.x, pos.y, radius);
        Gdx.gl.glLineWidth(1);
        shape.end();
    }
}
