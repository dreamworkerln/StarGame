package ru.geekbrains.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import ru.geekbrains.math.MatrixUtils;
import ru.geekbrains.math.Rect;

public abstract class BaseScreen implements Screen, InputProcessor {

    protected SpriteBatch batch;
    protected ShapeRenderer shapeRenderer;


    private Vector3 touch;
    protected Vector2 target;

    protected Rect screenBounds;
    protected Rect worldBounds;
    protected Rect glBounds;

    private Matrix4 worldToGl;
    private Matrix3 screenToWorld;

    @Override
    public void show() {
        System.out.println("show");
        Gdx.input.setInputProcessor(this);
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.touch = new Vector3();
        this.target = new Vector2();
        this.screenBounds = new Rect();
        this.worldBounds = new Rect();
        this.glBounds = new Rect(0, 0, 1f, 1f);
        this.worldToGl = new Matrix4();
        this.screenToWorld = new Matrix3();
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        System.out.println("resize width = " + width + " height = " + height);

        // setup transition to another coordinate system using affine transformation

        // https://open.gl/transformations
        // https://raw.githubusercontent.com/Overv/Open.GL/master/ebook/Modern%20OpenGL%20Guide.pdf (p47)
        // https://compgraphics.info/2D/affine_transform.php
        // (use tor-browser if can't access this pages)

        // Prepare transition from Screen to World

        // set translation vector between old and new coordinate system (in old coordinates)
        screenBounds.setPos( new Vector2(width / 2f, height / 2f));

        // so we will apply translation first, then scaling
        // - y to apply inversion transformation over X axe
        screenBounds.setSize(width, -height);

        float aspect = width / (float) height;

        // setup World
        worldBounds.setHeight(1f);
        worldBounds.setWidth(1f * aspect);

        // Get Transition matrix from world to Gl coordinate system
        MatrixUtils.calcTransitionMatrix(worldToGl, worldBounds, glBounds);
        // apply to batcher
        batch.setProjectionMatrix(worldToGl);
        shapeRenderer.setProjectionMatrix(worldToGl);

        // Get Transition matrix from Screen to World coordinate system
        MatrixUtils.calcTransitionMatrix(screenToWorld, screenBounds, worldBounds);
        resize(worldBounds);
    }

    public void resize(Rect worldBounds) {
        System.out.println("resize worldBounds.width = " + worldBounds.getWidth() + " worldBounds.height = " + worldBounds.getHeight());
    }

    @Override
    public void pause() {
        System.out.println("pause");
    }

    @Override
    public void resume() {
        System.out.println("resume");
    }

    @Override
    public void hide() {
        System.out.println("hide");
        dispose();
    }

    @Override
    public void dispose() {
        System.out.println("dispose");
        batch.dispose();
    }


    @Override
    public boolean keyDown(int keycode) {
        System.out.println("keyDown keycode = " + keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        System.out.println("keyUp keycode = " + keycode);
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        System.out.println("keyTyped keycode = " + character);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        System.out.println("touchDown screenX = " + screenX + " screenY = " + screenY);
        touch.set(screenX, screenY, 1).mul(screenToWorld);
        target.set(touch.x, touch.y);
        touchDown(touch, pointer);
        return false;
    }

    public boolean touchDown(Vector3 touch, int pointer) {
        System.out.println("touchDown touchX = " + touch.x + " touchY = " + touch.y);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        System.out.println("touchUp screenX = " + screenX + " screenY = " + screenY);
        touch.set(screenX, screenY, 1).mul(screenToWorld);
        target.set(touch.x, touch.y);
        touchUp(touch, pointer);
        return false;
    }

    public boolean touchUp(Vector3 touch, int pointer) {
        System.out.println("touchUp touchX = " + touch.x + " touchY = " + touch.y);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        System.out.println("touchDragged screenX = " + screenX + " screenY = " + screenY);
        touch.set(screenX, screenY, 1).mul(screenToWorld);
        target.set(touch.x, touch.y);
        touchDragged(touch, pointer);
        return false;
    }

    public boolean touchDragged(Vector3 touch, int pointer) {
        System.out.println("touchDragged touchX = " + touch.x + " touchY = " + touch.y);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        System.out.println("scrolled amount = " + amount);
        return false;
    }
}
