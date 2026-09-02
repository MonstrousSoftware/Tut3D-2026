package com.monstrous.tut3d.physics;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.Settings;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DQuaternionC;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;

public class PhysicsBody {

    public DGeom geom;
    private final Vector3 position;               // for convenience, matches geom.getPosition() but converted to Vector3
    private final Quaternion quaternion;          // for convenience, matches geom.getQuaternion() but converted to LibGDX Quaternion
    public final ModelInstance debugInstance;    // visualisation of collision shape for debug view

    public PhysicsBody(DGeom geom, ModelInstance debugInstance) {
        this.geom = geom;
        this.debugInstance = debugInstance;
        position = new Vector3();
        quaternion = new Quaternion();
    }

    public Vector3 getPosition() {
        DVector3C pos = geom.getPosition();
        position.x = (float) pos.get0();
        position.y = (float) pos.get1();
        position.z = (float) pos.get2();
        return position;
    }

    public void setPosition( Vector3 pos ) {
        geom.setPosition(pos.x, pos.y, pos.z);
        DBody rigidBody = geom.getBody();
        if(rigidBody != null)
            rigidBody.setPosition(pos.x, pos.y, pos.z);
    }

    public Quaternion getOrientation() {
        DQuaternionC odeQ = geom.getQuaternion();
        float ow = (float) odeQ.get0();
        float ox = (float) odeQ.get1();
        float oy = (float) odeQ.get2();
        float oz = (float) odeQ.get3();
        quaternion.set(ox, oy, oz, ow);
        return quaternion;
    }

    // get orientation of rigid body, i.e. without any geom offset rotation
    public Quaternion getBodyOrientation() {
        DQuaternionC odeQ;
        if(geom.getBody() == null)      // if geom does not have a body attached, fall back to geom orientation
            odeQ = geom.getQuaternion();
        else
            odeQ = geom.getBody().getQuaternion();
        float ow = (float) odeQ.get0();
        float ox = (float) odeQ.get1();
        float oy = (float) odeQ.get2();
        float oz = (float) odeQ.get3();
        quaternion.set(ox, oy, oz, ow);
        return quaternion;
    }

    public void setOrientation( Quaternion q ){
        DQuaternion odeQ = new DQuaternion(q.w, q.x, q.y, q.z);       // convert to ODE quaternion
        geom.setQuaternion(odeQ);
        DBody rigidBody = geom.getBody();
        if(rigidBody != null)
            rigidBody.setQuaternion(odeQ);
    }

    public void applyForce( Vector3 force ){
        DBody rigidBody = geom.getBody();
        rigidBody.addForce(force.x, force.y, force.z);
    }

    public void applyTorque( Vector3 torque ){
        DBody rigidBody = geom.getBody();
        rigidBody.addTorque(torque.x, torque.y, torque.z);
    }

    public void setCapsuleCharacteristics() {
        DBody rigidBody = geom.getBody();
        rigidBody.setDamping(Settings.playerLinearDamping, Settings.playerAngularDamping);
        rigidBody.setAutoDisableFlag(false);       // never allow player to get disabled
        rigidBody.setMaxAngularSpeed(0);        // keep capsule upright by not allowing rotations
    }
}
