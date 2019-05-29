package ru.geekbrains.entities.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EnemyShip extends Ship {

    public EnemyShip(TextureRegion textureRegion, float height) {
        super(textureRegion, height);
    }


    @Override
    protected void guide() {

        super.guide();

        if (target != null) {
            gun.startFire();
        }
        else {
            gun.stopFire();
        }
    }
}
