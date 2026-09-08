package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.gui.GUI;
import com.monstrous.tut3d.inputs.MyControllerAdapter;
import com.monstrous.tut3d.nav.NavMeshView;
import com.monstrous.tut3d.physics.CollisionShapeType;
import com.monstrous.tut3d.views.GameView;
import com.monstrous.tut3d.views.GridView;
import com.monstrous.tut3d.views.PhysicsView;

public class GameScreen extends ScreenAdapter {
    private World world;
    private GameView gameView;
    private GridView gridView;
    private PhysicsView physicsView;
    private ScopeOverlay scopeOverlay;
    private NavMeshView navMeshView;
    private GUI gui;
    private GameView gunView;
    private World gunWorld;
    private GameObject gun;
    private boolean thirdPersonView = false;
    private boolean lookThroughScope = false;
    private boolean debugRender = false;
    private int windowedWidth, windowedHeight;
    private boolean navScreen = false;

    @Override
    public void show() {
        world = new World();
        Populator.populate(world);
        gui = new GUI(world, this);
        gameView = new GameView(world,false, 0.1f, 300f, 1f);
        physicsView = new PhysicsView(world);
        gridView = new GridView();
        navMeshView = new NavMeshView();
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
        gunView = new GameView(gunWorld, true, 0.01f, 10f, 1.0f);
        scopeOverlay = new ScopeOverlay();

        if (Controllers.getCurrent() != null) {
            MyControllerAdapter controllerAdapter = new MyControllerAdapter(world.getPlayerController(), this);
            Controllers.addListener(controllerAdapter);
        }

        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        im.addProcessor(gui.stage);
        im.addProcessor(world.getPlayerController());
        im.addProcessor(gameView.camController);

        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    private void setScopeMode( boolean scopeView ){
        // scope view is only activated if player is holding gun
        // and we're in first person view
        //
        boolean sv = scopeView && !thirdPersonView && world.weaponState.currentWeaponType == WeaponType.GUN;
        if(sv == this.lookThroughScope) // no change
            return;
        this.lookThroughScope = sv;
        if(sv)  // entering scope view
            gameView.setFieldOfView(20f);        // very narrow field of view
        else   // leaving scope view, back to normal view
            gameView.setFieldOfView(67f);
    }

    private void toggleFullScreen() {        // toggle full screen / windowed screen
        if (!Gdx.graphics.isFullscreen()) {
            windowedWidth = Gdx.graphics.getWidth();        // remember current width & height
            windowedHeight = Gdx.graphics.getHeight();
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        } else {
            Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
            resize(windowedWidth, windowedHeight);
        }
    }

    @Override
    public void render(float delta) {
        setScopeMode(world.weaponState.scopeMode);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        if(Gdx.input.isKeyJustPressed(Input.Keys.R))
            restart();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            debugRender = !debugRender;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2) ) {
            thirdPersonView = !gameView.camController.getThirdPersonMode();
            gameView.camController.setThirdPersonMode(thirdPersonView);
            world.player.visible = thirdPersonView;            // hide player mesh in first person
            gameView.refresh();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3) )
            navScreen = !navScreen;
        if(Gdx.input.isKeyJustPressed(Input.Keys.F11))
            toggleFullScreen();

        gameView.camController.update(world.player.getPosition(), world.getPlayerController().getViewingDirection());

        world.update(delta);
        float moveSpeed = world.player.body.getVelocity().len();
        gameView.render(delta, moveSpeed);
        if(debugRender) {
            physicsView.render(gameView.getCamera());
            gridView.render(gameView.getCamera());
        }
        if(world.weaponState.firing){
            world.weaponState.firing = false;
            if(world.weaponState.currentWeaponType == WeaponType.GUN && !thirdPersonView && !lookThroughScope)
                gun.scene.animationController.setAnimation("Fire", 1);   // run the fire weapon animation once
            scopeOverlay.startRecoilEffect();
        }
        if(!thirdPersonView && world.weaponState.currentWeaponType == WeaponType.GUN &&!lookThroughScope) {
            gunView.getCamera().position.y = Settings.eyeHeight; // reset camera height
            gunView.render(delta, moveSpeed);
        }
        if(lookThroughScope)
            scopeOverlay.render(delta);

        if(navScreen) {
            navMeshView.update(world);
            navMeshView.render(gameView.getCamera());
        }
        gui.showCrossHair( !gameView.inThirdPersonMode() );
        gui.render(delta);
    }

    public void restart() {
        Populator.populate(world);
        Gdx.input.setCursorCatched(true);
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
        scopeOverlay.dispose();
        navMeshView.dispose();
    }
}
