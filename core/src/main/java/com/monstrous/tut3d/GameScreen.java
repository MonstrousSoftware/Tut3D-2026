package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.monstrous.tut3d.gui.GUI;
import com.monstrous.tut3d.views.GameView;
import com.monstrous.tut3d.views.GridView;
import com.monstrous.tut3d.views.PhysicsView;

public class GameScreen extends ScreenAdapter {
    private World world;
    private GameView gameView;
    private GridView gridView;
    private PhysicsView physicsView;
    private GUI gui;

    private boolean debugRender = false;

    @Override
    public void show() {
        world = new World();
        Populator.populate(world);
        gui = new GUI(world, this);
        gameView = new GameView(world);
        physicsView = new PhysicsView(world);
        gridView = new GridView();

        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        im.addProcessor(gui.stage);
        im.addProcessor(world.getPlayerController());
        im.addProcessor(gameView.camController);

        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        if(Gdx.input.isKeyJustPressed(Input.Keys.R))
            Populator.populate(world);
        if(Gdx.input.isKeyJustPressed(Input.Keys.F))
            world.shoot();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            debugRender = !debugRender;

        gameView.camController.update(world.player.getPosition(), world.getPlayerController().getViewingDirection());

        world.update(delta);
        gameView.render(delta);
        if(debugRender) {
            physicsView.render(gameView.getCamera());
            gridView.render(gameView.getCamera());
        }
        gui.render(delta);
    }

    public void restart() {
        Populator.populate(world);
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        gameView.resize(width, height);
        gui.resize(width, height);
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        gameView.dispose();
        gridView.dispose();
        physicsView.dispose();
        world.dispose();
        gui.dispose();
    }
}
