package ru.geekbrains.entities.auxiliary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import ru.geekbrains.entities.objects.DummyObject;
import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.Planet;
import ru.geekbrains.entities.objects.Shell;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public class TrajectorySimulator {

    protected GameObject target;

    protected Planet planet;

    public DummyObject tracer;

    public GameObject type;

    protected Color color;

    protected int iterationCount = 1500;



    private ArrayList<Vector2> trajectory = new ArrayList<>();


    private Vector2 tmp0 = new Vector2();

    public int mode = 0;


    public TrajectorySimulator(GameObject target, GameObject type) {

        this.target = target;
        this.type = type;
        this.planet = GameScreen.INSTANCE.planet;

        this.tracer = new DummyObject(target.getRadius());

        color = new Color(1f,1f,0f,0.4f);

    }

    public void update(float dt) {

        // if target is dead exit
        if (target == null ||  target.readyToDispose) {

            target = null;
            return;
        }


        trajectory.clear();

        tracer.setMass(type.getMass());
        tracer.dir.set(target.dir);
        tracer.pos.set(target.pos);
        tracer.vel.set(target.vel);


        if (type instanceof Shell) {

            tmp0.set(tracer.dir).setLength(300); // dummy shell speed
            tracer.applyForce(tmp0);             // dummy force applied to shell

            color.set(0f,0.76f,0.9f,0.5f);

            iterationCount = 150;
        }



        for (int i = 0; i <  iterationCount; i++) {

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

        ShapeRenderer shape =renderer.shape;


        shape.begin();
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.set(ShapeRenderer.ShapeType.Line);
        shape.setColor(color);

        for(int i = 0; i< trajectory.size()-2;i++){
            renderer.shape.line(trajectory.get(i), trajectory.get(i + 1));
        }

        Gdx.gl.glLineWidth(1);
        shape.end();
    }

}
