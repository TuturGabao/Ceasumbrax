package com.Ceasumbrax;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class CeasumbraxGame extends ApplicationAdapter implements InputProcessor {
    private int WIDTH;
    private int HEIGHT;

    private ShapeRenderer shapeRenderer;

    private int squareWidth;
    private int squareHeight;

    public int speed;

    private MapRelated mapRelated;
    private SpriteBatch batch;

    public boolean left;
    public boolean right;
    public boolean up;
    public boolean down;
    public boolean shifted;

    private final int leftKey = Input.Keys.A;
    private final int rightKey = Input.Keys.D;
    private final int upKey = Input.Keys.W;
    private final int downKey = Input.Keys.S;
    private final int shiftKey = Input.Keys.SHIFT_LEFT;

    private DataReader dataReader;

    OrthographicCamera camera = new OrthographicCamera();


    public void init() {
        squareWidth = 80;
        squareHeight = 80;

        speed = 300;

        WIDTH = Gdx.graphics.getDisplayMode().width;
        HEIGHT = Gdx.graphics.getDisplayMode().height;

        mapRelated = new MapRelated(WIDTH, HEIGHT, "Arthur Game", false);
        batch = new SpriteBatch();
        camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(camera.combined);

        Gdx.input.setInputProcessor(this);

    }

    @Override
    public void create() {
        init();

        dataReader = new DataReader("Arthur Game");

        // Gdx.graphics.setWindowedMode(WIDTH, HEIGHT);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        handleMovement(delta);

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw square
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        //shapeRenderer.rect(player.x, player.y, squareWidth, squareHeight);

        batch.begin();

        int[][] tiledMap = mapRelated.drawTiledMapReturningIntMap(batch);

        batch.end();


        Gdx.graphics.setForegroundFPS(120);
        //System.out.println(Gdx.graphics.getFramesPerSecond());

        shapeRenderer.end();
    }

    public void handleMovement(float delta) {

        mapRelated.left = left;
        mapRelated.right = right;
        mapRelated.up = up;
        mapRelated.down = down;
        mapRelated.shifted = shifted;

        mapRelated.handleMapMovement(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == leftKey) left = true;
        if (keycode == rightKey) right = true;
        if (keycode == upKey) up = true;
        if (keycode == downKey) down = true;
        if (keycode == shiftKey) shifted = true;

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == leftKey) left = false;
        if (keycode == rightKey) right = false;
        if (keycode == upKey) up = false;
        if (keycode == downKey) down = false;
        if (keycode == shiftKey) shifted = false;

        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
