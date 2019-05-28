package ru.geekbrains.sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.math.Rect;

public class Sprite extends Rect {

    protected float angle;
    protected float scale = 1f;
    protected TextureRegion[] textureList;
    protected int frame = 0;

    public Sprite(TextureRegion textureRegion) {

        this.textureList = new TextureRegion[1];
        this.textureList[0] = textureRegion;

        // default height
        halfHeight = 1f;

        // calculate width
        resizeWidth();
    }

    protected void resizeWidth() {
        float aspect = textureList[frame].getRegionWidth() / (float) textureList[frame].getRegionHeight();
        halfWidth = halfHeight * aspect;
    }


    public void setHeightAndResize(float height) {
        halfHeight = height / 2f;
        resizeWidth();
    }

    public void draw(SpriteBatch batch) {

        batch.begin();
        batch.draw(
                textureList[frame],
                getLeft(), getBottom(),
                halfWidth, halfHeight,
                getWidth(), getHeight(),
                scale, scale,
                angle
        );
        batch.end();
    }





//    /**
//     * Resize sprite due to changing object width
//     * @param wordBounds
//     */
//    public void resize(Rect wordBounds) {
//        //setHeight(halfHeight);
//
//        float aspect = textureList[frame].getRegionWidth() / (float) textureList[frame].getRegionHeight();
//        halfWidth = halfHeight * aspect;
//        //setWidth(halfHeight * aspect);
//    }

//    public void setHeightProportion(float height) {
//
//    }

    public boolean touchDown(Vector2 touch, int pointer) {
        return false;
    }

    public boolean touchUp(Vector2 touch, int pointer) {
        return false;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Linear filter
     */
    public void setFilter() {
        textureList[frame].getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void dispose() {

        for (TextureRegion texture: textureList) {
            texture.getTexture().dispose();
        }
    }


}
