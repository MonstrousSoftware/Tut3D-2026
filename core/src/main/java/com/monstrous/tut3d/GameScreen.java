package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen extends ScreenAdapter {
    private World world;
    private GameView gameView;
    private GridView gridView;
    private CamController camController;

    @Override
    public void show() {
        world = new World("models/step4a.gltf");
        Populator.populate(world);
        gameView = new GameView(world);
        gridView = new GridView();

        camController = new CamController (gameView.getCamera());
        Gdx.input.setInputProcessor(camController);

        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        camController.update(delta);

        ScreenUtils.clear(Color.TEAL, true);

        world.update(delta);
        gameView.render(delta);
        gridView.render(gameView.getCamera());
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        gameView.resize(width, height);
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        gameView.dispose();
        gridView.dispose();
        world.dispose();
    }
}
