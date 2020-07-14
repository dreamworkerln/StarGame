package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.concurrent.ThreadLocalRandom;

import ru.geekbrains.entities.auxiliary.TrajectorySimulator;
import ru.geekbrains.entities.equipment.CompNames;
import ru.geekbrains.entities.particles.Message;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.KeyDown;


public class JebediahKerman extends DrivenObject {


    public JebediahKerman(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.PLAYER_KERMAN);

        damage = 0.1f;
        armour = 1;
        penetration = 0.1f;
        side = ObjectSide.ALLIES;



        setMass(0.01f);
        setMaxFuel(15);
        fuelConsumption = 1f;

        setMaxThrottle(2f);
        setMaxHealth(1f);
        guideVector.set(dir);
        maxRotationSpeed = 0.08f;

        TrajectorySimulator trajectorySim = new TrajectorySimulator(this, this);

        addComponent(CompNames.SIM_TRAJECTORY, trajectorySim);

    }

    @Override
    public void update(float dt) {
        super.update(dt);
    }

    @Override
    protected void guide(float dt) {


        float rot = maxRotationSpeed;
        float currentThrottle = maxThrottle;

        if (KeyDown.SHIFT) {
            rot = maxRotationSpeed/2;
            currentThrottle = maxThrottle/2;
        }

        if (KeyDown.A) {
            guideVector.rotateRad(rot);

        }
        if (KeyDown.D) {
            guideVector.rotateRad(-rot);
        }

        if (KeyDown.W) {
            requiredThrottle = throttle + currentThrottle * 0.05f;
        }

        if (KeyDown.S) {
            requiredThrottle = throttle - currentThrottle * 0.05f;
        }

        // full throttle ------------------------
        if (KeyDown.SPACE) {
            requiredThrottle = currentThrottle;
            KeyDown.SPACE_TRIGGER_ON = true;
        }

        if (!KeyDown.SPACE && KeyDown.SPACE_TRIGGER_ON) {
            requiredThrottle = 0;
            KeyDown.SPACE_TRIGGER_ON = false;
        }

        if (KeyDown.SCROLLED != 0) {

            rot = maxRotationSpeed/2 * KeyDown.SCROLLED;

            guideVector.rotateRad(rot);
            KeyDown.SCROLLED = 0;
        }
    }


    private void spawnPlayerShip() {

        PlayerShip  playerShip = new PlayerShip(new TextureRegion(new Texture("ship_player.png")), 50, null);
        GameScreen.INSTANCE.playerShip = playerShip;

        float fi = (float) ThreadLocalRandom.current().nextDouble(0, 2*Math.PI);
        Planet planet = GameScreen.INSTANCE.planet;
        float x = (float) ((planet.radius + playerShip.radius + 30) * Math.cos(fi));
        float y = (float) ((planet.radius + playerShip.radius + 30) * Math.sin(fi));
        tmp0.set(planet.pos).add(x,y);



        tmp1.set(tmp0).sub(planet.pos).nor();
        tmp2.set(tmp1).scl(200);
        playerShip.pos.set(tmp0);
        playerShip.vel.set(tmp2);
        playerShip.dir.set(tmp1);
        playerShip.guideVector.set(playerShip.dir);
        GameScreen.addObject(playerShip);

        Message msg = new Message("You have got new ship, proceed", 0);
        GameScreen.INSTANCE.particleObjects.add(msg);
    }

    @Override
    public void dispose() {

        if (!shouldExplode) {
            spawnPlayerShip();
        }




        super.dispose();
    }
}
