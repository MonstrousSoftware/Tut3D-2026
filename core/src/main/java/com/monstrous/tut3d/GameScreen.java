package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.gui.GUI;
import com.monstrous.tut3d.physics.CollisionShapeType;
import com.monstrous.tut3d.views.GameView;
import com.monstrous.tut3d.views.GridView;
import com.monstrous.tut3d.views.PhysicsView;

public class GameScreen extends ScreenAdapter {
    private World world;
    private GameView gameView;
    private GridView gridView;
    private PhysicsView physicsView;
    private GUI gui;
    private GameView gunView;
    private World gunWorld;
    private GameObject gun;
    private boolean thirdPersonView = false;

    private boolean debugRender = false;

    @Override
    public void show() {
        world = new World();
        Populator.populate(world);
        gui = new GUI(world, this);
        gameView = new GameView(world,false, 0.1f, 300f, 1f);
        physicsView = new PhysicsView(world);
        gridView = new GridView();
        gameView.camController.setThirdPersonMode(thirdPersonView);
        world.player.visible = thirdPersonView;            // hide player mesh in first person

        // load gun model
        gunWorld = new World();
        gunWorld.clear();
        gun = gunWorld.spawnObject(GameObjectType.TYPE_STATIC, "GunArmature", null, CollisionShapeType.BOX, true, new Vector3(0,0,0), 1f);
        gun.scene.animationController.allowSameAnimation = true;
        gun.scene.modelInstance.transform.setToScaling(Settings.gunScale, Settings.gunScale, Settings.gunScale);
        gun.scene.modelInstance.transform.setTranslation(Settings.gunPosition);

        // create an overlay view and add gun model
        gunView = new GameView(gunWorld, true, 0.01f, 10f, 0.1f);

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2) ) {
            thirdPersonView = !gameView.camController.getThirdPersonMode();
            gameView.camController.setThirdPersonMode(thirdPersonView);
            world.player.visible = thirdPersonView;            // hide player mesh in first person
            gameView.refresh();
        }

        gameView.camController.update(world.player.getPosition(), world.getPlayerController().getViewingDirection());

        world.update(delta);
        gameView.render(delta);
        if(debugRender) {
            physicsView.render(gameView.getCamera());
            gridView.render(gameView.getCamera());
        }
        if(world.weaponState.firing){
            world.weaponState.firing = false;
            if(world.weaponState.currentWeaponType == WeaponType.GUN && !thirdPersonView)
                gun.scene.animationController.setAnimation("Fire", 1);   // run the fire weapon animation once
        }
        if(!thirdPersonView && world.weaponState.currentWeaponType == WeaponType.GUN) {
            gunView.render(delta);
        }
        gui.showCrossHair( !gameView.inThirdPersonMode() );
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
