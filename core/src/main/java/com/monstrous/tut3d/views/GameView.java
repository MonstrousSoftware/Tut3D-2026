package com.monstrous.tut3d.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.monstrous.tut3d.inputs.CamController;
import com.monstrous.tut3d.Settings;
import com.monstrous.tut3d.World;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class GameView implements Disposable {

    private final World world;                                // reference to World
    private final SceneManager sceneManager;
    private final PerspectiveCamera cam;
    private final Cubemap diffuseCubemap;
    private final Cubemap environmentCubemap;
    private final Cubemap specularCubemap;
    private final Texture brdfLUT;
    private final SceneSkybox skybox;
    public CamController camController;
    private boolean isOverlay;
    private float bobScale;

    public GameView(World world, boolean overlay, float near, float far, float bobScale) {
        this.world = world;
        this.isOverlay = overlay;
        this.bobScale = bobScale;
        sceneManager = new SceneManager();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(),  Gdx.graphics.getHeight());
        cam.position.set(0f, Settings.eyeHeight, 0f);
        cam.lookAt(0,Settings.eyeHeight,10f);
        cam.near = near;
        cam.far = far;
        cam.update();

        camController = new CamController (cam);

        sceneManager.setCamera(cam);

        // setup light
        DirectionalLightEx light = new net.mgsx.gltf.scene3d.lights.DirectionalShadowLight(Settings.shadowMapSize, Settings.shadowMapSize)
            .setViewport(50,50,10f,100);
        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);
        light.intensity = 3f;
        sceneManager.environment.add(light);

        // setup quick IBL (image based lighting)
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        sceneManager.setAmbientLight(0.1f);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1f/512f)); // reduce shadow acne

        // setup skybox
        if(!isOverlay) {
            skybox = new SceneSkybox(environmentCubemap);
            sceneManager.setSkyBox(skybox);
        } else {
            skybox = null;
        }
    }


    public PerspectiveCamera getCamera() {
        return cam;
    }

    public void refresh() {
        if(world.player != null)
            world.player.visible = camController.getThirdPersonMode();
        sceneManager.getRenderableProviders().clear();        // remove all scenes

        // add scene for each game object
        int num = world.getNumGameObjects();
        for(int i = 0; i < num; i++){
            Scene scene = world.getGameObject(i).scene;
            if(world.getGameObject(i).visible)
                sceneManager.addScene(scene, false);
        }
    }

    public void render(float delta ) {

//        if(!isOverlay)
//            camController.update(world.player.getPosition(), world.getPlayerController().getViewingDirection());
        cam.update();
        if(world.isDirty())
            refresh();

        sceneManager.update(delta);

        // render
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);   // clear depth buffer only
        sceneManager.render();
    }

    public void resize(int width, int height){
        sceneManager.updateViewport(width, height);
    }


    public boolean inThirdPersonMode() {
        return camController.getThirdPersonMode();
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        if(skybox != null)
            skybox.dispose();
    }
}
