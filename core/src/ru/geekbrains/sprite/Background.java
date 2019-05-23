package ru.geekbrains.sprite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.math.Rect;

public class Background extends Sprite {

    public Background(TextureRegion textureRegion) {
        super(textureRegion);
    }

//    @Override
//    public void resize(Rect wordBounds) {
//        super.resize(wordBounds);
//
//        //setHeightProportion(1f);
//        pos.set(wordBounds.getPos());
//    }
}
