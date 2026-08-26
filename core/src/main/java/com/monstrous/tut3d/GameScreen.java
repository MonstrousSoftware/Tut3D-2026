package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen extends ScreenAdapter {
    private PerspectiveCamera cam;
    private Model cubeModel;
    private ModelInstance cubeInstance;
    private ModelBatch modelBatch;

    @Override
    public void show() {
        // Prepare your screen here.

        // create camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(),  Gdx.graphics.getHeight());
        cam.position.set(10f, 1.5f, 5f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        modelBatch = new ModelBatch();

        ModelBuilder modelBuilder = new ModelBuilder();

        // create model
        cubeModel = modelBuilder.createBox(5f, 5f, 5f,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // create model instance
        cubeInstance = new ModelInstance(cubeModel, 0, 0, 0);
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        cubeInstance.transform.rotate(Vector3.Y, 45f * delta);

        ScreenUtils.clear(Color.TEAL, true);
        modelBatch.begin(cam);
        modelBatch.render(cubeInstance);
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
        modelBatch.dispose();
    }
}
