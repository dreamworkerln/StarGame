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


    protected Renderer renderer;

    //protected SpriteBatch batch;

    //ShapeRenderer shapeRenderer = new ShapeRenderer();
    //protected ShapeRenderer shapeRenderer;


    private Vector3 touch;
    private int oldScreenX, oldScreenY;
    public Vector2 target;    // положение курсора в игровим мире

    protected Rect touchBounds;  // координаты экрана телефона, в том виде, как он их выплевывает onTouch
    protected Rect worldBounds;  // мировые координаты
    protected Rect clipBounds;   // clip space + screen space (костыли)
    protected float aspect;

    private Matrix4 worldToClip;
    private Matrix3 screenToWorld;



    @Override
    public void show() {
        System.out.println("show");
        Gdx.input.setInputProcessor(this);

        this.renderer = new Renderer(new SpriteBatch(), new ShapeRenderer());
        renderer.shape.setAutoShapeType(true);

        this.touch = new Vector3();
        this.target = new Vector2();
        this.touchBounds = new Rect();
        this.worldBounds = new Rect();
        //this.clipBounds = new Rect(0, 0, 1f, 1f);
        this.clipBounds = new Rect();
        this.worldToClip = new Matrix4();
        this.screenToWorld = new Matrix3();

        oldScreenX = -1;
        oldScreenY = -1;
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        System.out.println("resize width = " + width + " height = " + height);

        aspect = width / (float) height;

        // setup transition to another coordinate system using affine transformation

        // https://open.gl/transformations
        // https://raw.githubusercontent.com/Overv/Open.GL/master/ebook/Modern%20OpenGL%20Guide.pdf (p47)
        // https://compgraphics.info/2D/affine_transform.php
        // (use tor-browser if can't access this pages)

        // setup World
        worldBounds.setHeight(1000f);
        worldBounds.setWidth(1000f);

        //https://learnopengl.com/Getting-started/Coordinate-Systems

        // OpenGL ((-1,-1), ( 1, 1)) clip space
        clipBounds.setHeight(2f);
        clipBounds.setWidth(2f / aspect); // костыль на aspect ratio
        // (видимо, где-то там в блевотеке lib_gdx aspect уже учитывается, в районе вызова glViewport)

        // Get Transition matrix from world to ClipSpace coordinate system
        MatrixUtils.calcTransitionMatrix(worldToClip, worldBounds, clipBounds);

        // apply to batcher and shapeRenderer
        renderer.batch.setProjectionMatrix(worldToClip);
        renderer.shape.setProjectionMatrix(worldToClip);

        // Prepare transition from Screen to World
        // set translation vector between old and new coordinate system (in old coordinates)
        touchBounds.setPos( new Vector2(width / 2f, height / 2f));

        // so we will apply translation first, then scaling
        // - y to apply inversion transformation over X axe
        touchBounds.setSize(width  / aspect, -height);

        // Get Transition matrix from Screen to World coordinate system
        MatrixUtils.calcTransitionMatrix(screenToWorld, touchBounds, worldBounds);
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
        renderer.dispose();
    }


    @Override
    public boolean keyDown(int keycode) {
        //System.out.println("keyDown keycode = " + keycode);

        switch (keycode) {

            case 51:
                KeyDown.W = true;
                break;

            case 32:
                KeyDown.D = true;
                break;

            case 47:
                KeyDown.S = true;
                break;

            case 29:
                KeyDown.A = true;
                break;

            case 62:
                KeyDown.SPACE = true;
                break;

            case 131:
                dispose();
                System.exit(0);
                break;
        }


        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        //System.out.println("keyUp keycode = " + keycode);

        switch (keycode) {

            case 51:
                KeyDown.W = false;
                break;

            case 32:
                KeyDown.D = false;
                break;

            case 47:
                KeyDown.S = false;
                break;

            case 29:
                KeyDown.A = false;
                break;

            case 62:
                KeyDown.SPACE = false;
                break;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        //System.out.println("keyTyped keycode = " + character);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {


        switch (button) {

            case 0:
                KeyDown.MOUSE0 = true;
                break;

            case 1:
                KeyDown.MOUSE1 = true;
                break;
        }


        //System.out.println("touchDown screenX = " + screenX + " screenY = " + screenY +" button = " + button);
        touch.set(screenX, screenY, 1).mul(screenToWorld);
        target.set(touch.x, touch.y);
        touchDown(touch, pointer);
        return false;
    }

    public boolean touchDown(Vector3 touch, int pointer) {
        //System.out.println("touchDown touchX = " + touch.x + " touchY = " + touch.y);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        switch (button) {

            case 0:
                KeyDown.MOUSE0 = false;
                break;

            case 1:
                KeyDown.MOUSE1 = false;
                break;
        }


        //System.out.println("touchUp screenX = " + screenX + " screenY = " + screenY +" button = " + button);
        touch.set(screenX, screenY, 1).mul(screenToWorld);
        target.set(touch.x, touch.y);
        touchUp(touch, pointer);
        return false;
    }

    public boolean touchUp(Vector3 touch, int pointer) {
        //System.out.println("touchUp touchX = " + touch.x + " touchY = " + touch.y);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        //System.out.println("touchDragged screenX = " + screenX + " screenY = " + screenY +" pointer = " + pointer);
        touch.set(screenX, screenY, 1).mul(screenToWorld);
        target.set(touch.x, touch.y);
        touchDragged(touch, pointer);
        return false;
    }

    public boolean touchDragged(Vector3 touch, int pointer) {
        //System.out.println("touchDragged touchX = " + touch.x + " touchY = " + touch.y);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        if (screenX != oldScreenX ||
            screenY != oldScreenY) {

            touch.set(screenX, screenY, 1).mul(screenToWorld);
            target.set(touch.x, touch.y);

            oldScreenX = screenX;
            oldScreenY = screenY;
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        //System.out.println("scrolled amount = " + amount);
        return false;
    }
}
