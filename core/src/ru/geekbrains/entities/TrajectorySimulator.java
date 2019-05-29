package ru.geekbrains.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import ru.geekbrains.screen.GameScreen;

public class TrajectorySimulator {

    protected GameObject target;

    protected Planet planet;

    public DummyObject tracer;

    private ArrayList<Vector2> trajectory = new ArrayList<>();


    private Vector2 tmp0 = new Vector2();


    public TrajectorySimulator(GameObject target, Planet planet) {

        this.target = target;
        this.planet = planet;

        tracer = new DummyObject(target.getRadius());
    }

    public void update(float dt) {

        // if target is dead exit
        if (target == null ||  target.readyToDispose) {

            target = null;
            return;
        }


        trajectory.clear();
        tracer.pos = target.pos.cpy();
        tracer.vel = target.vel.cpy();

        for (int i = 0; i < 1500; i++) {

            // calculate gravitation force from planet
            GameScreen.applyPlanetGravForce(tracer, planet);

            // check collision to planet
            tmp0.set(planet.pos);
            tmp0.sub(tracer.pos);
            if (tmp0.len() <= planet.getRadius() + tracer.getRadius()) {
                break;
            }

            // update velocity, position
            tracer.update(dt);

            // add new point to simulated trajectory
            trajectory.add(tracer.pos.cpy());
        }

    }


    public void draw(Renderer renderer) {

        renderer.shape.begin();
        Gdx.gl.glLineWidth(1);
        renderer.shape.set(ShapeRenderer.ShapeType.Line);
        renderer.shape.setColor(Color.YELLOW);

        for(int i = 0; i< trajectory.size()-2;i++){
            renderer.shape.line(trajectory.get(i), trajectory.get(i + 1));
        }

        Gdx.gl.glLineWidth(1);
        renderer.shape.end();
    }

}
