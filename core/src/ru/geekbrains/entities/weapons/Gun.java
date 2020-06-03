package ru.geekbrains.entities.weapons;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import ru.geekbrains.entities.objects.GameObject;

public class Gun extends AbstractGun {

    private static Sound cannonFireStatic;

    static {
        cannonFireStatic = Gdx.audio.newSound(Gdx.files.internal("Light Naval Cannon Blast 15.mp3"));
    }

    public Gun(float height, GameObject owner) {
        super(height, owner);
        cannonFire = cannonFireStatic;
    }

    @Override
    public void rotate() {

        // Nozzle-mounted gun
        if (owner!= null && !owner.readyToDispose) {
            dir.set(owner.dir);
        }

    }
}
