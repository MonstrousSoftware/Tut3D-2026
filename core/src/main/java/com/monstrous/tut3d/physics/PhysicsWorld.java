package com.monstrous.tut3d.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.tut3d.Settings;
import org.ode4j.ode.*;

import static org.ode4j.ode.OdeConstants.*;



// World of rigid body dynamics and collisions
//
public class PhysicsWorld implements Disposable {
    static final float TIME_STEP = 1f/200f;  // fixed physics time step

    DWorld world;
    public DSpace space;
    private final DJointGroup contactGroup;
    private float timeElapsed;

    public PhysicsWorld() {
        OdeHelper.initODE2(0);
        Gdx.app.log("ODE version", OdeHelper.getVersion());
        Gdx.app.log("ODE config", OdeHelper.getConfiguration());
        contactGroup = OdeHelper.createJointGroup();
        reset();
    }

    // reset world, note this invalidates (orphans) all rigid bodies and geoms so should be used in combination with deleting all game objects
    public void reset() {
        if(world != null)
            world.destroy();
        if(space != null)
            space.destroy();

        world = OdeHelper.createWorld();
        space = OdeHelper.createSapSpace( null, DSapSpace.AXES.XZY  );

        world.setGravity (0, Settings.gravity, 0);
        world.setCFM (1e-5);
        world.setERP (0.4);
        world.setQuickStepNumIterations (40);
        world.setAngularDamping(0.5f);

        // set auto disable parameters to make inactive objects go to sleep
        world.setAutoDisableFlag(true);
        world.setAutoDisableLinearThreshold(0.1);
        world.setAutoDisableAngularThreshold(0.1);
        world.setAutoDisableTime(2);
        timeElapsed = 0;
    }

    // update the physics with fixed time steps
    public void update(float deltaTime) {
        timeElapsed += deltaTime;
        while(timeElapsed > TIME_STEP) {
            space.collide(null, nearCallback);
            world.quickStep(TIME_STEP);
            contactGroup.empty();
            timeElapsed -= TIME_STEP;
        }
    }

    // called for potential collisions
    private final DGeom.DNearCallback nearCallback = new DGeom.DNearCallback() {

        @Override
        public void call(Object data, DGeom o1, DGeom o2) {
            DBody b1 = o1.getBody();
            DBody b2 = o2.getBody();
            if (b1 != null && b2 != null && OdeHelper.areConnected(b1, b2))
                return;

            final int N = 8;
            DContactBuffer contacts = new DContactBuffer(N);

            int n = OdeHelper.collide(o1, o2, N, contacts.getGeomBuffer());
            if (n > 0) {

                for (int i = 0; i < n; i++) {
                    DContact contact = contacts.get(i);
                    contact.surface.mode = dContactBounce | dContactSoftCFM;
                    contact.surface.mu = 0.1;   // friction coefficient
                    contact.surface.bounce = 0.9;       // Set restitution (0.0 to 1.0)
                    contact.surface.bounce_vel = 0.01;   // Minimum velocity to trigger bounce
                    contact.surface.soft_cfm = 0.001;

                    DJoint c = OdeHelper.createContactJoint(world, contactGroup, contact);
                    c.attach(o1.getBody(), o2.getBody());
                }
            }
        }
    };

    @Override
    public void dispose() {
        contactGroup.destroy();
        space.destroy();
        world.destroy();
        OdeHelper.closeODE();
    }
}

