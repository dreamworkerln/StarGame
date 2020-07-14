package ru.geekbrains.entities.weapons.gun;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import ru.geekbrains.entities.objects.GameObject;

public abstract class NonRotatableGun extends AbstractGun {


    private static Sound cannonFireStatic = Gdx.audio.newSound(Gdx.files.internal("Light Naval Cannon Blast 15.mp3"));


    public NonRotatableGun(float height, GameObject owner) {
        super(height, owner);

        gunFire = cannonFireStatic;

    }

    @Override
    public void rotate() {

        // Nozzle-mounted gun
        if (owner!= null && !owner.readyToDispose) {
            dir.set(owner.dir);
        }

    }
}
