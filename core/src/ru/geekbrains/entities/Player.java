package ru.geekbrains.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.screen.KeyDown;

public class Player extends DrivenObject {

    public Player(TextureRegion textureRegion, float height) {
        super(textureRegion, height);
    }


    @Override
    protected void guide() {

        if (KeyDown.A) {
            dir.rotateRad(maxRotationSpeed);
        }
        if (KeyDown.D) {
            dir.rotateRad(-maxRotationSpeed);
        }

        if (KeyDown.W) {
            throttle += maxThrottle * 0.05f;
            if (throttle >= maxThrottle) {
                throttle = maxThrottle;
            }
        }

        if (KeyDown.S) {
            throttle -= maxThrottle * 0.05f;
            if (throttle < 0) {
                throttle = 0;
            }
        }

        if (KeyDown.SPACE) {
            throttle = maxThrottle * 0.5f;
            KeyDown.SPACE_TRIGGER_ON = true;
        }

        if (!KeyDown.SPACE && KeyDown.SPACE_TRIGGER_ON) {
            throttle = 0;
            KeyDown.SPACE_TRIGGER_ON = false;
        }





    }


}
