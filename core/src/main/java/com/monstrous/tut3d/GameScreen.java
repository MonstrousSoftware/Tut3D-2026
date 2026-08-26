package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen extends ScreenAdapter {
    private PerspectiveCamera cam;
    private CamController camController;
    private Model cubeModel;
    private Model groundModel;
    private ModelInstance cubeInstance;
    private Array<ModelInstance> instances;
    private ModelBatch modelBatch;
    private Environment environment;
    private Texture textureGround;

    @Override
    public void show() {
        // Prepare your screen here.

        // create camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(),  Gdx.graphics.getHeight());
        cam.position.set(10f, Settings.eyeHeight, 5f);
        cam.lookAt(0,Settings.eyeHeight,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        camController = new CamController(cam);
        Gdx.input.setInputProcessor(camController);

        modelBatch = new ModelBatch();

        ModelBuilder modelBuilder = new ModelBuilder();

        // create model
        cubeModel = modelBuilder.createBox(5f, 5f, 5f,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // create model instance
        cubeInstance = new ModelInstance(cubeModel, 0, 2.5f, 0);

        textureGround = new Texture(Gdx.files.internal("textures/Stylized_Stone_Floor_005_basecolor.jpg"), true);
        textureGround.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textureGround.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        TextureRegion textureRegion = new TextureRegion(textureGround);
        int repeats = 10;
        textureRegion.setRegion(0,0,textureGround.getWidth()*repeats, textureGround.getHeight()*repeats );

        // create model
        groundModel = modelBuilder.createBox(100f, 1f, 100f,
            new Material(TextureAttribute.createDiffuse(textureRegion)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        instances = new Array<>();
        instances.add(new ModelInstance(groundModel, 0, -1, 0));	// 'table top' surface
        instances.add(cubeInstance);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
        environment.add(new DirectionalLight().setColor(0.5f, 0.5f, 0.5f, 1.0f).setDirection(-0.3f, -0.8f, -0.2f));

        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        camController.update(delta);

        // Draw your screen here. "delta" is the time since last render in seconds.
        cubeInstance.transform.rotate(Vector3.Y, 45f * delta);

        ScreenUtils.clear(Color.TEAL, true);
        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        cubeModel.dispose();
        groundModel.dispose();
        modelBatch.dispose();
    }
}
