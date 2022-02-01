package ru.dreamworkerln.stargame.entities.objects;

import com.badlogic.gdx.math.Vector2;

import ru.dreamworkerln.stargame.entities.particles.ParticleObject;

public abstract class ShipComponent extends ParticleObject {

    protected boolean enabled = true;


    public void enable(boolean enable) {
        enabled = enable;
    }

    public ShipComponent(float height, GameObject owner) {
        super(height, owner);
    }

    public ShipComponent(GameObject owner) {
        super(owner);
    }

    public Vector2 getDir() {
        return dir;
    }

    public void setDir(Vector2 dir) {
        this.dir.set(dir);
    }

    public abstract void init();
}
