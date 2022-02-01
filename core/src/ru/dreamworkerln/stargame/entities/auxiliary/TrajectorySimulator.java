package ru.dreamworkerln.stargame.entities.auxiliary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import ru.dreamworkerln.stargame.entities.objects.DrivenObject;
import ru.dreamworkerln.stargame.entities.objects.DummyObject;
import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.objects.Planet;
import ru.dreamworkerln.stargame.entities.objects.Ship;
import ru.dreamworkerln.stargame.entities.objects.ShipComponent;
import ru.dreamworkerln.stargame.entities.projectile.Ammo;
import ru.dreamworkerln.stargame.entities.weapons.gun.CourseGun;
import ru.dreamworkerln.stargame.screen.GameScreen;
import ru.dreamworkerln.stargame.screen.Renderer;
import ru.dreamworkerln.stargame.screen.RendererType;

public class TrajectorySimulator extends ShipComponent {

    protected GameObject target;

    protected Planet planet;

    public DummyObject tracer;

    public GameObject model;

    protected Color color;

    public int baseIterationCount = 1500;

    protected int iterationCount;



    private ArrayList<Vector2> trajectory = new ArrayList<>();


    private Vector2 tmp0 = new Vector2();

    public int mode = 0;


    public TrajectorySimulator(DrivenObject owner, GameObject simType) {

        super(owner);

        this.target = owner;
        this.model = simType;
        this.planet = GameScreen.INSTANCE.planet;

        this.tracer = new DummyObject(owner);

        color = new Color(0.5f,1f,0f,0.4f);

    }


    public void update(float dt) {

        // if target is dead exit
        if (target == null ||  target.readyToDispose || !enabled) {

            target = null;
            return;
        }

        iterationCount = baseIterationCount;


        trajectory.clear();
        tracer.setMass(model.getMass());
        tracer.setRadius(model.getRadius());
        tracer.dir.set(target.dir);
        tracer.pos.set(target.pos);
        tracer.vel.set(target.vel);


        if (model.type.contains(ObjectType.SHELL)) {

            Ship ship = (Ship)target;
            CourseGun gun = (CourseGun)ship.getCourseGun();


            tracer.pos.set(gun.nozzlePos);
            tracer.dir.set(gun.dir);

            tmp0.set(target.dir).setLength(((Ammo)model).getFirePower()); // dummy shell speed
            tracer.applyForce(tmp0);                   // dummy force applied to shell

            color.set(0f,0.76f,0.9f,0.5f);

            iterationCount = baseIterationCount / 10;
        }


        for (int i = 0; i <  iterationCount; i++) {


            // calculate gravitation force from planet
            GameScreen.applyPlanetGravForce(tracer, planet);

            // check collision to planet
            tmp0.set(planet.pos).sub(tracer.pos);
            if (tmp0.len() <= planet.getRadius() + tracer.getRadius()) {
               break;
            }

            // update acceleration, velocity, position
            tracer.update(dt);

            // add new point to simulated trajectory
            trajectory.add(tracer.pos.cpy());
        }

    }

    public void draw(Renderer renderer) {

        // NO SUPER AVAILABLE

        if (renderer.rendererType != RendererType.SHAPE) {
            return;
        }

        ShapeRenderer shape = renderer.shape;

        if(!enabled) {
            return;
        }



//        //ship line of fire DEBUG
//        shape.begin();
//        Gdx.gl.glLineWidth(1);
//        Gdx.gl.glEnable(GL20.GL_BLEND);
//        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        shape.set(ShapeRenderer.ShapeType.Line);
//        shape.setColor(1f,1,1,0.5f);
//
//        tmp0.set(((Ship)target).dir).setLength(500).add(((Ship)target).pos);
//        shape.line(((Ship)target).pos,tmp0);
//
//        Gdx.gl.glLineWidth(1);
//        shape.end();
//
//        // ---------------------------------------------



        //shape.begin();
        //Gdx.gl.glLineWidth(1);
        Gdx.gl.glLineWidth(2);
        //Gdx.gl.glEnable(GL20.GL_BLEND);
        //Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.set(ShapeRenderer.ShapeType.Line);
        shape.setColor(color);

        for(int i = 0; i< trajectory.size()-2;i++){
            renderer.shape.line(trajectory.get(i), trajectory.get(i + 1));
        }

        Gdx.gl.glLineWidth(2);

        shape.flush();
        //shape.end();
    }

    @Override
    public void enable(boolean enable) {
        super.enable(enable);

        if(enable) {
            this.target = owner;
        }

    }

    @Override
    public void init() {

    }
}
